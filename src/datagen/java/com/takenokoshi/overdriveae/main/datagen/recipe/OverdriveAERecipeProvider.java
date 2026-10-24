package com.takenokoshi.overdriveae.main.datagen.recipe;

import java.util.concurrent.CompletableFuture;

import com.takenokoshi.overdriveae.main.OverdriveAE;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEItems;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEMachines;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipeBuilder;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipeBuilder;
import jp.main.taikun.insaneae.registries.ModItems;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

public class OverdriveAERecipeProvider extends RecipeProvider {

    public OverdriveAERecipeProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        TransformRecipeBuilder.transform(recipeOutput, OverdriveAE.makeId("overdrive_crystal"),
                OverdriveAEItems.OVERDRIVE_CRYSTAL, 2, TransformCircumstance.fluid(Tags.Fluids.WATER),
                new ItemLike[] {
                        AEItems.CERTUS_QUARTZ_CRYSTAL_CHARGED,
                        Items.GLOWSTONE_DUST,
                        Items.AMETHYST_SHARD,
                });

        InscriberRecipeBuilder
                .inscribe(OverdriveAEItems.OVERDRIVE_CRYSTAL, OverdriveAEItems.PRINTED_OVERDRIVE_PROCESSOR, 1)
                .setMode(InscriberProcessType.INSCRIBE)
                .setTop(Ingredient.of(new ItemLike[] {
                        ModItems.INSANE_PROCESSOR_PRESS.get(),
                }))
                .save(recipeOutput, OverdriveAE.makeId("printed_overdrive_processor"));
        InscriberRecipeBuilder
                .inscribe(Tags.Items.DUSTS_REDSTONE, OverdriveAEItems.OVERDRIVE_PROCESSOR, 1)
                .setMode(InscriberProcessType.PRESS)
                .setTop(Ingredient.of(new ItemLike[] {
                        AEItems.SILICON_PRINT,
                }))
                .setBottom(Ingredient.of(new ItemLike[] {
                        OverdriveAEItems.PRINTED_OVERDRIVE_PROCESSOR,
                }))
                .save(recipeOutput, OverdriveAE.makeId("overdrive_processor"));

        ShapedRecipeBuilder
                .shaped(RecipeCategory.REDSTONE, new ItemStack(OverdriveAEMachines.OVERDRIVE_CHARGER, 1))
                .define('O', OverdriveAEItems.OVERDRIVE_PROCESSOR)
                .define('C', BuiltInRegistries.ITEM.get(OverdriveAE.makeInsaneId("improved_charger")))
                .define('P', AEBlocks.PATTERN_PROVIDER)
                .pattern("COC")
                .pattern("OPO")
                .pattern("COC")
                .unlockedBy("unlock", has(OverdriveAEItems.OVERDRIVE_PROCESSOR.asItem()))
                .save(recipeOutput);
        ShapedRecipeBuilder
                .shaped(RecipeCategory.REDSTONE, new ItemStack(OverdriveAEMachines.OVERDRIVE_INSCRIBER, 1))
                .define('O', OverdriveAEItems.OVERDRIVE_PROCESSOR)
                .define('C', AEBlocks.INSCRIBER)
                .define('P', AEBlocks.PATTERN_PROVIDER)
                .pattern("COC")
                .pattern("OPO")
                .pattern("COC")
                .unlockedBy("unlock", has(OverdriveAEItems.OVERDRIVE_PROCESSOR.asItem()))
                .save(recipeOutput);
        ShapedRecipeBuilder
                .shaped(RecipeCategory.REDSTONE, new ItemStack(OverdriveAEMachines.OVERDRIVE_MATTER_CONDENSER, 1))
                .define('O', OverdriveAEItems.OVERDRIVE_PROCESSOR)
                .define('C', AEBlocks.CONDENSER)
                .define('P', AEBlocks.PATTERN_PROVIDER)
                .define('S', AEItems.CELL_COMPONENT_256K)
                .pattern("CSC")
                .pattern("OPO")
                .pattern("COC")
                .unlockedBy("unlock", has(OverdriveAEItems.OVERDRIVE_PROCESSOR.asItem()))
                .save(recipeOutput);
        ShapedRecipeBuilder
                .shaped(RecipeCategory.REDSTONE, new ItemStack(OverdriveAEMachines.OVERDRIVE_TRANSFORM_CHAMBER, 1))
                .define('O', OverdriveAEItems.OVERDRIVE_PROCESSOR)
                .define('C', AEBlocks.SKY_STONE_TANK)
                .define('P', AEBlocks.PATTERN_PROVIDER)
                .pattern("COC")
                .pattern("OPO")
                .pattern("COC")
                .unlockedBy("unlock", has(OverdriveAEItems.OVERDRIVE_PROCESSOR.asItem()))
                .save(recipeOutput);
        ShapedRecipeBuilder
                .shaped(RecipeCategory.REDSTONE, new ItemStack(OverdriveAEMachines.OVERDRIVE_ENERGIZED_FURNACE, 1))
                .define('O', OverdriveAEItems.OVERDRIVE_PROCESSOR)
                .define('C', Items.FURNACE)
                .define('P', AEBlocks.PATTERN_PROVIDER)
                .pattern("COC")
                .pattern("OPO")
                .pattern("COC")
                .unlockedBy("unlock", has(OverdriveAEItems.OVERDRIVE_PROCESSOR.asItem()))
                .save(recipeOutput);
        ShapedRecipeBuilder
                .shaped(RecipeCategory.REDSTONE, new ItemStack(OverdriveAEMachines.OVERDRIVE_ENERGIZED_BLAST_FURNACE, 1))
                .define('O', OverdriveAEItems.OVERDRIVE_PROCESSOR)
                .define('C', Items.BLAST_FURNACE)
                .define('P', AEBlocks.PATTERN_PROVIDER)
                .pattern("COC")
                .pattern("OPO")
                .pattern("COC")
                .unlockedBy("unlock", has(OverdriveAEItems.OVERDRIVE_PROCESSOR.asItem()))
                .save(recipeOutput);
        ShapedRecipeBuilder
                .shaped(RecipeCategory.REDSTONE, new ItemStack(OverdriveAEMachines.OVERDRIVE_ENERGIZED_SMOKER, 1))
                .define('O', OverdriveAEItems.OVERDRIVE_PROCESSOR)
                .define('C', Items.SMOKER)
                .define('P', AEBlocks.PATTERN_PROVIDER)
                .pattern("COC")
                .pattern("OPO")
                .pattern("COC")
                .unlockedBy("unlock", has(OverdriveAEItems.OVERDRIVE_PROCESSOR.asItem()))
                .save(recipeOutput);
    }

}
