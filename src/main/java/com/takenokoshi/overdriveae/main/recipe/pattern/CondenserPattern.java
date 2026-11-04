package com.takenokoshi.overdriveae.main.recipe.pattern;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import appeng.api.config.CondenserOutput;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;

public class CondenserPattern implements IPatternDetails {

    private final AEItemKey def;
    public final @NotNull GenericStack input;
    public final @NotNull AEItemKey output;

    private final SimpleInput[] simpleInputs;
    private final List<GenericStack> outputs;

    private CondenserPattern(AEItemKey def, @NotNull GenericStack input, @NotNull AEItemKey output) {
        this.def = def;
        this.input = input;
        this.output = output;

        this.simpleInputs = new SimpleInput[] { new SimpleInput(new GenericStack[] { this.input, }), };
        this.outputs = List.of(new GenericStack(this.output, 1L));
    }

    public static CondenserPattern matterBall(AEItemKey def, AEItemKey input) {
        return new CondenserPattern(def, new GenericStack(input, 1L * CondenserOutput.MATTER_BALLS.requiredPower),
                AEItemKey.of(AEItems.MATTER_BALL));
    }

    public static CondenserPattern matterBall(AEItemKey def, AEFluidKey input) {
        return new CondenserPattern(def,
                new GenericStack(input,
                        1L * AEKeyType.fluids().getAmountPerOperation() * CondenserOutput.MATTER_BALLS.requiredPower),
                AEItemKey.of(AEItems.MATTER_BALL));
    }

    public static CondenserPattern singularity(AEItemKey def, AEItemKey input) {
        return new CondenserPattern(def, new GenericStack(input, 1L * CondenserOutput.SINGULARITY.requiredPower),
                AEItemKey.of(AEItems.SINGULARITY));
    }

    public static CondenserPattern singularity(AEItemKey def, AEFluidKey input) {
        return new CondenserPattern(def,
                new GenericStack(input,
                        1L * AEKeyType.fluids().getAmountPerOperation() * CondenserOutput.SINGULARITY.requiredPower),
                AEItemKey.of(AEItems.SINGULARITY));
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
        return def.hashCode() * 961 + input.what().hashCode() * 31 + output.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj != null && obj.getClass().equals(this.getClass())) {
            CondenserPattern other = (CondenserPattern) obj;
            return this.def.equals(other.def) && this.input.what().equals(other.input.what())
                    && this.output.equals(other.output);
        } else {
            return false;
        }
    }
}
