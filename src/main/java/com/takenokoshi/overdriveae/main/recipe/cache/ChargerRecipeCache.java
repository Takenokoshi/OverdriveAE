package com.takenokoshi.overdriveae.main.recipe.cache;

import java.util.List;
import com.takenokoshi.overdriveae.main.recipe.map.ItemCacheMap;

import appeng.recipes.handlers.ChargerRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ChargerRecipeCache implements IRecipeCache<ChargerRecipe> {

    private final ItemCacheMap<ChargerRecipe> cacheMap = new ItemCacheMap<>();

    @Override
    public void initCaches(List<RecipeHolder<ChargerRecipe>> recipeHolders) {
        for (RecipeHolder<ChargerRecipe> recipeHolder : recipeHolders) {
            ChargerRecipe recipe = recipeHolder.value();
            cacheMap.putRecipe(recipe.ingredient, recipe);
        }
    }

    @Override
    public void clear() {
        cacheMap.clear();
    }

    public boolean hasRecipe(ItemStack input) {
        return cacheMap.hasRecipe(input);
    }

    public ChargerRecipe getRecipe(ItemStack input) {
        return cacheMap.getRecipe(input);
    }

}
