package com.takenokoshi.overdriveae.main.recipe.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import appeng.recipes.handlers.ChargerRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ChargerRecipeCache implements IRecipeCache<ChargerRecipe> {

    private final Map<Item, ChargerRecipe> cacheMap = new HashMap<>();

    @Override
    public void initCaches(List<RecipeHolder<ChargerRecipe>> recipeHolders) {
        for (RecipeHolder<ChargerRecipe> recipeHolder : recipeHolders) {
            ChargerRecipe recipe = recipeHolder.value();
            for (ItemStack item : recipe.ingredient.getItems()) {
                cacheMap.put(item.getItem(), recipe);
            }
        }
    }

    @Override
    public void clear() {
        cacheMap.clear();
    }

    public boolean hasRecipe(ItemStack input) {
        return cacheMap.containsKey(input.getItem());
    }

    public ChargerRecipe getRecipe(ItemStack input) {
        return cacheMap.get(input.getItem());
    }

}
