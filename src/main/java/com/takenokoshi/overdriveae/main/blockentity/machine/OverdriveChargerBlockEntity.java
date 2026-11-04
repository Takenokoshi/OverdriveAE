package com.takenokoshi.overdriveae.main.blockentity.machine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.main.blockentity.prefab.BigGenericHolder;
import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveConstantEnergyUsageBlockEntity;
import com.takenokoshi.overdriveae.main.menu.OverdriveMachineMenu;
import com.takenokoshi.overdriveae.main.recipe.lookup.ChargerRecipeLookupHandler;
import com.takenokoshi.overdriveae.main.recipe.pattern.ChargerPattern;
import com.takenokoshi.overdriveae.main.recipe.patterncache.SingleToSingleCache;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEMachines;
import com.takenokoshi.overdriveae.main.util.AEKeyUtils;

import appeng.api.config.PowerMultiplier;
import appeng.api.ids.AEComponents;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.misc.ChargerBlockEntity;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedProcessingPattern;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveChargerBlockEntity extends
        OverdriveConstantEnergyUsageBlockEntity<ChargerPattern, SingleToSingleCache>
        implements ChargerRecipeLookupHandler {

    public OverdriveChargerBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine.getBlockEntityType(), pos, blockState, deferredMachine,
                () -> ChargerBlockEntity.POWER_MAXIMUM_AMOUNT * 1.0D,
                () -> ChargerBlockEntity.POWER_MAXIMUM_AMOUNT * 256.0D, PowerMultiplier.CONFIG);
    }

    @Override
    protected OverdriveMachineLogic<ChargerPattern, SingleToSingleCache> createLogic() {
        return new OverdriveMachineLogic<>(this, getMainNode(), ChargerPattern.class, ChargerPattern[][]::new,
                ChargerPattern[]::new, IActionSource.ofMachine(this));
    }

    @Override
    public @NotNull ChargerPattern[] decodePattern(@Nullable GenericStack stack) {
        if (stack == null) {
            return new ChargerPattern[] {};
        }
        if (stack.what() instanceof AEItemKey itemKey) {
            if (AEItems.PROCESSING_PATTERN.is(itemKey)) {
                EncodedProcessingPattern pattern = itemKey.get(AEComponents.ENCODED_PROCESSING_PATTERN);
                if (pattern == null) {
                    return new ChargerPattern[] {};
                }
                ItemStack input = AEKeyUtils.findFirstItemStack(pattern.sparseInputs());
                if (hasRecipe(input)) {
                    return new ChargerPattern[] {
                            new ChargerPattern(itemKey, getRecipe(input)),
                    };
                }
            } else if (hasRecipe(itemKey.getReadOnlyStack())) {
                return new ChargerPattern[] {
                        new ChargerPattern(itemKey, getRecipe(itemKey.getReadOnlyStack())),
                };
            }
        }
        return new ChargerPattern[] {};
    }

    @Override
    public boolean isPatternAcceptable(@Nullable AEKey key) {
        if (key != null && key instanceof AEItemKey itemKey) {
            if (AEItems.PROCESSING_PATTERN.is(itemKey)) {
                EncodedProcessingPattern pattern = itemKey.get(AEComponents.ENCODED_PROCESSING_PATTERN);
                if (pattern == null) {
                    return false;
                }
                return hasRecipe(AEKeyUtils.findFirstItemStack(pattern.sparseInputs()));
            } else {
                return hasRecipe(itemKey.getReadOnlyStack());
            }
        }
        return false;
    }

    @Override
    public @NotNull SingleToSingleCache createCache(@NotNull ChargerPattern pattern,
            @NotNull KeyCounter[] inputHolder) {
        return pattern.createCache(inputHolder[0], getLevel());
    }

    @Override
    public boolean matchesCache(@NotNull SingleToSingleCache cache, @NotNull KeyCounter[] inputHolder) {
        return cache.matchesInput(inputHolder[0]);
    }

    @Override
    public void consumeInputs(@NotNull ChargerPattern pattern, @NotNull SingleToSingleCache cache, long times,
            KeyCounter[] inputHolder) {
        inputHolder[0].remove(cache.input().what(), times);
    }

    @Override
    public void addOutputsToHolder(@NotNull ChargerPattern pattern, @NotNull SingleToSingleCache cache, long times,
            BigGenericHolder outputHolder) {
        outputHolder.add(cache.output().what(), times);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(OverdriveMachineMenu.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(OverdriveMachineMenu.TYPE, player, subMenu.getLocator());
    }

    @Override
    public InternalInventory getInternalInventory() {
        return InternalInventory.empty();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(OverdriveAEMachines.OVERDRIVE_CHARGER, 1);
    }

    @Override
    public Component getGuiTitle() {
        return hasCustomName() ? getCustomName() : OverdriveAEMachines.OVERDRIVE_CHARGER.deferredBlock.get().getName();
    }

}
