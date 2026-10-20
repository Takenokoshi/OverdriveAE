package com.takenokoshi.overdriveae.recipe.lookup;

import com.takenokoshi.overdriveae.recipe.RecipeCacheHolder;
import com.takenokoshi.overdriveae.recipe.cache.CookingRecipeCache;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.Level;

public interface CookingRecipeCacheLookupHandler<R extends AbstractCookingRecipe> {

    Level getLevel();

    RecipeCacheHolder<?, R, CookingRecipeCache<R>> getCacheHolder();

    default boolean hasRecipe(ItemStack input) {
        if (input.isEmpty()) {
            return false;
        }
        getCacheHolder().initCacheIfNeeded(getLevel());
        return getCacheHolder().recipeCache.hasRecipe(input);
    }

    default R getRecipe(ItemStack input) {
        if (input.isEmpty()) {
            return null;
        }
        getCacheHolder().initCacheIfNeeded(getLevel());
        return getCacheHolder().recipeCache.getRecipe(input);
    }
}
