package com.takenokoshi.overdriveae.pattern;

import java.util.Collections;
import java.util.List;

import com.takenokoshi.overdriveae.blockentity.prefab.OverdriveMachineLogic.Assembly;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;

public class SingleToSinglePattern implements IPatternDetails {

    public final AEItemKey def;
    public final GenericStack[] input;
    public final GenericStack output;

    private final SimpleInput simpleInput;

    public SingleToSinglePattern(AEItemKey def, GenericStack[] input, GenericStack output) {
        this.def = def;
        this.input = input;
        this.output = output;
        this.simpleInput = new SimpleInput(this.input);
    }

    @Override
    public AEItemKey getDefinition() {
        return def;
    }

    @Override
    public IInput[] getInputs() {
        return new IInput[] { simpleInput, };
    }

    @Override
    public List<GenericStack> getOutputs() {
        return Collections.singletonList(output);
    }

    public static class SingleToSingleAssembly implements Assembly {

        public final GenericStack input;
        public final GenericStack output;

        public SingleToSingleAssembly(GenericStack input, GenericStack output) {
            this.input = input;
            this.output = output;
        }

        @Override
        public GenericStack[] inputs() {
            return new GenericStack[] { input };
        }

        @Override
        public GenericStack[] outputs() {
            return new GenericStack[] { output };
        }

    }

}
