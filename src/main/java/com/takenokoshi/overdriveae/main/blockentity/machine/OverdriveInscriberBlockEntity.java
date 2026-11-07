package com.takenokoshi.overdriveae.main.blockentity.machine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.main.blockentity.prefab.BigGenericHolder;
import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveConstantEnergyUsageBlockEntity;
import com.takenokoshi.overdriveae.main.menu.OverdriveInscriberMenu;
import com.takenokoshi.overdriveae.main.recipe.lookup.InscriberRecipeLookupHandler;
import com.takenokoshi.overdriveae.main.recipe.pattern.InscriberPattern;
import com.takenokoshi.overdriveae.main.recipe.patterncache.MultiToSingleCache;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEMachines;

import appeng.api.config.PowerMultiplier;
import appeng.api.ids.AEComponents;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedProcessingPattern;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import appeng.util.inv.filter.IAEItemFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveInscriberBlockEntity
        extends OverdriveConstantEnergyUsageBlockEntity<InscriberPattern, MultiToSingleCache>
        implements InscriberRecipeLookupHandler {

    public static final String NBT_CATALYST_INV = "catalystInv";

    public static final int CATALYST_INV_SLOTS = 45;

    private final CatalystInv catalystInv;

    public OverdriveInscriberBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine.getBlockEntityType(), pos, blockState, deferredMachine, () -> 10000.0D, () -> 2560000.0D,
                PowerMultiplier.CONFIG);
        this.catalystInv = new CatalystInv(this, this::isCatalystItem, logic::markAvailablePatternsDirty);
    }

    public AppEngInternalInventory getCatalystInv() {
        return catalystInv;
    }

    @Override
    protected OverdriveMachineLogic<InscriberPattern, MultiToSingleCache> createLogic() {
        return new OverdriveMachineLogic<>(this, getMainNode(), InscriberPattern.class, IActionSource.ofMachine(this));
    }

    /**
     * EncodedProcessingPattern.sparseInputs()は常にサイズ9のnull含むリスト
     */

    /** */

    @Override
    public @NotNull InscriberPattern[] decodePattern(@Nullable GenericStack stack) {
        if (stack == null) {
            return new InscriberPattern[] {};
        }
        if (stack.what() instanceof AEItemKey itemKey) {
            if (AEItems.PROCESSING_PATTERN.is(itemKey)) {
                EncodedProcessingPattern pattern = itemKey.get(AEComponents.ENCODED_PROCESSING_PATTERN);
                if (pattern == null) {
                    return new InscriberPattern[] {};
                }
                var inputs = pattern.sparseInputs();
                if (inputs.get(0) == null) {
                    return null;
                } else if (inputs.get(1) == null) {
                    if (inputs.get(0).what() instanceof AEItemKey encodedItemKey) {
                        InscriberRecipe recipe = getRecipeSingle(encodedItemKey.getReadOnlyStack());
                        return recipe == null
                                ? new InscriberPattern[] {}
                                : new InscriberPattern[] { new InscriberPattern(itemKey, recipe), };
                    }
                } else if (inputs.get(2) == null) {
                    if (inputs.get(0).what() instanceof AEItemKey firstItemKey
                            && inputs.get(1).what() instanceof AEItemKey secondItemKey) {
                        InscriberRecipe recipe = getRecipeDouble(firstItemKey.getReadOnlyStack(),
                                secondItemKey.getReadOnlyStack());
                        return recipe == null || !isRecipeProcessable(recipe)
                                ? new InscriberPattern[] {}
                                : new InscriberPattern[] { new InscriberPattern(itemKey, recipe), };
                    }
                } else {
                    if (inputs.get(0).what() instanceof AEItemKey topItemKey
                            && inputs.get(1).what() instanceof AEItemKey centerItemKey
                            && inputs.get(2).what() instanceof AEItemKey bottomItemKey) {
                        InscriberRecipe recipe = getRecipeTriple(topItemKey.getReadOnlyStack(),
                                centerItemKey.getReadOnlyStack(), bottomItemKey.getReadOnlyStack());
                        return recipe == null || !isRecipeProcessable(recipe)
                                ? new InscriberPattern[] {}
                                : new InscriberPattern[] { new InscriberPattern(itemKey, recipe), };
                    }
                }
            } else if (hasRecipeSingle(itemKey.getReadOnlyStack())) {
                return new InscriberPattern[] {
                        new InscriberPattern(itemKey, getRecipeSingle(itemKey.getReadOnlyStack())),
                };
            }
        }
        return new InscriberPattern[] {};
    }

    @Override
    public boolean isPatternAcceptable(@Nullable AEKey key) {
        if (key == null || !(key instanceof AEItemKey itemKey)) {
            return false;
        }
        if (AEItems.PROCESSING_PATTERN.is(itemKey)) {
            EncodedProcessingPattern pattern = itemKey.get(AEComponents.ENCODED_PROCESSING_PATTERN);
            if (pattern == null) {
                return false;
            }
            var inputs = pattern.sparseInputs();
            if (inputs.get(0) == null) {
                return false;
            } else if (inputs.get(1) == null) {
                if (inputs.get(0).what() instanceof AEItemKey encodedItemKey) {
                    return hasRecipeSingle(encodedItemKey.getReadOnlyStack());
                }
            } else if (inputs.get(2) == null) {
                if (inputs.get(0).what() instanceof AEItemKey firstItemKey
                        && inputs.get(1).what() instanceof AEItemKey secondItemKey) {
                    return hasRecipeDouble(firstItemKey.getReadOnlyStack(), secondItemKey.getReadOnlyStack());
                }
            } else {
                if (inputs.get(0).what() instanceof AEItemKey topItemKey
                        && inputs.get(1).what() instanceof AEItemKey centerItemKey
                        && inputs.get(2).what() instanceof AEItemKey bottomItemKey) {
                    return hasRecipeTriple(topItemKey.getReadOnlyStack(), centerItemKey.getReadOnlyStack(),
                            bottomItemKey.getReadOnlyStack());
                }
            }
            return false;
        } else {
            return hasRecipeSingle(itemKey.getReadOnlyStack());
        }
    }

    @Override
    public Set<ItemStack> getAvailableCatalysts() {
        Set<ItemStack> result = new HashSet<>();
        for (ItemStack stack : catalystInv) {
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result;
    }

    @Override
    public @NotNull MultiToSingleCache createCache(@NotNull InscriberPattern pattern,
            @NotNull KeyCounter[] inputHolder) {
        return pattern.createCache(inputHolder);
    }

    @Override
    public boolean matchesCache(@NotNull MultiToSingleCache cache, @NotNull KeyCounter[] inputHolder) {
        return cache.matchesInput(inputHolder);
    }

    @Override
    public void consumeInputs(@NotNull InscriberPattern pattern, @NotNull MultiToSingleCache cache, long times,
            KeyCounter[] inputHolder) {
        for (int i = 0; i < cache.inputs().length; i++) {
            inputHolder[i].remove(cache.inputs()[i].what(), times);
        }
    }

    @Override
    public void addOutputsToHolder(@NotNull InscriberPattern pattern, @NotNull MultiToSingleCache cache, long times,
            BigGenericHolder outputHolder) {
        outputHolder.add(pattern.output.what(), pattern.output.amount() * times);
    }

    @Override
    public Component getGuiTitle() {
        return hasCustomName() ? getCustomName() : OverdriveAEMachines.OVERDRIVE_INSCRIBER.getBlock().getName();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(OverdriveAEMachines.OVERDRIVE_INSCRIBER, 1);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(OverdriveInscriberMenu.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(OverdriveInscriberMenu.TYPE, player, subMenu.getLocator());
    }

    @Override
    public InternalInventory getInternalInventory() {
        return catalystInv;
    }

    @Override
    public void saveAdditional(CompoundTag data, Provider registries) {
        super.saveAdditional(data, registries);
        catalystInv.writeToNBT(data, NBT_CATALYST_INV, registries);
    }

    @Override
    public void loadTag(CompoundTag data, Provider registries) {
        super.loadTag(data, registries);
        catalystInv.readFromNBT(data, NBT_CATALYST_INV, registries);
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        catalystInv.addDrops(drops);
    }

    private static class CatalystInv extends AppEngInternalInventory {

        private final Runnable onChange;

        public CatalystInv(InternalInventoryHost host, Predicate<ItemStack> filter, Runnable onChange) {
            super(host, CATALYST_INV_SLOTS, 1,new IAEItemFilter() {
                @Override
                public boolean allowInsert(InternalInventory inv, int slot, ItemStack stack) {
                    return  filter.test(stack);
                }
            });
            this.onChange = onChange;
        }

        @Override
        public void setItemDirect(int slot, ItemStack stack) {
            super.setItemDirect(slot, stack);
            onChange.run();
        }

        public void addDrops(List<ItemStack> drops) {
            for (ItemStack itemStack : this) {
                if (!itemStack.isEmpty()) {
                    drops.add(itemStack);
                }
            }
        }

    }

}
