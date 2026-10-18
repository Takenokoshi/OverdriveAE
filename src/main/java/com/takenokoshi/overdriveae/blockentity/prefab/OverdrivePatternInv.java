package com.takenokoshi.overdriveae.blockentity.prefab;

import java.util.function.Predicate;

import appeng.api.inventories.InternalInventory;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.world.item.ItemStack;

public class OverdrivePatternInv extends AppEngInternalInventory {

    private final PatternInvChangeListener patternInvChangeListener;

    public OverdrivePatternInv(InternalInventoryHost host, int size, IAEItemFilter filter,
            PatternInvChangeListener patternInvChangeListener) {
        super(host, size, 1, filter);
        this.patternInvChangeListener = patternInvChangeListener;
    }
    public OverdrivePatternInv(InternalInventoryHost host, int size, Predicate<ItemStack> filter,
            PatternInvChangeListener patternInvChangeListener) {
        super(host, size, 1, new OverdrivePatternInvFilter(filter));
        this.patternInvChangeListener = patternInvChangeListener;
    }

    @Override
    public void setItemDirect(int slot, ItemStack stack) {
        super.setItemDirect(slot, stack);
        patternInvChangeListener.onPatternInvChanged(slot, stack);
    }

    @FunctionalInterface
    public static interface PatternInvChangeListener {
        void onPatternInvChanged(int slot, ItemStack stack);
    }

    public static class OverdrivePatternInvFilter implements IAEItemFilter {
        private final Predicate<ItemStack> isItemVaild;

        public OverdrivePatternInvFilter(Predicate<ItemStack> isItemVaild) {
            this.isItemVaild = isItemVaild;
        }

        @Override
        public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
            return  isItemVaild.test(stack);
        }

    }
}
