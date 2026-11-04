package com.takenokoshi.overdriveae.main.recipe.pattern;

import java.util.Arrays;
import java.util.List;

import com.takenokoshi.overdriveae.main.recipe.patterncache.MultiToSingleCache;
import com.takenokoshi.overdriveae.main.util.AEKeyUtils;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.recipes.transform.TransformRecipe;

public class TransformPattern implements IPatternDetails {
    private final AEItemKey def;
    public final TransformRecipe recipe;
    public final GenericStack[][] inputs;
    public final GenericStack output;

    private final SimpleInput[] simpleInputs;
    private final List<GenericStack> outputs;

    public TransformPattern(AEItemKey def, TransformRecipe recipe) {
        this.def = def;
        this.recipe = recipe;
        this.inputs = this.recipe.ingredients.stream().map(AEKeyUtils::ingredientToGeneric)
                .toArray(GenericStack[][]::new);
        this.output = GenericStack.fromItemStack(this.recipe.output);

        this.simpleInputs = Arrays.stream(this.inputs).map(SimpleInput::new).toArray(SimpleInput[]::new);
        this.outputs = List.of(this.output);
    }

    @Override
    public AEItemKey getDefinition() {
        return def;
    }

    @Override
    public IInput[] getInputs() {
        return simpleInputs;
    }

    @Override
    public List<GenericStack> getOutputs() {
        return outputs;
    }

    @Override
    public int hashCode() {
        return def.hashCode() * 31 + recipe.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj != null && obj.getClass() == this.getClass()) {
            TransformPattern other = (TransformPattern) obj;
            return other.def.equals(this.def) && other.recipe.equals(this.recipe);
        } else {
            return false;
        }
    }

    public MultiToSingleCache createCache(KeyCounter[] counters) {
        return new MultiToSingleCache(Arrays.stream(counters)
                .map(counter -> new GenericStack(counter.getFirstKey(), 1L)).toArray(GenericStack[]::new), output);
    }
}
