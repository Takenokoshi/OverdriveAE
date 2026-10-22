package com.takenokoshi.overdriveae.main.blockentity.machine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.main.blockentity.logic.OverdrivePatternInv;
import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveMachineBlockEntity;
import com.takenokoshi.overdriveae.main.menu.OverdriveInscriberMenu;
import com.takenokoshi.overdriveae.main.pattern.InscriberPattern;
import com.takenokoshi.overdriveae.main.pattern.SingleOutputAssembly;
import com.takenokoshi.overdriveae.main.recipe.lookup.InscriberRecipeLookupHandler;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;

import appeng.api.config.PowerMultiplier;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveInscriberBlockEntity
        extends OverdriveMachineBlockEntity<SingleOutputAssembly, InscriberPattern, OverdriveInscriberBlockEntity>
        implements InscriberRecipeLookupHandler {

    public static final String NBT_CATALYST_INV = "catalystInv";

    public static final int CATALYST_INV_SLOTS = 45;

    private final CatalystInv catalystInv;

    public OverdriveInscriberBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine, pos, blockState, 0);
        this.catalystInv = new CatalystInv(this, this::isCatalystItem, logic::markShouldDecodedPatternAgain);
    }

    public AppEngInternalInventory getCatalystInv() {
        return catalystInv;
    }

    @Override
    protected OverdriveMachineLogic<SingleOutputAssembly, InscriberPattern, OverdriveInscriberBlockEntity> createLogic() {
        return new OverdriveMachineLogic<SingleOutputAssembly, InscriberPattern, OverdriveInscriberBlockEntity>(
                getMainNode(), this, InscriberPattern[]::new, InscriberPattern.class);
    }

    /**
     * EncodedProcessingPattern.sparseInputs()は常にサイズ9のnull含むリスト
     */

    /** */
    @Override
    public @Nullable InscriberPattern tryDecodePattern(ItemStack itemStack) {
        if (AEItems.PROCESSING_PATTERN.is(itemStack)) {
            EncodedProcessingPattern pattern = itemStack.get(AEComponents.ENCODED_PROCESSING_PATTERN);
            if (pattern == null) {
                return null;
            }
            var inputs = pattern.sparseInputs();
            if (inputs.get(0) == null) {
                return null;
            } else if (inputs.get(1) == null) {
                if (inputs.get(0).what() instanceof AEItemKey itemKey) {
                    InscriberRecipe recipe = getRecipeSingle(itemKey.getReadOnlyStack());
                    return recipe == null ? null
                            : InscriberPattern.fromRecipe(AEItemKey.of(itemStack), recipe);
                }
            } else if (inputs.get(2) == null) {
                if (inputs.get(0).what() instanceof AEItemKey firstItemKey
                        && inputs.get(1).what() instanceof AEItemKey secondItemKey) {
                    InscriberRecipe recipe = getRecipeDouble(firstItemKey.getReadOnlyStack(),
                            secondItemKey.getReadOnlyStack());
                    return recipe == null || !isRecipeProcessable(recipe)
                            ? null
                            : InscriberPattern.fromRecipe(AEItemKey.of(itemStack), recipe);
                }
            } else {
                if (inputs.get(0).what() instanceof AEItemKey topItemKey
                        && inputs.get(1).what() instanceof AEItemKey centerItemKey
                        && inputs.get(2).what() instanceof AEItemKey bottomItemKey) {
                    InscriberRecipe recipe = getRecipeTriple(topItemKey.getReadOnlyStack(),
                            centerItemKey.getReadOnlyStack(), bottomItemKey.getReadOnlyStack());
                    return recipe == null || !isRecipeProcessable(recipe)
                            ? null
                            : InscriberPattern.fromRecipe(AEItemKey.of(itemStack), recipe);
                }
            }
            return null;
        }
        InscriberRecipe recipe = getRecipeSingle(itemStack);
        return recipe == null ? null : InscriberPattern.fromRecipe(AEItemKey.of(itemStack), recipe);
    }

    @Override
    public boolean isPatternAcceptable(ItemStack itemStack) {
        if (AEItems.PROCESSING_PATTERN.is(itemStack)) {
            EncodedProcessingPattern pattern = itemStack.get(AEComponents.ENCODED_PROCESSING_PATTERN);
            if (pattern == null) {
                return false;
            }
            var inputs = pattern.sparseInputs();
            if (inputs.get(0) == null) {
                return false;
            } else if (inputs.get(1) == null) {
                if (inputs.get(0).what() instanceof AEItemKey itemKey) {
                    return hasRecipeSingle(itemKey.getReadOnlyStack());
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
        }
        return hasRecipeSingle(itemStack);
    }

    @Override
    public SingleOutputAssembly assemble(InscriberPattern pattern, KeyCounter[] inputHolder) {
        return new SingleOutputAssembly(Arrays.stream(inputHolder)
                .map(counter -> new GenericStack(counter.getFirstKey(), 1)).toArray(GenericStack[]::new),
                pattern.output);
    }

    @Override
    public boolean matchesCache(SingleOutputAssembly assembly, KeyCounter[] inputHolder) {
        if (assembly.inputs.length != inputHolder.length) {
            return false;
        }
        for (int i = 0; i < inputHolder.length; i++) {
            if (!assembly.inputs[i].what().equals(inputHolder[i].getFirstKey())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public double getEnergyPerProcess(InscriberPattern pattern) {
        return PowerMultiplier.CONFIG.multiply(10000.0D);
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
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(OverdriveInscriberMenu.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(OverdriveInscriberMenu.TYPE, player, subMenu.getLocator());
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        catalystInv.addDrops(drops);
    }

    private static class CatalystInv extends AppEngInternalInventory {

        private final Runnable onChange;

        public CatalystInv(InternalInventoryHost host, Predicate<ItemStack> filter, Runnable onChange) {
            super(host, CATALYST_INV_SLOTS, 1, new OverdrivePatternInv.OverdrivePatternInvFilter(filter));
            this.onChange = onChange;
        }

        @Override
        public void setItemDirect(int slot, ItemStack stack) {
            super.setItemDirect(slot, stack);
            onChange.run();
        }

        public void addDrops(List<ItemStack> drops){
            for (ItemStack itemStack : this) {
                if (!itemStack.isEmpty()) {
                    drops.add(itemStack);
                }
            }
        }

    }

}
