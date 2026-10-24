package com.takenokoshi.overdriveae.main.blockentity.machine;

import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveMachineBlockEntity;
import com.takenokoshi.overdriveae.main.pattern.SingleToSinglePattern;
import com.takenokoshi.overdriveae.main.pattern.SingleToSinglePattern.SingleToSingleAssembly;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;

import appeng.api.config.CondenserOutput;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedProcessingPattern;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveCondenserBlockEntity extends
        OverdriveMachineBlockEntity<SingleToSinglePattern.SingleToSingleAssembly, SingleToSinglePattern, OverdriveCondenserBlockEntity> {

    public OverdriveCondenserBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine, pos, blockState);
    }

    @Override
    protected OverdriveMachineLogic<SingleToSingleAssembly, SingleToSinglePattern, OverdriveCondenserBlockEntity> createLogic() {
        return new OverdriveMachineLogic<>(getMainNode(), this, SingleToSinglePattern[]::new,
                SingleToSinglePattern.class);
    }

    @Override
    public @Nullable SingleToSinglePattern decodePattern(ItemStack itemStack) {
        if (AEItems.PROCESSING_PATTERN.is(itemStack)) {
            EncodedProcessingPattern pattern = itemStack.get(AEComponents.ENCODED_PROCESSING_PATTERN);
            if (pattern == null) {
                return null;
            }
            GenericStack input = pattern.sparseInputs().get(0);
            GenericStack output = pattern.sparseOutputs().get(0);
            if (input == null || output == null) {
                return null;
            }
            if (AEItems.SINGULARITY.is(output.what())) {
                if (input.what() instanceof AEItemKey) {
                    return new SingleToSinglePattern(AEItemKey.of(itemStack), new GenericStack[] {
                            new GenericStack(input.what(), CondenserOutput.SINGULARITY.requiredPower),
                    }, AEItems.SINGULARITY.genericStack(1L));
                } else if (input.what() instanceof AEFluidKey what) {
                    return new SingleToSinglePattern(AEItemKey.of(itemStack), new GenericStack[] {
                            new GenericStack(what,
                                    1L * CondenserOutput.SINGULARITY.requiredPower * what.getAmountPerOperation()),
                    }, AEItems.SINGULARITY.genericStack(1L));
                }
            } else if (AEItems.MATTER_BALL.is(output.what())) {
                if (input.what() instanceof AEItemKey) {
                    return new SingleToSinglePattern(AEItemKey.of(itemStack), new GenericStack[] {
                            new GenericStack(input.what(), CondenserOutput.MATTER_BALLS.requiredPower),
                    }, AEItems.MATTER_BALL.genericStack(1L));
                } else if (input.what() instanceof AEFluidKey what) {
                    return new SingleToSinglePattern(AEItemKey.of(itemStack), new GenericStack[] {
                            new GenericStack(what,
                                    1L * CondenserOutput.MATTER_BALLS.requiredPower * what.getAmountPerOperation()),
                    }, AEItems.MATTER_BALL.genericStack(1L));
                }
            }
        }
        return null;
    }

    @Override
    public boolean isPatternAcceptable(ItemStack itemStack) {
        if (AEItems.PROCESSING_PATTERN.is(itemStack)) {
            EncodedProcessingPattern pattern = itemStack.get(AEComponents.ENCODED_PROCESSING_PATTERN);
            if (pattern == null) {
                return false;
            }
            GenericStack input = pattern.sparseInputs().get(0);
            GenericStack output = pattern.sparseOutputs().get(0);
            if (input == null || output == null) {
                return false;
            }
            AEKey whatI = input.what();
            AEKey whatO = output.what();
            return (whatI instanceof AEItemKey || whatI instanceof AEFluidKey)
                    && (AEItems.SINGULARITY.is(whatO) || AEItems.MATTER_BALL.is(whatO));
        }
        return false;
    }

    @Override
    public SingleToSingleAssembly assemble(SingleToSinglePattern pattern, KeyCounter[] inputHolder) {
        AEKey what = inputHolder[0].getFirstKey();
        long rq = AEItems.SINGULARITY.is(pattern.output.what())
                ? CondenserOutput.SINGULARITY.requiredPower
                : CondenserOutput.MATTER_BALLS.requiredPower;
        return new SingleToSingleAssembly(what instanceof AEFluidKey fluidKey
                ? new GenericStack(fluidKey, rq * fluidKey.getAmountPerOperation())
                : new GenericStack(what, rq),
                pattern.output);
    }

    @Override
    public boolean matchesCache(SingleToSinglePattern.SingleToSingleAssembly assembly, KeyCounter[] inputHolder) {
        return assembly.input.what().equals(inputHolder[0].getFirstKey());
    }

    @Override
    public double getEnergyPerProcess(SingleToSinglePattern pattern) {
        return 0.0D;
    }

}
