package com.takenokoshi.overdriveae.main.recipe.patterncache;

import org.jetbrains.annotations.NotNull;

import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;

public record SingleToSingleCache(@NotNull GenericStack input, @NotNull GenericStack output) {

    @Override
    public final int hashCode() {
        return input.hashCode() * 31 + output.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj != null && obj instanceof SingleToSingleCache other) {
            return this.input.equals(other.input) && this.output.equals(other.output);
        } else {
            return false;
        }
    }

    public boolean matchesInput(@NotNull KeyCounter counter) {
        return input.what().equals(counter.getFirstKey(input.what().getClass()));
    }
}
