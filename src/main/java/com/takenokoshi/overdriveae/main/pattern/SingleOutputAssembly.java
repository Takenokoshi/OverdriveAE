package com.takenokoshi.overdriveae.main.pattern;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;

import appeng.api.stacks.GenericStack;

public class SingleOutputAssembly implements OverdriveMachineLogic.Assembly {

    public final GenericStack[] inputs;
    public final GenericStack output;

    public SingleOutputAssembly(GenericStack[] inputs, GenericStack output) {
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public GenericStack[] inputs() {
        return inputs;
    }

    @Override
    public GenericStack[] outputs() {
        return new GenericStack[] { output };
    }
}
