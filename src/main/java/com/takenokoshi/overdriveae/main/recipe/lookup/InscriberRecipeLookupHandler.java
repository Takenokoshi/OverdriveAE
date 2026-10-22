package com.takenokoshi.overdriveae.main.recipe.lookup;

import java.util.Set;

import com.takenokoshi.overdriveae.main.recipe.RecipeCacheHolder;

import appeng.blockentity.misc.InscriberRecipes;
import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public interface InscriberRecipeLookupHandler {

    Level getLevel();

    Set<ItemStack> getAvailableCatalysts();

    default boolean hasRecipeTriple(ItemStack topInput, ItemStack centerInput, ItemStack bottomInput) {
        if (topInput.isEmpty() || centerInput.isEmpty() || bottomInput.isEmpty()) {
            return false;
        }
        /** 名前つけるやつ */
        if (AEItems.NAME_PRESS.is(topInput) && AEItems.NAME_PRESS.is(bottomInput)) {
            return true;
        }
        RecipeCacheHolder.INSCRIBER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.INSCRIBER.recipeCache.hasRecipeTriple(topInput, centerInput, bottomInput);
    }

    default InscriberRecipe getRecipeTriple(ItemStack topInput, ItemStack centerInput, ItemStack bottomInput) {
        if (topInput.isEmpty() || centerInput.isEmpty() || bottomInput.isEmpty()) {
            return null;
        }
        /** 名前つけるやつ */
        if (AEItems.NAME_PRESS.is(topInput) && AEItems.NAME_PRESS.is(bottomInput)) {
            return InscriberRecipes.findRecipe(getLevel(), centerInput, topInput, bottomInput, true);
        }
        RecipeCacheHolder.INSCRIBER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.INSCRIBER.recipeCache.getRecipeTriple(topInput, centerInput, bottomInput);
    }

    default boolean hasRecipeDouble(ItemStack firstInput, ItemStack secondInput) {
        if (firstInput.isEmpty() || secondInput.isEmpty()) {
            return false;
        }
        /** 名前つけるやつ */
        if (AEItems.NAME_PRESS.is(firstInput) || AEItems.NAME_PRESS.is(secondInput)) {
            return true;
        }
        RecipeCacheHolder.INSCRIBER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.INSCRIBER.recipeCache.hasRecipeDouble(firstInput, secondInput);
    }

    default InscriberRecipe getRecipeDouble(ItemStack firstInput, ItemStack secondInput) {
        if (firstInput.isEmpty() || secondInput.isEmpty()) {
            return null;
        }
        /** 名前つけるやつ */
        if (AEItems.NAME_PRESS.is(firstInput)) {
            return InscriberRecipes.findRecipe(getLevel(), secondInput, firstInput, ItemStack.EMPTY, true);
        } else if (AEItems.NAME_PRESS.is(secondInput)) {
            return InscriberRecipes.findRecipe(getLevel(), firstInput, secondInput, ItemStack.EMPTY, true);
        }
        RecipeCacheHolder.INSCRIBER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.INSCRIBER.recipeCache.getRecipeDouble(firstInput, secondInput);
    }

    default boolean hasRecipeSingle(ItemStack centerInput) {
        if (centerInput.isEmpty()) {
            return false;
        }
        RecipeCacheHolder.INSCRIBER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.INSCRIBER.recipeCache.hasRecipeSingle(centerInput);
    }

    default InscriberRecipe getRecipeSingle(ItemStack centerInput) {
        if (centerInput.isEmpty()) {
            return null;
        }
        RecipeCacheHolder.INSCRIBER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.INSCRIBER.recipeCache.getRecipeSingle(centerInput);
    }

    default boolean isRecipeProcessable(InscriberRecipe recipe) {
        if (recipe.getProcessType() == InscriberProcessType.PRESS) {
            return true;
        }

        Ingredient top = recipe.getTopOptional();
        Ingredient bottom = recipe.getBottomOptional();

        if (top.isEmpty() && bottom.isEmpty()) {
            return true;
        }

        Set<ItemStack> catalysts = getAvailableCatalysts();

        if (top.isEmpty()) {
            return catalysts.stream().anyMatch(bottom);
        }

        if (bottom.isEmpty()) {
            return catalysts.stream().anyMatch(top);
        }

        return catalysts.stream().anyMatch(bottom)
                && catalysts.stream().anyMatch(top);
    }

    default boolean isCatalystItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        RecipeCacheHolder.INSCRIBER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.INSCRIBER.recipeCache.isCatalystItem(stack);
    }
}
