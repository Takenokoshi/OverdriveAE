package com.takenokoshi.overdriveae.main.recipe.map;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

public class FluidCacheMap<RECIPE> extends HashMap<Fluid, Set<PredicateRecipeCache<FluidStack, RECIPE>>> {
    public void putRecipe(FluidIngredient ingredient, RECIPE value) {
        PredicateRecipeCache<FluidStack, RECIPE> cache = new PredicateRecipeCache<>(ingredient, value);
        for (FluidStack stack : ingredient.getStacks()) {
            Set<PredicateRecipeCache<FluidStack, RECIPE>> set = computeIfAbsent(stack.getFluid(),
                    v -> new LinkedHashSet<>());
            set.add(cache);
        }
    }

    public boolean hasRecipe(FluidStack stack) {
        Fluid fluid = stack.getFluid();
        Set<PredicateRecipeCache<FluidStack, RECIPE>> set = get(fluid);
        if (set == null) {
            return false;
        }
        return set.stream().anyMatch(v -> v.test(stack));
    }

    public boolean hasRecipe(FluidStack stack, Predicate<RECIPE> predicate) {
        Fluid fluid = stack.getFluid();
        Set<PredicateRecipeCache<FluidStack, RECIPE>> set = get(fluid);
        if (set == null) {
            return false;
        }
        return set.stream().anyMatch(v -> v.test(stack) && predicate.test(v.value()));
    }

    public boolean hasRecipe(FluidStack stack, BiPredicate<RECIPE, FluidStack> biPredicate) {
        Fluid fluid = stack.getFluid();
        Set<PredicateRecipeCache<FluidStack, RECIPE>> set = get(fluid);
        if (set == null) {
            return false;
        }
        return set.stream().anyMatch(v -> v.test(stack) && biPredicate.test(v.value(), stack));
    }

    public RECIPE getRecipe(FluidStack stack) {
        Fluid fluid = stack.getFluid();
        Set<PredicateRecipeCache<FluidStack, RECIPE>> set = get(fluid);
        if (set == null) {
            return null;
        }
        /** 一つのレシピしか要らない場合、先にfindFirst()で対象を減らしてからmapで変換する */
        return set.stream().filter(v -> v.test(stack)).findFirst().map(PredicateRecipeCache::value).orElse(null);
    }

    public Stream<RECIPE> getRecipeStream(FluidStack stack) {
        Fluid fluid = stack.getFluid();
        Set<PredicateRecipeCache<FluidStack, RECIPE>> set = get(fluid);
        if (set == null) {
            return Stream.empty();
        }
        return set.stream().filter(v -> v.test(stack)).map(PredicateRecipeCache::value);
    }

    public RECIPE getRecipe(FluidStack stack, Predicate<RECIPE> predicate) {
        return getRecipeStream(stack).filter(predicate).findFirst().orElse(null);
    }

    public RECIPE getRecipe(FluidStack stack, BiPredicate<RECIPE, FluidStack> biPredicate) {
        return getRecipeStream(stack).filter(v -> biPredicate.test(v, stack)).findFirst().orElse(null);
    }
}
