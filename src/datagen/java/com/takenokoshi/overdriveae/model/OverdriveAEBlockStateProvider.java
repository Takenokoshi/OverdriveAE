package com.takenokoshi.overdriveae.model;

import com.google.gson.JsonObject;
import com.takenokoshi.overdriveae.OverdriveAE;
import com.takenokoshi.overdriveae.registries.OverdriveAEMachines;

import appeng.api.orientation.BlockOrientation;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class OverdriveAEBlockStateProvider extends BlockStateProvider {

    public static final ExistingFileHelper.ResourceType TEXTURE = new ExistingFileHelper.ResourceType(
            PackType.CLIENT_RESOURCES, ".png", "textures");
    protected static final ExistingFileHelper.ResourceType MODEL = new ExistingFileHelper.ResourceType(
            PackType.CLIENT_RESOURCES, ".json", "models");

    public OverdriveAEBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, OverdriveAE.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        orientedBlock(OverdriveAEMachines.OVERDRIVE_CHARGER.getBlock(),
                models().getBuilder("block/overdrive_charger")
                        .texture("particle", modLoc("block/overdrive_frame"))
                        .customLoader(CompositeModelAdvancedBuilder::begin)
                        .existingChild("base", modLoc("block/overdrive_base"))
                        .modifiedExistingChildStart("charger", OverdriveAE.makeInsaneId("block/improved_charger"))
                        .rootTransforms().scale(0.5F).translation(-0.25F, -0.25F, -0.25F).backToModifiedChildBuilder()
                        .backToCompositeBuilder()
                        .itemRenderOrder("charger", "base")
                        .block()
                        .end());
        orientedBlock(OverdriveAEMachines.OVERDRIVE_ENERGIZED_BLAST_FURNACE.getBlock(),
                models().getBuilder("block/overdrive_energized_blast_furnace")
                        .texture("particle", modLoc("block/overdrive_frame"))
                        .customLoader(CompositeModelAdvancedBuilder::begin)
                        .existingChild("base", modLoc("block/overdrive_base"))
                        .modifiedExistingChildStart("blast_furnace", mcLoc("block/blast_furnace"))
                        .rootTransforms().scale(0.5F).translation(-0.25F, -0.25F, -0.25F).backToModifiedChildBuilder()
                        .backToCompositeBuilder()
                        .itemRenderOrder("blast_furnace", "base")
                        .block()
                        .end());
        orientedBlock(OverdriveAEMachines.OVERDRIVE_ENERGIZED_FURNACE.getBlock(),
                models().getBuilder("block/overdrive_energized_furnace")
                        .texture("particle", modLoc("block/overdrive_frame"))
                        .customLoader(CompositeModelAdvancedBuilder::begin)
                        .existingChild("base", modLoc("block/overdrive_base"))
                        .modifiedExistingChildStart("furnace", mcLoc("block/furnace"))
                        .rootTransforms().scale(0.5F).translation(-0.25F, -0.25F, -0.25F).backToModifiedChildBuilder()
                        .backToCompositeBuilder()
                        .itemRenderOrder("furnace", "base")
                        .block()
                        .end());
        orientedBlock(OverdriveAEMachines.OVERDRIVE_ENERGIZED_SMOKER.getBlock(),
                models().getBuilder("block/overdrive_energized_smoker")
                        .texture("particle", modLoc("block/overdrive_frame"))
                        .customLoader(CompositeModelAdvancedBuilder::begin)
                        .existingChild("base", modLoc("block/overdrive_base"))
                        .modifiedExistingChildStart("smoker", mcLoc("block/smoker"))
                        .rootTransforms().scale(0.5F).translation(-0.25F, -0.25F, -0.25F).backToModifiedChildBuilder()
                        .backToCompositeBuilder()
                        .itemRenderOrder("smoker", "base")
                        .block()
                        .end());
    }

    /**
     * 向き付きブロック 1 個ぶんのブロックステートを出す
     * ({@code facing} 6 方向 × {@code spin} 4 回転 = 24 通り)。
     *
     * <p>
     * AE2 の {@code OrientationStrategies.full()} を使うブロック用。
     * 角度は {@link BlockOrientation} から引くので、AE2 のチャージャーなどと
     * <b>同じ向き付けになる</b>ことが保証される。
     * </p>
     *
     * <p>
     * <b>{@code getVariantBuilder} が使えない理由</b>: 24 通りのうち半分は
     * <b>Z 軸回転</b>を要求するが、バニラのブロックステートは x / y しか持たない。
     * AE2 は {@code ae2:z} という独自キーを足し、その解釈を Mixin
     * ({@code VariantDeserializerMixin}) でバニラのパーサに入れている。
     * これは名前空間に関係なく効くのでこちらの JSON でも使えるが、
     * NeoForge の {@code ConfiguredModel} には z を書く口が無いので、
     * ここだけ JSON を直接組んで {@code registeredBlocks} に入れている。
     * </p>
     */
    private void orientedBlock(Block block, ModelFile model) {
        JsonObject variants = new JsonObject();
        for (Direction facing : Direction.values()) {
            for (int spin = 0; spin < 4; spin++) {
                BlockOrientation orientation = BlockOrientation.get(facing, spin);
                JsonObject variant = new JsonObject();
                variant.addProperty("model", model.getLocation().toString());
                putAngle(variant, "x", orientation.getAngleX());
                putAngle(variant, "y", orientation.getAngleY());
                putAngle(variant, "ae2:z", orientation.getAngleZ());
                variants.add("facing=" + facing.getSerializedName() + ",spin=" + spin, variant);
            }
        }

        JsonObject blockState = new JsonObject();
        blockState.add("variants", variants);
        registeredBlocks.put(block, () -> blockState);
    }

    /** 0 度は書かない (AE2 が出す JSON と同じ形にするため)。 */
    private static void putAngle(JsonObject variant, String key, int angle) {
        int normalized = Math.floorMod(angle, 360);
        if (normalized != 0) {
            variant.addProperty(key, normalized);
        }
    }

}
