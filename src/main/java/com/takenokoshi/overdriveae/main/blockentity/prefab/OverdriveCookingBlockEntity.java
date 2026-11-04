package com.takenokoshi.overdriveae.main.blockentity.prefab;

import java.util.function.DoubleSupplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.main.menu.OverdriveMachineMenu;
import com.takenokoshi.overdriveae.main.recipe.lookup.CookingRecipeCacheLookupHandler;
import com.takenokoshi.overdriveae.main.recipe.pattern.CookingPattern;
import com.takenokoshi.overdriveae.main.recipe.patterncache.SingleToSingleCache;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;
import com.takenokoshi.overdriveae.main.util.AEKeyUtils;

import appeng.api.config.PowerMultiplier;
import appeng.api.ids.AEComponents;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedProcessingPattern;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.level.block.state.BlockState;

public abstract class OverdriveCookingBlockEntity<RECIPE extends AbstractCookingRecipe>
        extends OverdriveDynamicEnergyUsageBlockEntity<CookingPattern, SingleToSingleCache>
        implements CookingRecipeCacheLookupHandler<RECIPE> {

    protected final OverdriveAEDeferredMachine<?, ?> deferredMachine;

    public OverdriveCookingBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState,
            DoubleSupplier baseEnergyCapacity) {
        super(deferredMachine.getBlockEntityType(), pos, blockState, deferredMachine, baseEnergyCapacity,
                PowerMultiplier.ONE);
        this.deferredMachine = deferredMachine;
    }

    @Override
    protected OverdriveMachineLogic<CookingPattern, SingleToSingleCache> createLogic() {
        return new OverdriveMachineLogic<>(this, getMainNode(), CookingPattern.class, CookingPattern[][]::new,
                CookingPattern[]::new, IActionSource.ofMachine(this));
    }

    @Override
    public @NotNull CookingPattern[] decodePattern(@Nullable GenericStack stack) {
        if (stack != null && stack.what() instanceof AEItemKey itemKey) {
            if (AEItems.PROCESSING_PATTERN.is(itemKey)) {
                EncodedProcessingPattern pattern = itemKey.get(AEComponents.ENCODED_PROCESSING_PATTERN);
                if (pattern == null) {
                    return new CookingPattern[] {};
                }
                ItemStack input = AEKeyUtils.findFirstItemStack(pattern.sparseInputs());
                if (hasRecipe(input)) {
                    return new CookingPattern[] {
                            new CookingPattern(itemKey, getRecipe(input), getLevel()),
                    };
                }
            } else if (hasRecipe(itemKey.getReadOnlyStack())) {
                return new CookingPattern[] {
                        new CookingPattern(itemKey, getRecipe(itemKey.getReadOnlyStack()), getLevel()),
                };
            }

        }
        return new CookingPattern[] {};
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
    public @NotNull SingleToSingleCache createCache(@NotNull CookingPattern pattern,
            @NotNull KeyCounter[] inputHolder) {
        return pattern.createCache(inputHolder[0], getLevel());
    }

    @Override
    public boolean matchesCache(@NotNull SingleToSingleCache cache, @NotNull KeyCounter[] inputHolder) {
        return cache.matchesInput(inputHolder[0]);
    }

    @Override
    public void consumeInputs(@NotNull CookingPattern pattern, @NotNull SingleToSingleCache cache, long times,
            KeyCounter[] inputHolder) {
        inputHolder[0].remove(cache.input().what(), times);
    }

    @Override
    public void addOutputsToHolder(@NotNull CookingPattern pattern, @NotNull SingleToSingleCache cache, long times,
            BigGenericHolder outputHolder) {
        outputHolder.add(cache.output().what(), times);
    }

    @Override
    protected double getEnergyPerProcess(CookingPattern pattern) {
        return AEConfig.instance().getVibrationChamberBaseEnergyPerFuelTick() * pattern.recipe.getCookingTime();
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
    public ItemStack getMainMenuIcon() {
        return new ItemStack(deferredMachine, 1);
    }

    @Override
    public Component getGuiTitle() {
        return hasCustomName() ? getCustomName() : deferredMachine.getBlock().getName();
    }

    @Override
    public InternalInventory getInternalInventory() {
        return InternalInventory.empty();
    }

}
