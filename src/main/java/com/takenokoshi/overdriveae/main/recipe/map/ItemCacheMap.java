package com.takenokoshi.overdriveae.main.recipe.map;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ItemCacheMap<RECIPE> extends HashMap<Item, Set<PredicateRecipeCache<ItemStack, RECIPE>>> {

    public void putRecipe(Ingredient ingredient, RECIPE value) {
        PredicateRecipeCache<ItemStack, RECIPE> cache = new PredicateRecipeCache<>(ingredient, value);
        /**
         * CustomIngredientが自身にとっての全対象を返さない場合は、返してくるもののみ登録
         * 逆に、本来受け付けないものを返してくる場合は、hasRecipe/getRecipe側で検索時にはじく
         */
        for (ItemStack item : ingredient.getItems()) {
            Set<PredicateRecipeCache<ItemStack, RECIPE>> set = computeIfAbsent(item.getItem(),
                    v -> new LinkedHashSet<>());
            set.add(cache);
        }
    }

    public boolean hasRecipe(ItemStack stack) {
        Item item = stack.getItem();
        Set<PredicateRecipeCache<ItemStack, RECIPE>> set = get(item);
        if (set == null) {
            return false;
        }
        return set.stream().anyMatch(v -> v.test(stack));
    }

    public boolean hasRecipe(ItemStack stack, Predicate<RECIPE> predicate) {
        Item item = stack.getItem();
        Set<PredicateRecipeCache<ItemStack, RECIPE>> set = get(item);
        if (set == null) {
            return false;
        }
        return set.stream().anyMatch(v -> v.test(stack) && predicate.test(v.value()));
    }

    public boolean hasRecipe(ItemStack stack, BiPredicate<RECIPE, ItemStack> biPredicate) {
        Item item = stack.getItem();
        Set<PredicateRecipeCache<ItemStack, RECIPE>> set = get(item);
        if (set == null) {
            return false;
        }
        return set.stream().anyMatch(v -> v.test(stack) && biPredicate.test(v.value(), stack));
    }

    public RECIPE getRecipe(ItemStack stack) {
        Item item = stack.getItem();
        Set<PredicateRecipeCache<ItemStack, RECIPE>> set = get(item);
        if (set == null) {
            return null;
        }
        /** 一つのレシピしか要らない場合、先にfindFirst()で対象を減らしてからmapで変換する */
        return set.stream().filter(v -> v.test(stack)).findFirst().map(PredicateRecipeCache::value).orElse(null);
    }

    public Stream<RECIPE> getRecipeStream(ItemStack stack) {
        Item item = stack.getItem();
        Set<PredicateRecipeCache<ItemStack, RECIPE>> set = get(item);
        if (set == null) {
            return null;
        }
        return set.stream().filter(v -> v.test(stack)).map(PredicateRecipeCache::value);
    }

    /**
     * biPredicateに渡すのはPredicateRecipeCache::valueの結果なので先に変換してからフィルターしたほうが計算量は少ない
     */
    public RECIPE getRecipe(ItemStack stack, Predicate<RECIPE> predicate) {
        return getRecipeStream(stack).filter(predicate).findFirst().orElse(null);
    }

    public RECIPE getRecipe(ItemStack stack, BiPredicate<RECIPE, ItemStack> biPredicate) {
        return getRecipeStream(stack).filter(v -> biPredicate.test(v, stack)).findFirst().orElse(null);
    }

}
