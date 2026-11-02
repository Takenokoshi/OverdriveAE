package com.takenokoshi.overdriveae.main.recipe.recipecache;

import java.util.List;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public interface IRecipeCache<R extends Recipe<?>> {
    void initCaches(List<RecipeHolder<R>> recipeHolders);

    void clear();
}
