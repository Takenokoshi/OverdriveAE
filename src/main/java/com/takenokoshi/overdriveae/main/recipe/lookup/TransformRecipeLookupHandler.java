package com.takenokoshi.overdriveae.main.recipe.lookup;

import java.util.Set;

import com.takenokoshi.overdriveae.main.recipe.RecipeCacheHolder;

import appeng.recipes.transform.TransformRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public interface TransformRecipeLookupHandler {
    Level getLevel();

    Set<Fluid> getAvailableCatalysts();

    default boolean hasRecipe(ItemStack[] stacks, Fluid fluid) {
        if (stacks.length == 0 || fluid == null) {
            return false;
        }
        for (ItemStack itemStack : stacks) {
            if (itemStack == null || itemStack.isEmpty()) {
                return false;
            }
        }
        RecipeCacheHolder.TRANSFORM.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.TRANSFORM.recipeCache.hasRecipe(stacks, fluid);
    }

    default TransformRecipe getRecipe(ItemStack[] stacks, Fluid fluid) {
        if (stacks.length == 0 || fluid == null) {
            return null;
        }
        for (ItemStack itemStack : stacks) {
            if (itemStack == null || itemStack.isEmpty()) {
                return null;
            }
        }
        RecipeCacheHolder.TRANSFORM.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.TRANSFORM.recipeCache.getRecipe(stacks, fluid);
    }

    default boolean isCatalystFluid(Fluid fluid) {
        if (fluid == null) {
            return false;
        }
        RecipeCacheHolder.TRANSFORM.initCacheIfNeeded(getLevel());
        return RecipeCacheHolder.TRANSFORM.recipeCache.isCatalystFluid(fluid);
    }

    default boolean isRecipeProcessable(TransformRecipe recipe) {
        return getAvailableCatalysts().stream().anyMatch(recipe.circumstance::isFluid);
    }
}
