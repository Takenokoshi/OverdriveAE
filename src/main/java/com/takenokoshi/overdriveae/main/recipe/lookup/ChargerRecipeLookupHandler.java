package com.takenokoshi.overdriveae.main.recipe.lookup;

import com.takenokoshi.overdriveae.main.recipe.RecipeCacheHolder;

import appeng.recipes.handlers.ChargerRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ChargerRecipeLookupHandler {
    Level getLevel();

    default boolean hasRecipe(ItemStack input) {
        if (input.isEmpty()) {
            return false;
        }
        RecipeCacheHolder.CHARGER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.CHARGER.recipeCache.hasRecipe(input);
    }

    default ChargerRecipe getRecipe(ItemStack input) {
        if (input.isEmpty()) {
            return null;
        }
        RecipeCacheHolder.CHARGER.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.CHARGER.recipeCache.getRecipe(input);
    }
}
