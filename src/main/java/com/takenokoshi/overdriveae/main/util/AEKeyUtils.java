package com.takenokoshi.overdriveae.main.util;

import java.util.List;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class AEKeyUtils {

    public static GenericStack[] ingredientToGeneric(Ingredient ingredient) {
        final ItemStack[] itemStacks = ingredient.getItems();
        GenericStack[] stacks = new GenericStack[itemStacks.length];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = GenericStack.fromItemStack(itemStacks[i]);
        }
        return stacks;
    }

    public static GenericStack[] sizedIngredientToGeneric(SizedIngredient sizedIngredient) {
        final ItemStack[] itemStacks = sizedIngredient.getItems();
        GenericStack[] stacks = new GenericStack[itemStacks.length];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = GenericStack.fromItemStack(itemStacks[i]);
        }
        return stacks;
    }

    public static ItemStack genericToItem(GenericStack stack) {
        return stack.what() instanceof AEItemKey itemKey
                ? itemKey.toStack(Math.clamp(stack.amount(), 0, Integer.MAX_VALUE))
                : ItemStack.EMPTY;
    }

    public static ItemStack findFirstItemStack(List<GenericStack> genericStacks) {
        for (GenericStack stack : genericStacks) {
            if (stack == null) {
                continue;
            }
            if (stack.what() instanceof AEItemKey itemKey) {
                return itemKey.toStack(Math.clamp(stack.amount(), 0, Integer.MAX_VALUE));
            }
        }
        return ItemStack.EMPTY;
    }

}
