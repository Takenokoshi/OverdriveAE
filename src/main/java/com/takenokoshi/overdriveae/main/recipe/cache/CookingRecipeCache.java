package com.takenokoshi.overdriveae.main.recipe.cache;

import java.util.List;
import com.takenokoshi.overdriveae.main.recipe.map.ItemCacheMap;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CookingRecipeCache<R extends AbstractCookingRecipe> implements IRecipeCache<R> {

    private final ItemCacheMap<R> cacheMap = new ItemCacheMap<>();

    @Override
    public void initCaches(List<RecipeHolder<R>> recipeHolders) {
        for (RecipeHolder<R> recipeHolder : recipeHolders) {
            R recipe = recipeHolder.value();
            cacheMap.putRecipe(recipe.getIngredients().get(0), recipe);
        }
    }

    @Override
    public void clear() {
        cacheMap.clear();
    }

    public boolean hasRecipe(ItemStack input) {
        return cacheMap.hasRecipe(input);
    }

    public R getRecipe(ItemStack input) {
        return cacheMap.getRecipe(input);
    }

}
