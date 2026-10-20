package com.takenokoshi.overdriveae.recipe.lookup;

import java.util.List;

import com.takenokoshi.overdriveae.recipe.RecipeCacheHolder;

import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface InscriberRecipeLookupHandler {

    Level getLevel();

    List<ItemStack> getAvailableCatalysts();

    default boolean hasRecipeTriple(ItemStack topInput, ItemStack centerInput, ItemStack bottomInput) {
        if (topInput.isEmpty() || centerInput.isEmpty() || bottomInput.isEmpty()) {
            return false;
        }
        RecipeCacheHolder.INSCRIBER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.INSCRIBER.recipeCache.hasRecipeTriple(topInput, centerInput, bottomInput);
    }

    default InscriberRecipe getRecipeTriple(ItemStack topInput, ItemStack centerInput, ItemStack bottomInput) {
        if (topInput.isEmpty() || centerInput.isEmpty() || bottomInput.isEmpty()) {
            return null;
        }
        RecipeCacheHolder.INSCRIBER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.INSCRIBER.recipeCache.getRecipeTriple(topInput, centerInput, bottomInput);
    }

    default boolean hasRecipeDouble(ItemStack firstInput, ItemStack secondInput) {
        if (firstInput.isEmpty() || secondInput.isEmpty()) {
            return false;
        }
        RecipeCacheHolder.INSCRIBER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.INSCRIBER.recipeCache.hasRecipeDouble(firstInput, secondInput);
    }

    default InscriberRecipe getRecipeDouble(ItemStack firstInput, ItemStack secondInput) {
        if (firstInput.isEmpty() || secondInput.isEmpty()) {
            return null;
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
}
