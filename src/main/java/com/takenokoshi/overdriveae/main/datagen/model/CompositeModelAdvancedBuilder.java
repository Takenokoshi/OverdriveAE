package com.takenokoshi.overdriveae.main.datagen.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.util.TransformationHelper;

/**
 * NeoForgeのCompositeModelBuilderは既に存在するモデル(他のブロック用に生成済/手書き/他のModからの参照)には対応していない。
 * 自力で対応させるというゴリ押しを敢行した。
 */
public class CompositeModelAdvancedBuilder<T extends ModelBuilder<T>> extends CompositeModelBuilder<T> {

    private static final String CHILDREN = "children";

    private final Map<String, ResourceLocation> existingChildModels;
    /** CompositeModelBuilder側は自身の子モデルの名称しか参照しないので、こちらで組む */
    private final List<String> itemRenderOrder;

    private final Map<String, ModifiedExistingChildModel<T>> modifiedChildModels;
    private boolean isBlockModel;

    public static <T extends ModelBuilder<T>> CompositeModelAdvancedBuilder<T> begin(T parent,
            ExistingFileHelper existingFileHelper) {
        return new CompositeModelAdvancedBuilder<T>(parent, existingFileHelper);
    }

    protected CompositeModelAdvancedBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(parent, existingFileHelper);
        this.existingChildModels = new LinkedHashMap<>();
        this.itemRenderOrder = new ArrayList<>();
        this.modifiedChildModels = new LinkedHashMap<>();
    }

    /**
     * 他のModのモデルを参照したいことがあるので、ExistingFileHelperによる検証は行わない
     * ResourceLocationによる参照なので、モデルファイルであればどんなものでも指定可
     */
    public CompositeModelAdvancedBuilder<T> existingChild(String name, ResourceLocation childLoc) {
        existingChildModels.put(name, childLoc);
        return this;
    }

    public ModifiedExistingChildModel<T> modifiedExistingChildStart(String name, ResourceLocation childLoc) {
        return new ModifiedExistingChildModel<>(name, this, childLoc);
    }

    public CompositeModelAdvancedBuilder<T> block() {
        isBlockModel = true;
        return this;
    }

    /** 返り値のクラスを差し替え */
    @Override
    public CompositeModelAdvancedBuilder<T> child(String name, T modelBuilder) {
        super.child(name, modelBuilder);
        return this;
    }

    @Override
    public CompositeModelAdvancedBuilder<T> itemRenderOrder(String... names) {
        this.itemRenderOrder.clear();
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
            children.add(name, existingChildJson(loc));
        });
        modifiedChildModels.forEach((name, child) -> {
            children.add(name, child.toJson());
        });
        result.add(CHILDREN, children);

        JsonArray itemRenderOrder = new JsonArray();

        for (String name : this.itemRenderOrder) {
            itemRenderOrder.add(name);
        }

        result.add("item_render_order", itemRenderOrder);

        if (isBlockModel) {
            result.addProperty("parent", "block/block");
        }
        return result;
    }

    private static JsonObject existingChildJson(ResourceLocation childLoc) {
        JsonObject json = new JsonObject();
        json.addProperty("parent", childLoc.toString());
        return json;
    }

    public static class ModifiedExistingChildModel<T extends ModelBuilder<T>> {

        private final CompositeModelAdvancedBuilder<T> composite;
        private final ResourceLocation parentLoc;
        private final RootTransforms<T> rootTransforms;

        private ModifiedExistingChildModel(String name, CompositeModelAdvancedBuilder<T> composite,
                ResourceLocation parentLoc) {
            this.composite = composite;
            this.composite.modifiedChildModels.put(name, this);
            this.parentLoc = parentLoc;
            this.rootTransforms = new RootTransforms<>(this);
        }

        public CompositeModelAdvancedBuilder<T> backToCompositeBuilder() {
            return composite;
        }

        public  RootTransforms<T> rootTransforms(){
            return this.rootTransforms;
        }

        private JsonObject toJson() {
            JsonObject result = new JsonObject();
            result.addProperty("parent", parentLoc.toString());

            JsonObject transform = this.rootTransforms.toJson();
            if (transform.size() > 0) {
                result.add("transform", transform);
            }
            return result;
        }
    }

    public static class RootTransforms<T extends ModelBuilder<T>> {
        private static final Vector3f ONE = new Vector3f(1.0F, 1.0F, 1.0F);
        private final ModifiedExistingChildModel<T> self;
        private Vector3f translation = new Vector3f();
        private Quaternionf leftRotation = new Quaternionf();
        private Quaternionf rightRotation = new Quaternionf();
        private Vector3f scale;
        private TransformationHelper.@Nullable TransformOrigin origin;
        private @Nullable Vector3f originVec;

        RootTransforms(ModifiedExistingChildModel<T> self) {
            this.scale = ONE;
            this.self = self;
        }

        public ModifiedExistingChildModel<T> backToModifiedChildBuilder() {
            return self;
        }

        public RootTransforms<T> translation(Vector3f translation) {
            this.translation = (Vector3f) Preconditions.checkNotNull(translation, "Translation must not be null");
            return this;
        }

        public RootTransforms<T> translation(float x, float y, float z) {
            return this.translation(new Vector3f(x, y, z));
        }

        public RootTransforms<T> rotation(Quaternionf rotation) {
            this.leftRotation = (Quaternionf) Preconditions.checkNotNull(rotation, "Rotation must not be null");
            return this;
        }

        public RootTransforms<T> rotation(float x, float y, float z, boolean isDegrees) {
            return this.rotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        public RootTransforms<T> leftRotation(Quaternionf leftRotation) {
            return this.rotation(leftRotation);
        }

        public RootTransforms<T> leftRotation(float x, float y, float z, boolean isDegrees) {
            return this.leftRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        public RootTransforms<T> rightRotation(Quaternionf rightRotation) {
            this.rightRotation = (Quaternionf) Preconditions.checkNotNull(rightRotation, "Rotation must not be null");
            return this;
        }

        public RootTransforms<T> rightRotation(float x, float y, float z, boolean isDegrees) {
            return this.rightRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        public RootTransforms<T> postRotation(Quaternionf postRotation) {
            return this.rightRotation(postRotation);
        }

        public RootTransforms<T> postRotation(float x, float y, float z, boolean isDegrees) {
            return this.postRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
        }

        public RootTransforms<T> scale(float scale) {
            return this.scale(new Vector3f(scale, scale, scale));
        }

        public RootTransforms<T> scale(float xScale, float yScale, float zScale) {
            return this.scale(new Vector3f(xScale, yScale, zScale));
        }

        public RootTransforms<T> scale(Vector3f scale) {
            this.scale = (Vector3f) Preconditions.checkNotNull(scale, "Scale must not be null");
            return this;
        }

        public RootTransforms<T> transform(Transformation transformation) {
            Preconditions.checkNotNull(transformation, "Transformation must not be null");
            this.translation = transformation.getTranslation();
            this.leftRotation = transformation.getLeftRotation();
            this.rightRotation = transformation.getRightRotation();
            this.scale = transformation.getScale();
            return this;
        }

        public RootTransforms<T> origin(Vector3f origin) {
            this.originVec = (Vector3f) Preconditions.checkNotNull(origin, "Origin must not be null");
            this.origin = null;
            return this;
        }

        public RootTransforms<T> origin(TransformationHelper.TransformOrigin origin) {
            this.origin = (TransformationHelper.TransformOrigin) Preconditions.checkNotNull(origin,
                    "Origin must not be null");
            this.originVec = null;
            return this;
        }

        public JsonObject toJson() {
            JsonObject transform = new JsonObject();
            if (!this.translation.equals(0.0F, 0.0F, 0.0F)) {
                transform.add("translation", writeVec3(this.translation));
            }

            if (!this.scale.equals(ONE)) {
                transform.add("scale", writeVec3(this.scale));
            }

            if (!this.leftRotation.equals(0.0F, 0.0F, 0.0F, 1.0F)) {
                transform.add("rotation", writeQuaternion(this.leftRotation));
            }

            if (!this.rightRotation.equals(0.0F, 0.0F, 0.0F, 1.0F)) {
                transform.add("post_rotation", writeQuaternion(this.rightRotation));
            }

            if (this.origin != null) {
                transform.addProperty("origin", this.origin.getSerializedName());
            } else if (this.originVec != null && !this.originVec.equals(0.0F, 0.0F, 0.0F)) {
                transform.add("origin", writeVec3(this.originVec));
            }

            return transform;
        }

        private static JsonArray writeVec3(Vector3f vector) {
            JsonArray array = new JsonArray();
            array.add(vector.x());
            array.add(vector.y());
            array.add(vector.z());
            return array;
        }

        private static JsonArray writeQuaternion(Quaternionf quaternion) {
            JsonArray array = new JsonArray();
            array.add(quaternion.x());
            array.add(quaternion.y());
            array.add(quaternion.z());
            array.add(quaternion.w());
            return array;
        }
    }
}
