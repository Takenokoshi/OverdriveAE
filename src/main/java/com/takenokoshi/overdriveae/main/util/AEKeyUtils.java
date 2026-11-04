package com.takenokoshi.overdriveae.main.util;

import java.util.ArrayList;
import java.util.List;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

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
                ? itemKey.toStack(Math.clamp(stack.amount(), 1, Integer.MAX_VALUE))
                : ItemStack.EMPTY;
    }

    public static ItemStack findFirstItemStack(List<GenericStack> genericStacks) {
        for (GenericStack stack : genericStacks) {
            if (stack == null) {
                continue;
            }
            if (stack.what() instanceof AEItemKey itemKey) {
                return itemKey.toStack(Math.clamp(stack.amount(), 1, Integer.MAX_VALUE));
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack[] filterAndMapToItemStack(List<GenericStack> genericStacks) {
        List<ItemStack> result = new ArrayList<>();
        for (GenericStack stack : genericStacks) {
            if (stack == null) {
                continue;
            }
            if (stack.what() instanceof AEItemKey itemKey) {
                result.add(itemKey.toStack(Math.clamp(stack.amount(), 1, Integer.MAX_VALUE)));
            }
        }
        return result.toArray(ItemStack[]::new);
    }

    public static GenericStack[] fluidIngredientToGeneric(FluidIngredient ingredient, int amount) {
        final FluidStack[] fluidStacks = ingredient.getStacks();
        GenericStack[] stacks = new GenericStack[fluidStacks.length];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = GenericStack.fromFluidStack(fluidStacks[i].copyWithAmount(amount));
        }
        return stacks;
    }

    public static GenericStack[] sizedFluidIngredientToGeneric(SizedFluidIngredient ingredient) {
        final FluidStack[] fluidStacks = ingredient.getFluids();
        GenericStack[] stacks = new GenericStack[fluidStacks.length];
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = GenericStack.fromFluidStack(fluidStacks[i]);
        }
        return stacks;
    }

    public static FluidStack genericToFluid(GenericStack stack) {
        return stack.what() instanceof AEFluidKey fluidKey
                ? fluidKey.toStack(Math.clamp(stack.amount(), 1, Integer.MAX_VALUE))
                : FluidStack.EMPTY;
    }

    public static FluidStack findFirstFluidStack(List<GenericStack> genericStacks) {
        for (GenericStack stack : genericStacks) {
            if (stack == null) {
                continue;
            }
            if (stack.what() instanceof AEFluidKey fluidKey) {
                return fluidKey.toStack(Math.clamp(stack.amount(), 1, Integer.MAX_VALUE));
            }
        }
        return FluidStack.EMPTY;
    }

    public static FluidStack[] filterAndMapToFluidStack(List<GenericStack> genericStacks) {
        List<FluidStack> result = new ArrayList<>();
        for (GenericStack stack : genericStacks) {
            if (stack == null) {
                continue;
            }
            if (stack.what() instanceof AEFluidKey fluidKey) {
                result.add(fluidKey.toStack(Math.clamp(stack.amount(), 1, Integer.MAX_VALUE)));
            }
        }
        return result.toArray(FluidStack[]::new);
    }

}
