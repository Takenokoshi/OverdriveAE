package com.takenokoshi.overdriveae.main.blockentity.machine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.main.blockentity.prefab.BigGenericHolder;
import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveMachineBlockEntity;
import com.takenokoshi.overdriveae.main.menu.OverdriveMachineMenu;
import com.takenokoshi.overdriveae.main.recipe.pattern.CondenserPattern;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEMachines;

import appeng.api.ids.AEComponents;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedProcessingPattern;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveCondenserBlockEntity extends
        OverdriveMachineBlockEntity<CondenserPattern, CondenserPattern> {

    public OverdriveCondenserBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine.getBlockEntityType(), pos, blockState, deferredMachine);
    }

    @Override
    protected OverdriveMachineLogic<CondenserPattern, CondenserPattern> createLogic() {
        return new OverdriveMachineLogic<>(this, getMainNode(), CondenserPattern.class, IActionSource.ofMachine(this));
    }

    @Override
    public @NotNull CondenserPattern[] decodePattern(@Nullable GenericStack stack) {
        if (stack == null) {
            return new CondenserPattern[] {};
        }
        AEKey stackKey = stack.what();
        if (stackKey instanceof AEItemKey itemKey) {
            if (AEItems.PROCESSING_PATTERN.is(itemKey)) {
                EncodedProcessingPattern pattern = itemKey.get(AEComponents.ENCODED_PROCESSING_PATTERN);
                if (pattern == null || pattern.sparseInputs().get(0) == null) {
                    return new CondenserPattern[] {};
                }
                AEKey encodedKey = pattern.sparseInputs().get(0).what();
                if (encodedKey instanceof AEItemKey encodedItemKey) {
                    return new CondenserPattern[] {
                            CondenserPattern.matterBall(itemKey, encodedItemKey),
                            CondenserPattern.singularity(itemKey, encodedItemKey),
                    };
                } else if (encodedKey instanceof AEFluidKey encodedFluidKey) {
                    return new CondenserPattern[] {
                            CondenserPattern.matterBall(itemKey, encodedFluidKey),
                            CondenserPattern.singularity(itemKey, encodedFluidKey),
                    };
                }
            } else {
                return new CondenserPattern[] {
                        CondenserPattern.matterBall(itemKey, itemKey),
                        CondenserPattern.singularity(itemKey, itemKey),
                };
            }
        } else if (stackKey instanceof AEFluidKey fluidKey) {
            return new CondenserPattern[] {
                    CondenserPattern.matterBall(AEItemKey.of(Items.BUCKET), fluidKey),
                    CondenserPattern.singularity(AEItemKey.of(Items.BUCKET), fluidKey),
            };
        }
        return new CondenserPattern[] {};
    }

    @Override
    public boolean isPatternAcceptable(@Nullable AEKey key) {
        return key instanceof AEItemKey || key instanceof AEFluidKey;
    }

    @Override
    public @NotNull CondenserPattern createCache(@NotNull CondenserPattern pattern, @NotNull KeyCounter[] inputHolder) {
        return pattern;
    }

    @Override
    public boolean matchesCache(@NotNull CondenserPattern cache, @NotNull KeyCounter[] inputHolder) {
        return true;
    }

    @Override
    public void consumeInputs(@NotNull CondenserPattern pattern, @NotNull CondenserPattern cache, long times,
            KeyCounter[] inputHolder) {
        inputHolder[0].remove(cache.input.what(), cache.input.amount() * times);
    }

    @Override
    public void addOutputsToHolder(@NotNull CondenserPattern pattern, @NotNull CondenserPattern cache, long times,
            BigGenericHolder outputHolder) {
        outputHolder.add(cache.output, times);
    }

    @Override
    public Component getGuiTitle() {
        return hasCustomName() ? getCustomName() : OverdriveAEMachines.OVERDRIVE_MATTER_CONDENSER.getBlock().getName();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(OverdriveAEMachines.OVERDRIVE_MATTER_CONDENSER, 1);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(OverdriveMachineMenu.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(OverdriveMachineMenu.TYPE, player, subMenu.getLocator());
    }

}
