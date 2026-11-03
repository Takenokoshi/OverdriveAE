package com.takenokoshi.overdriveae.main.recipe.pattern;

import java.util.List;

import com.takenokoshi.overdriveae.main.recipe.patterncache.SingleToSingleCache;
import com.takenokoshi.overdriveae.main.util.AEKeyUtils;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class CookingPattern implements IPatternDetails {

    private final AEItemKey def;
    public final AbstractCookingRecipe recipe;
    public final GenericStack[] input;
    public final GenericStack output;

    private final SimpleInput[] simpleInputs;
    private final List<GenericStack> outputs;

    public CookingPattern(AEItemKey def, AbstractCookingRecipe recipe, Level level) {
        this.def = def;
        this.recipe = recipe;
        this.input = AEKeyUtils.ingredientToGeneric(this.recipe.getIngredients().get(0));
        this.output = GenericStack.fromItemStack(this.recipe.getResultItem(level.registryAccess()));
        this.simpleInputs = new SimpleInput[] { new SimpleInput(input), };
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
            CookingPattern other = (CookingPattern) obj;
            return other.def.equals(this.def) && other.recipe.equals(this.recipe);
        } else {
            return false;
        }
    }

    public SingleToSingleCache createCache(KeyCounter counter, Level level) {
        AEItemKey inputKey = counter.getFirstKey(AEItemKey.class);
        /** 実際にinputKeyがnullになることはないはずである */
        return inputKey != null
                ? new SingleToSingleCache(new GenericStack(inputKey, 1L),
                        GenericStack.fromItemStack(recipe.assemble(new SingleRecipeInput(inputKey.getReadOnlyStack()),
                                level.registryAccess())))
                : new SingleToSingleCache(input[0], output);
    }
}
