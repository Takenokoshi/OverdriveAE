package com.takenokoshi.overdriveae.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.takenokoshi.overdriveae.recipe.cache.ChargerRecipeCache;
import com.takenokoshi.overdriveae.recipe.cache.CookingRecipeCache;
import com.takenokoshi.overdriveae.recipe.cache.IRecipeCache;

import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.ChargerRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;

public class RecipeCacheHolder<I extends RecipeInput, R extends Recipe<I>, C extends IRecipeCache<R>> {

    private static final List<RecipeCacheHolder<?, ?, ?>> HOLDERS = new ArrayList<>();

    public final RecipeType<R> recipeType;
    public final C recipeCache;
    private boolean isCached;

    public RecipeCacheHolder(RecipeType<R> recipeType, Function<RecipeType<R>, C> recipeCacheCreator) {
        this.recipeType = recipeType;
        this.recipeCache = recipeCacheCreator.apply(this.recipeType);
        HOLDERS.add(this);
    }

    public RecipeCacheHolder(RecipeType<R> recipeType, C recipeCache) {
        this.recipeType = recipeType;
        this.recipeCache = recipeCache;
        HOLDERS.add(this);
    }

    public static void clearAllCaches() {
        HOLDERS.forEach(RecipeCacheHolder::clearCache);
    }

    private void clearCache() {
        this.recipeCache.clear();
        this.isCached = false;
    }

    public void initCacheIfNeeded(Level level) {
        if (!this.isCached) {
            this.isCached = true;
            this.recipeCache.initCaches(level.getRecipeManager().getAllRecipesFor(this.recipeType));
        }
    }

    public boolean isCached() {
        return this.isCached;
    }

    public static final RecipeCacheHolder<SingleRecipeInput, SmeltingRecipe, CookingRecipeCache<SmeltingRecipe>> SMELTING = new RecipeCacheHolder<>(
            RecipeType.SMELTING, new CookingRecipeCache<>());

    public static final RecipeCacheHolder<SingleRecipeInput, BlastingRecipe, CookingRecipeCache<BlastingRecipe>> BLASTING = new RecipeCacheHolder<>(
            RecipeType.BLASTING, new CookingRecipeCache<>());

    public static final RecipeCacheHolder<SingleRecipeInput, SmokingRecipe, CookingRecipeCache<SmokingRecipe>> SMOKING = new RecipeCacheHolder<>(
            RecipeType.SMOKING, new CookingRecipeCache<>());

    public static final RecipeCacheHolder<RecipeInput, ChargerRecipe, ChargerRecipeCache> CHARGER = new RecipeCacheHolder<>(
            AERecipeTypes.CHARGER, new ChargerRecipeCache());
}
