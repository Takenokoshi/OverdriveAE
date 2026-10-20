package com.takenokoshi.overdriveae.pattern;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.world.item.crafting.Ingredient;

/** PatternDetailsHelper経由での変換はできない */
public class InscriberPattern implements IPatternDetails {
    public final AEItemKey def;
    public final GenericStack[][] inputs;
    public final GenericStack output;
    private final SimpleInput[] simpleInputs;

    public InscriberPattern(AEItemKey def, GenericStack[][] inputs, GenericStack output) {
        this.def = def;
        this.inputs = inputs;
        this.output = output;
        this.simpleInputs = new SimpleInput[this.inputs.length];
        for (int i = 0; i < this.inputs.length; i++) {
            this.simpleInputs[i] = new SimpleInput(this.inputs[i]);
        }
    }

    public static InscriberPattern fromRecipe(AEItemKey def, InscriberRecipe recipe) {

        if (recipe.getProcessType() == InscriberProcessType.INSCRIBE) {
            return new InscriberPattern(def, new GenericStack[][] {
                    Arrays.stream(recipe.getMiddleInput().getItems()).map(GenericStack::fromItemStack)
                            .toArray(GenericStack[]::new),
            }, GenericStack.fromItemStack(recipe.getResultItem()));
        }

        Ingredient topIngredient = recipe.getTopOptional();
        boolean isTopEmpty = topIngredient.isEmpty();
        Ingredient bottomIngredient = recipe.getBottomOptional();
        boolean isBottomEmpty = bottomIngredient.isEmpty();

        if (isTopEmpty && isBottomEmpty) {
            return new InscriberPattern(def, new GenericStack[][] {
                    Arrays.stream(recipe.getMiddleInput().getItems()).map(GenericStack::fromItemStack)
                            .toArray(GenericStack[]::new),
            }, GenericStack.fromItemStack(recipe.getResultItem()));
        } else if (isTopEmpty) {
            return new InscriberPattern(def, new GenericStack[][] {
                    Arrays.stream(recipe.getMiddleInput().getItems()).map(GenericStack::fromItemStack)
                            .toArray(GenericStack[]::new),
                    Arrays.stream(bottomIngredient.getItems()).map(GenericStack::fromItemStack)
                            .toArray(GenericStack[]::new),
            }, GenericStack.fromItemStack(recipe.getResultItem()));
        } else if (isBottomEmpty) {
            return new InscriberPattern(def, new GenericStack[][] {
                    Arrays.stream(topIngredient.getItems()).map(GenericStack::fromItemStack)
                            .toArray(GenericStack[]::new),
                    Arrays.stream(recipe.getMiddleInput().getItems()).map(GenericStack::fromItemStack)
                            .toArray(GenericStack[]::new),
            }, GenericStack.fromItemStack(recipe.getResultItem()));
        } else {
            return new InscriberPattern(def, new GenericStack[][] {
                    Arrays.stream(topIngredient.getItems()).map(GenericStack::fromItemStack)
                            .toArray(GenericStack[]::new),
                    Arrays.stream(recipe.getMiddleInput().getItems()).map(GenericStack::fromItemStack)
                            .toArray(GenericStack[]::new),
                    Arrays.stream(recipe.getMiddleInput().getItems()).map(GenericStack::fromItemStack)
                            .toArray(GenericStack[]::new),
            }, GenericStack.fromItemStack(recipe.getResultItem()));
        }
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
        return Collections.singletonList(output);
    }
}
