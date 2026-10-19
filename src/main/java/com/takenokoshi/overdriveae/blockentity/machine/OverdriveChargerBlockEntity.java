package com.takenokoshi.overdriveae.blockentity.machine;

import java.util.Arrays;

import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.blockentity.prefab.OverdriveMachineBlockEntity;
import com.takenokoshi.overdriveae.blockentity.prefab.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.pattern.SingleToSinglePattern;
import com.takenokoshi.overdriveae.pattern.SingleToSinglePattern.SingleToSingleAssembly;
import com.takenokoshi.overdriveae.recipe.lookup.ChargerRecipeLookupHandler;
import com.takenokoshi.overdriveae.registration.OAEDeferredMachine;

import appeng.api.config.PowerMultiplier;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedProcessingPattern;
import appeng.recipes.handlers.ChargerRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveChargerBlockEntity extends
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

    /** チャージャーのレシピの材料になっているアイテムをパターンの代わりに使用することができるようにしてある */

    /**
     * 適合するレシピが見つかった場合、そのレシピに合わせたパターンに変換する
     * 処理パターンの場合は、材料の要求数が誤っている場合それを矯正、成果物はパターンの内容を無視して常に正しいものを指定
     * 処理パターンの最初の材料のみを見る。2番目以降に何か入ってても無視
     * AEProcessingPattern に変換せず、EncodedProcessingPattern を直接変換する
     */
    @Override
    public @Nullable SingleToSinglePattern tryDecodePattern(ItemStack itemStack) {

        if (AEItems.PROCESSING_PATTERN.is(itemStack)) {
            EncodedProcessingPattern pattern = itemStack.get(AEComponents.ENCODED_PROCESSING_PATTERN);
            if (pattern == null) {
                return null;
            }
            var input = pattern.sparseInputs().get(0);
            if (input.what() instanceof AEItemKey itemKey) {
                ChargerRecipe recipe = getRecipe(itemKey.getReadOnlyStack());
                return recipe == null ? null
                        : new SingleToSinglePattern(AEItemKey.of(itemStack),
                                Arrays.stream(recipe.ingredient.getItems())
                                        .map(stack -> GenericStack.fromItemStack(stack.copyWithCount(1)))
                                        .toArray(GenericStack[]::new),
                                GenericStack.fromItemStack(recipe.result));
            }
            return null;
        }

        ChargerRecipe recipe = getRecipe(itemStack);
        return recipe == null ? null
                : new SingleToSinglePattern(AEItemKey.of(itemStack),
                        Arrays.stream(recipe.ingredient.getItems())
                                .map(stack -> GenericStack.fromItemStack(stack.copyWithCount(1)))
                                .toArray(GenericStack[]::new),
                        GenericStack.fromItemStack(recipe.result));
    }

    @Override
    public boolean isPatternAcceptable(ItemStack itemStack) {
        if (AEItems.PROCESSING_PATTERN.is(itemStack)) {
            EncodedProcessingPattern pattern = itemStack.get(AEComponents.ENCODED_PROCESSING_PATTERN);
            if (pattern == null) {
                return false;
            }
            var input = pattern.sparseInputs().get(0);
            if (input.what() instanceof AEItemKey itemKey) {
                return hasRecipe(itemKey.getReadOnlyStack());
            }
            return false;
        }
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

    @Override
    public double getEnergyPerProcess(SingleToSingleAssembly assembly) {
        return PowerMultiplier.CONFIG.multiply(ChargerBlockEntity.POWER_MAXIMUM_AMOUNT);
    }

}
