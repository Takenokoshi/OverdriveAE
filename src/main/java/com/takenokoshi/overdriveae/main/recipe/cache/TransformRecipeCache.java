package com.takenokoshi.overdriveae.main.recipe.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.takenokoshi.overdriveae.main.recipe.map.ItemCacheMap;

import appeng.core.definitions.AEItems;
import appeng.recipes.transform.TransformRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;

/**
 * レシピにかいてある順番通りの材料搬入しか受け付けない
 */
public class TransformRecipeCache implements IRecipeCache<TransformRecipe> {

    private final ItemCacheMap<TransformRecipe> cacheMap = new ItemCacheMap<>();
    private final Set<Fluid> catalystFluids = new HashSet<>();

    @Override
    public void initCaches(List<RecipeHolder<TransformRecipe>> recipeHolders) {
        for (RecipeHolder<TransformRecipe> recipeHolder : recipeHolders) {
            TransformRecipe recipe = recipeHolder.value();
            /**
             * 液体以外で加工するレシピはサポートしない
             * 量子もつれシンギュラリティを出力するレシピはサポートしない
             */
            if (!recipe.circumstance.isFluid() || AEItems.QUANTUM_ENTANGLED_SINGULARITY.is(recipe.output)) {
                continue;
            }
            cacheMap.putRecipe(recipe.ingredients.get(0), recipe);
            catalystFluids.addAll(recipe.circumstance.getFluidsForRendering());
        }
    }

    @Override
    public void clear() {
        cacheMap.clear();
        catalystFluids.clear();
    }

    public boolean hasRecipe(ItemStack[] stacks, Fluid fluid) {
        if (stacks.length == 0) {
            return false;
        }
        return cacheMap.hasRecipe(stacks[0], r -> matchIngredients(stacks, r.ingredients) && r.circumstance.isFluid(fluid));
    }

    private static boolean matchIngredients(ItemStack[] stacks, List<Ingredient> ingredients) {
        if (stacks.length != ingredients.size() || stacks.length == 0 || ingredients.isEmpty()) {
            return false;
        }
        // 最初のアイテムと材料の一致はcacheMap側が確認済
        for (int i = 1; i < stacks.length; i++) {
            if (!ingredients.get(i).test(stacks[i])) {
                return false;
            }
        }
        return true;
    }

    public TransformRecipe getRecipe(ItemStack[] stacks, Fluid fluid) {
        if (stacks.length == 0) {
            return null;
        }
        return cacheMap.getRecipe(stacks[0],
                r -> matchIngredients(stacks, r.ingredients) && r.circumstance.isFluid(fluid));
    }

    public boolean isCatalystFluid(Fluid fluid) {
        return catalystFluids.contains(fluid);
    }

}
