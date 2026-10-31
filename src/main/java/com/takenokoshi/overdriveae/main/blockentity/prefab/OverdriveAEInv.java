package com.takenokoshi.overdriveae.main.blockentity.prefab;

import java.util.Objects;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.AEKeySlotFilter;
import appeng.helpers.externalstorage.GenericStackInv;

public class OverdriveAEInv extends GenericStackInv {

    private final OverdriveAEInvUpdateListener updateListener;

    public OverdriveAEInv(@Nullable Runnable listener, int size, OverdriveAEInvUpdateListener updateListener) {
        super(null, size);
        this.updateListener = updateListener;
    }

    public OverdriveAEInv(int size, OverdriveAEInvUpdateListener updateListener){
        this(null, size, updateListener);
    }

    @Override
    public void setFilter(@Nullable AEKeySlotFilter filter) {
        super.setFilter(filter);
    }

    public void setFilter(Predicate<AEKey> predicate) {
        setFilter((slot, key) -> predicate.test(key));
    }

    @Override
    public void setStack(int slot, @Nullable GenericStack stack) {
        if (stack != null) {
            AEKey what = stack.what();
            if (this.getMaxAmount(what) < stack.amount()) {
                stack = new GenericStack(what, this.getMaxAmount(what));
            }
        }

        if (!Objects.equals(this.stacks[slot], stack)) {
            this.stacks[slot] = stack;
            this.updateListener.onChange(slot, stack);
            this.onChange();
        }
    }

    @FunctionalInterface
    public static interface OverdriveAEInvUpdateListener {
        void onChange(int slotIndex, @Nullable GenericStack stack);
    }

}
