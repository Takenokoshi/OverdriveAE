package com.takenokoshi.overdriveae.blockentity.machine;

import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.blockentity.prefab.OverdriveMachineBlockEntity;
import com.takenokoshi.overdriveae.blockentity.prefab.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.pattern.SingleToSinglePattern;
import com.takenokoshi.overdriveae.recipe.lookup.ChargerRecipeLookupHandler;
import com.takenokoshi.overdriveae.registration.OAEDeferredMachine;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.recipes.handlers.ChargerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveChargerBlockEntity
        extends
        OverdriveMachineBlockEntity<SingleToSinglePattern.SingleToSingleAssembly, SingleToSinglePattern, OverdriveChargerBlockEntity>
        implements ChargerRecipeLookupHandler {

    public OverdriveChargerBlockEntity(OAEDeferredMachine<?, ?> deferredMachine, BlockPos pos, BlockState blockState) {
        super(deferredMachine, pos, blockState, 0);
    }

    @Override
    protected OverdriveMachineLogic<SingleToSinglePattern.SingleToSingleAssembly, SingleToSinglePattern, OverdriveChargerBlockEntity> createLogic() {
        return new OverdriveMachineLogic<>(getMainNode(), this, SingleToSinglePattern[]::new,
                SingleToSinglePattern.class);
    }

    @Override
    public @Nullable SingleToSinglePattern tryDecodePattern(ItemStack itemStack) {
        ChargerRecipe recipe = getRecipe(itemStack);
        return recipe == null ? null
                : new SingleToSinglePattern(AEItemKey.of(itemStack),
                        new GenericStack[] { GenericStack.fromItemStack(itemStack) },
                        GenericStack.fromItemStack(recipe.result));
    }

    @Override
    public boolean isPatternAcceptable(ItemStack itemStack) {
        return hasRecipe(itemStack);
    }

    @Override
    public SingleToSinglePattern.SingleToSingleAssembly assemble(SingleToSinglePattern pattern,
            KeyCounter[] inputHolder) {
        AEKey what = inputHolder[0].getFirstKey();
        /** チャージャーの材料は常に1個 */
        return new SingleToSinglePattern.SingleToSingleAssembly(new GenericStack(what, 1), pattern.output);
    }

    @Override
    public boolean matchesCache(SingleToSinglePattern.SingleToSingleAssembly assembly, KeyCounter[] inputHolder) {
        return assembly.input.what().equals(inputHolder[0].getFirstKey());
    }

}
