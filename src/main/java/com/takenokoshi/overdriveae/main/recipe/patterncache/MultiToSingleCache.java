package com.takenokoshi.overdriveae.main.recipe.patterncache;

import org.jetbrains.annotations.NotNull;

import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;

public record MultiToSingleCache(GenericStack[] inputs, GenericStack output) {

    @Override
    public final int hashCode() {
        return inputs.hashCode() * 31 + output.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj != null && obj instanceof MultiToSingleCache other) {
            return this.inputs.equals(other.inputs) && this.output.equals(other.output);
        } else {
            return false;
        }
    }

    public boolean matchesInput(@NotNull KeyCounter[] counter) {
        if (inputs.length != counter.length) {
            return false;
        }
        for (int i = 0; i < inputs.length; i++) {
            if (!inputs[i].what().equals(counter[i].getFirstKey())) {
                return false;
            }
        }
        return true;
    }
}
