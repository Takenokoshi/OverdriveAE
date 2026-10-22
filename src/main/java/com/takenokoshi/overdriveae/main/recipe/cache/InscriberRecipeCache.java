package com.takenokoshi.overdriveae.main.recipe.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.takenokoshi.overdriveae.main.recipe.map.ItemCacheMap;

import appeng.core.definitions.AEItems;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

/** 処理パターンからの検索は InscriberProcessType に関わらず全アイテムを見る */
public class InscriberRecipeCache implements IRecipeCache<InscriberRecipe> {

    /** キャッシュのキーはレシピ中央のアイテム */

    private final ItemCacheMap<InscriberRecipe> tripleInputCacheMap = new ItemCacheMap<>();
    private final ItemCacheMap<InscriberRecipe> topMiddleCacheMap = new ItemCacheMap<>();
    private final ItemCacheMap<InscriberRecipe> middleBottomCacheMap = new ItemCacheMap<>();
    private final ItemCacheMap<InscriberRecipe> singleInputCacheMap = new ItemCacheMap<>();

    /**
     * 触媒アイテムを保存しておく
     * データコンポーネント等を確認しておく必要は無い
     */
    private final Set<Item> catalystCacheSet = new HashSet<>();

    @Override
    public void initCaches(List<RecipeHolder<InscriberRecipe>> recipeHolders) {
        catalystCacheSet.add(AEItems.NAME_PRESS.get());
        for (RecipeHolder<InscriberRecipe> recipeHolder : recipeHolders) {
            InscriberRecipe recipe = recipeHolder.value();
            boolean isBottomEmpty = recipe.getBottomOptional().isEmpty();
            boolean isTopEmpty = recipe.getTopOptional().isEmpty();
            if (isBottomEmpty && isTopEmpty) {
                singleInputCacheMap.putRecipe(recipe.getMiddleInput(), recipe);
            } else if (isBottomEmpty) {
                topMiddleCacheMap.putRecipe(recipe.getMiddleInput(), recipe);
            } else if (isTopEmpty) {
                middleBottomCacheMap.putRecipe(recipe.getMiddleInput(), recipe);
            } else {
                tripleInputCacheMap.putRecipe(recipe.getMiddleInput(), recipe);
            }
            if (recipe.getProcessType() == InscriberProcessType.INSCRIBE) {
                for (ItemStack item : recipe.getTopOptional().getItems()) {
                    catalystCacheSet.add(item.getItem());
                }
                for (ItemStack item : recipe.getBottomOptional().getItems()) {
                    catalystCacheSet.add(item.getItem());
                }
            }
        }
    }

    @Override
    public void clear() {
        tripleInputCacheMap.clear();
        topMiddleCacheMap.clear();
        middleBottomCacheMap.clear();
        singleInputCacheMap.clear();
        catalystCacheSet.clear();
    }

    public static boolean topMatch(InscriberRecipe recipe, ItemStack topInput) {
        return recipe.getTopOptional().test(topInput);
    }

    public static boolean bottomMatch(InscriberRecipe recipe, ItemStack bottomInput) {
        return recipe.getBottomOptional().test(bottomInput);
    }

    public boolean hasRecipeTriple(ItemStack topInput, ItemStack centerInput, ItemStack bottomInput) {
        return tripleInputCacheMap.hasRecipe(bottomInput, r -> topMatch(r, topInput) && bottomMatch(r, bottomInput));
    }

    public InscriberRecipe getRecipeTriple(ItemStack topInput, ItemStack centerInput, ItemStack bottomInput) {
        return tripleInputCacheMap.getRecipe(bottomInput, r -> topMatch(r, topInput) && bottomMatch(r, bottomInput));
    }

    public boolean hasRecipeDouble(ItemStack firstInput, ItemStack secondInput) {
        return hasRecipeTopMiddle(firstInput, secondInput) || hasRecipeMiddleBottom(firstInput, secondInput);
    }

    public boolean hasRecipeTopMiddle(ItemStack topInput, ItemStack centerInput) {
        return topMiddleCacheMap.hasRecipe(centerInput, r -> topMatch(r, topInput));
    }

    public boolean hasRecipeMiddleBottom(ItemStack centerInput, ItemStack bottomInput) {
        return middleBottomCacheMap.hasRecipe(centerInput, r -> bottomMatch(r, bottomInput));
    }

    public InscriberRecipe getRecipeDouble(ItemStack firstInput, ItemStack secondInput) {
        InscriberRecipe result = getRecipeTopMiddle(firstInput, secondInput);
        return result != null ? result : getRecipeMiddleBottom(firstInput, secondInput);
    }

    public InscriberRecipe getRecipeTopMiddle(ItemStack topInput, ItemStack centerInput) {
        return topMiddleCacheMap.getRecipe(centerInput, r -> topMatch(r, topInput));
    }

    public InscriberRecipe getRecipeMiddleBottom(ItemStack centerInput, ItemStack bottomInput) {
        return middleBottomCacheMap.getRecipe(centerInput, r -> bottomMatch(r, bottomInput));
    }

    public boolean hasRecipeSingle(ItemStack centerInput) {
        return singleInputCacheMap.hasRecipe(centerInput);
    }

    public InscriberRecipe getRecipeSingle(ItemStack centerInput) {
        return singleInputCacheMap.getRecipe(centerInput);
    }

    public boolean isCatalystItem(ItemStack stack) {
        return catalystCacheSet.contains(stack.getItem());
    }
}
