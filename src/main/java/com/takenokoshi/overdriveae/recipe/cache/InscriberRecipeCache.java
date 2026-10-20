package com.takenokoshi.overdriveae.recipe.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/** 処理パターンからの検索は InscriberProcessType に関わらず全アイテムを見る */
public class InscriberRecipeCache implements IRecipeCache<InscriberRecipe> {

    /** キャッシュのキーはレシピ中央のアイテム */

    private final Map<Item, Set<InscriberRecipe>> tripleInputCacheMap = new HashMap<>();
    private final Map<Item, Set<InscriberRecipe>> doubleInputCacheMap = new HashMap<>();
    /** 材料1種類のレシピは一つしか保存しなくていい */
    private final Map<Item, InscriberRecipe> singleInputCacheMap = new HashMap<>();

    @Override
    public void initCaches(List<RecipeHolder<InscriberRecipe>> recipeHolders) {
        for (RecipeHolder<InscriberRecipe> recipeHolder : recipeHolders) {
            InscriberRecipe recipe = recipeHolder.value();
            boolean isBottomEmpty = recipe.getBottomOptional().isEmpty();
            boolean isTopEmpty = recipe.getTopOptional().isEmpty();
            if (isBottomEmpty && isTopEmpty) {
                for (ItemStack item : recipe.getMiddleInput().getItems()) {
                    singleInputCacheMap.put(item.getItem(), recipe);
                }
            } else if (isBottomEmpty || isTopEmpty) {
                for (ItemStack item : recipe.getMiddleInput().getItems()) {
                    Item key = item.getItem();
                    Set<InscriberRecipe> set = doubleInputCacheMap.computeIfAbsent(key, v -> new HashSet<>());
                    set.add(recipe);
                }
            } else {
                for (ItemStack item : recipe.getMiddleInput().getItems()) {
                    Item key = item.getItem();
                    Set<InscriberRecipe> set = tripleInputCacheMap.computeIfAbsent(key, v -> new HashSet<>());
                    set.add(recipe);
                }
            }
        }
    }

    @Override
    public void clear() {
        tripleInputCacheMap.clear();
        doubleInputCacheMap.clear();
        singleInputCacheMap.clear();
    }

    public static boolean topMatch(InscriberRecipe recipe, ItemStack topInput) {
        return recipe.getTopOptional().test(topInput);
    }

    public static boolean bottomMatch(InscriberRecipe recipe, ItemStack bottomInput) {
        return recipe.getBottomOptional().test(bottomInput);
    }

    public boolean hasRecipeTriple(ItemStack topInput, ItemStack centerInput, ItemStack bottomInput) {
        Set<InscriberRecipe> set = tripleInputCacheMap.getOrDefault(centerInput.getItem(), null);
        if (set == null) {
            return false;
        }
        return set.stream().anyMatch(r -> topMatch(r, topInput) && bottomMatch(r, bottomInput));
    }

    public InscriberRecipe getRecipeTriple(ItemStack topInput, ItemStack centerInput, ItemStack bottomInput) {
        Set<InscriberRecipe> set = tripleInputCacheMap.getOrDefault(centerInput.getItem(), null);
        if (set == null) {
            return null;
        }
        return set.stream().filter(r -> topMatch(r, topInput) && bottomMatch(r, bottomInput)).findFirst().orElse(null);
    }

    public boolean hasRecipeDouble(ItemStack firstInput, ItemStack secondInput) {
        return hasRecipeTopMiddle(firstInput, secondInput) || hasRecipeMiddleBottom(firstInput, secondInput);
    }

    public boolean hasRecipeTopMiddle(ItemStack topInput, ItemStack centerInput) {
        Set<InscriberRecipe> set = doubleInputCacheMap.getOrDefault(centerInput.getItem(), null);
        if (set == null) {
            return false;
        }
        return set.stream().anyMatch(r -> topMatch(r, topInput));
    }

    public boolean hasRecipeMiddleBottom(ItemStack centerInput, ItemStack bottomInput) {
        Set<InscriberRecipe> set = doubleInputCacheMap.getOrDefault(centerInput.getItem(), null);
        if (set == null) {
            return false;
        }
        return set.stream().anyMatch(r -> bottomMatch(r, bottomInput));
    }

    public InscriberRecipe getRecipeDouble(ItemStack firstInput, ItemStack secondInput) {
        InscriberRecipe result = getRecipeTopMiddle(firstInput, secondInput);
        return result != null ? result : getRecipeMiddleBottom(firstInput, secondInput);
    }

    public InscriberRecipe getRecipeTopMiddle(ItemStack topInput, ItemStack centerInput) {
        Set<InscriberRecipe> set = tripleInputCacheMap.getOrDefault(centerInput.getItem(), null);
        if (set == null) {
            return null;
        }
        return set.stream().filter(r -> topMatch(r, topInput)).findFirst().orElse(null);
    }

    public InscriberRecipe getRecipeMiddleBottom(ItemStack centerInput, ItemStack bottomInput) {
        Set<InscriberRecipe> set = tripleInputCacheMap.getOrDefault(centerInput.getItem(), null);
        if (set == null) {
            return null;
        }
        return set.stream().filter(r -> bottomMatch(r, bottomInput)).findFirst().orElse(null);
    }

    public boolean hasRecipeSingle(ItemStack centerInput) {
        return singleInputCacheMap.containsKey(centerInput.getItem());
    }

    public InscriberRecipe getRecipeSingle(ItemStack centerInput) {
        return singleInputCacheMap.get(centerInput.getItem());
    }
}
