package com.takenokoshi.overdriveae.main.pattern;

import org.jetbrains.annotations.Nullable;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.level.Level;

public class SimpleInput implements IPatternDetails.IInput {
    private final GenericStack[] input;

    public SimpleInput(GenericStack[] input) {
        this.input = input;
    }

    @Override
    public long getMultiplier() {
        return 1L;
    }

    @Override
    public GenericStack[] getPossibleInputs() {
        return input;
    }

    @Override
    public @Nullable AEKey getRemainingKey(AEKey arg0) {
        return null;
    }

    @Override
    public boolean isValid(AEKey what, Level arg1) {
        for (GenericStack input : input) {
            if (input.what().equals(what)) {
                return true;
            }
        }
        return false;
    }
}
