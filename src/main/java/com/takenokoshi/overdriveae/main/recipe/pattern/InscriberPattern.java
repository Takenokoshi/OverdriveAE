package com.takenokoshi.overdriveae.main.recipe.pattern;

import java.util.Arrays;
import java.util.List;

import com.takenokoshi.overdriveae.main.recipe.patterncache.MultiToSingleCache;
import com.takenokoshi.overdriveae.main.util.AEKeyUtils;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;

public class InscriberPattern implements IPatternDetails {
    private final AEItemKey def;
    public final InscriberRecipe recipe;
    public final GenericStack[][] inputs;
    public final GenericStack output;

    private final SimpleInput[] simpleInputs;
    private final List<GenericStack> outputs;

    public InscriberPattern(AEItemKey def, InscriberRecipe recipe) {
        this.def = def;
        this.recipe = recipe;
        if (this.recipe.getProcessType() == InscriberProcessType.INSCRIBE) {
            this.inputs = new GenericStack[][] { AEKeyUtils.ingredientToGeneric(this.recipe.getMiddleInput()), };
        } else {
            if (this.recipe.getTopOptional().isEmpty()) {
                if (this.recipe.getBottomOptional().isEmpty()) {
                    this.inputs = new GenericStack[][] {
                            AEKeyUtils.ingredientToGeneric(this.recipe.getMiddleInput()),
                    };
                } else {
                    this.inputs = new GenericStack[][] {
                            AEKeyUtils.ingredientToGeneric(this.recipe.getMiddleInput()),
                            AEKeyUtils.ingredientToGeneric(this.recipe.getBottomOptional()),
                    };
                }
            } else {
                if (this.recipe.getBottomOptional().isEmpty()) {
                    this.inputs = new GenericStack[][] {
                            AEKeyUtils.ingredientToGeneric(this.recipe.getTopOptional()),
                            AEKeyUtils.ingredientToGeneric(this.recipe.getMiddleInput()),
                    };
                } else {
                    this.inputs = new GenericStack[][] {
                            AEKeyUtils.ingredientToGeneric(this.recipe.getTopOptional()),
                            AEKeyUtils.ingredientToGeneric(this.recipe.getMiddleInput()),
                            AEKeyUtils.ingredientToGeneric(this.recipe.getBottomOptional()),
                    };
                }
            }
        }
        this.output = GenericStack.fromItemStack(recipe.getResultItem());

        this.simpleInputs = Arrays.stream(this.inputs).map(SimpleInput::new).toArray(SimpleInput[]::new);
        this.outputs = List.of(output);
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
            InscriberPattern other = (InscriberPattern) obj;
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
