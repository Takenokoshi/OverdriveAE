package com.takenokoshi.overdriveae.recipe.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public class CookingRecipeCache<R extends AbstractCookingRecipe> implements IRecipeCache<R> {

    private final Map<Item, R> cacheMap = new HashMap<>();

    @Override
    public void initCaches(List<RecipeHolder<R>> recipeHolders) {
        for (RecipeHolder<R> recipeHolder : recipeHolders) {
            R recipe = recipeHolder.value();
            for (ItemStack item : recipe.getIngredients().get(0).getItems()) {
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

    public R getRecipe(ItemStack input) {
        return cacheMap.get(input.getItem());
    }

}
