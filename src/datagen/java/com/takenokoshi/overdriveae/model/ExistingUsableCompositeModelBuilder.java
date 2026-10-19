package com.takenokoshi.overdriveae.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * NeoForgeのCompositeModelBuilderは既に存在するモデル(他のブロック用に生成済/手書き/他のModからの参照)には対応していない。
 * 自力で対応させるというゴリ押しを敢行した。
 */
public class ExistingUsableCompositeModelBuilder<T extends ModelBuilder<T>> extends CompositeModelBuilder<T> {

    private static final String CHILDREN = "children";

    private final Map<String, ResourceLocation> existingChildModels;
    /** CompositeModelBuilder側は自身の子モデルの名称しか参照しないので、こちらで組む */
    private final List<String> itemRenderOrder;

    private boolean isBlockModel;

    public static <T extends ModelBuilder<T>> ExistingUsableCompositeModelBuilder<T> begin(T parent,
            ExistingFileHelper existingFileHelper) {
        return new ExistingUsableCompositeModelBuilder<T>(parent, existingFileHelper);
    }

    protected ExistingUsableCompositeModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(parent, existingFileHelper);
        this.existingChildModels = new LinkedHashMap<>();
        this.itemRenderOrder = new ArrayList<>();
    }

    /**
     * 他のModのモデルを参照したいことがあるので、ExistingFileHelperによる検証は行わない
     * ResourceLocationによる参照なので、モデルファイルであればどんなものでも指定可
     */
    public ExistingUsableCompositeModelBuilder<T> existingChild(String name, ResourceLocation childLoc) {
        existingChildModels.put(name, childLoc);
        return this;
    }

    public ExistingUsableCompositeModelBuilder<T> block() {
        isBlockModel = true;
        return this;
    }

    /** 返り値のクラスを差し替え */
    @Override
    public ExistingUsableCompositeModelBuilder<T> child(String name, T modelBuilder) {
        super.child(name, modelBuilder);
        return this;
    }

    @Override
    public ExistingUsableCompositeModelBuilder<T> itemRenderOrder(String... names) {
        this.itemRenderOrder.addAll(Arrays.asList(names));
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        JsonObject result = super.toJson(json);
        // CompositeModelBuilder側で既にchildren:{}が生成済ならそれを、無いなら新規に作成
        JsonObject children = result.has(CHILDREN)
                ? result.getAsJsonObject(CHILDREN)
                : new JsonObject();
        existingChildModels.forEach((name, loc) -> {
            children.add(name, childJson(loc));
        });
        result.add(CHILDREN, children);

        JsonArray itemRenderOrder = new JsonArray();

        for (String name : this.itemRenderOrder) {
            itemRenderOrder.add(name);
        }

        json.add("item_render_order", itemRenderOrder);

        if (isBlockModel) {
            result.addProperty("parent", "block/block");
        }
        return result;
    }

    private static JsonObject childJson(ResourceLocation childLoc) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", childLoc.getNamespace() + ":" + childLoc.getPath());
        return json;
    }
}
