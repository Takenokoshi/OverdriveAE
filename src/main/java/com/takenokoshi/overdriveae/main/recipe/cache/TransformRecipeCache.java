package com.takenokoshi.overdriveae.main.recipe.cache;

import java.util.List;
import com.takenokoshi.overdriveae.main.recipe.map.ItemCacheMap;

import appeng.recipes.transform.TransformRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class TransformRecipeCache implements IRecipeCache<TransformRecipe> {

    private final ItemCacheMap<TransformRecipe> cacheMap = new ItemCacheMap<>();

    @Override
    public void initCaches(List<RecipeHolder<TransformRecipe>> recipeHolders) {
        for (RecipeHolder<TransformRecipe> recipeHolder : recipeHolders) {
            TransformRecipe recipe = recipeHolder.value();
            if (!recipe.circumstance.isFluid()) {
                /** 液体以外の加工手段はサポートしない */
                continue;
            }
        }
    }

    @Override
    public void clear() {
        cacheMap.clear();
    }

}
