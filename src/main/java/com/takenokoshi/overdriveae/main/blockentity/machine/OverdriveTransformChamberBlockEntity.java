package com.takenokoshi.overdriveae.main.blockentity.machine;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.main.blockentity.prefab.BigGenericHolder;
import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveMachineBlockEntity;
import com.takenokoshi.overdriveae.main.menu.OverdriveTransformChamberMenu;
import com.takenokoshi.overdriveae.main.recipe.lookup.TransformRecipeLookupHandler;
import com.takenokoshi.overdriveae.main.recipe.pattern.TransformPattern;
import com.takenokoshi.overdriveae.main.recipe.patterncache.MultiToSingleCache;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEMachines;
import com.takenokoshi.overdriveae.main.util.AEKeyUtils;

import appeng.api.ids.AEComponents;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedProcessingPattern;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.recipes.transform.TransformRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class OverdriveTransformChamberBlockEntity extends
        OverdriveMachineBlockEntity<TransformPattern, MultiToSingleCache>
        implements TransformRecipeLookupHandler {

    public static final String NBT_CATALYST_INV = "catalystInv";

    public static final int CATALYST_INV_SLOTS = 45;

    private final GenericStackInv catalystInv;

    private final Set<Fluid> availableCatalysts = new HashSet<>();

    public OverdriveTransformChamberBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine.getBlockEntityType(), pos, blockState, deferredMachine);
        this.catalystInv = new GenericStackInv(Set.of(AEKeyType.fluids()), this::updateAvailableCatalysts,
                GenericStackInv.Mode.STORAGE,
                CATALYST_INV_SLOTS);
        this.catalystInv.setCapacity(AEKeyType.fluids(), 1_000L);
    }

    @Override
    public Set<Fluid> getAvailableCatalysts() {
        return availableCatalysts;
    }

    private void updateAvailableCatalysts() {
        availableCatalysts.clear();
        for (int i = 0; i < CATALYST_INV_SLOTS; i++) {
            var stack = catalystInv.getStack(i);
            if (stack != null && stack.amount() >= 1_000L && stack.what() instanceof AEFluidKey key) {
                availableCatalysts.add(key.getFluid());
            }
        }
        logic.markAvailablePatternsDirty();
    }

    public GenericStackInv getCatalystInv() {
        return catalystInv;
    }

    @Override
    public void onReady() {
        super.onReady();
        updateAvailableCatalysts();
    }

    @Override
    protected OverdriveMachineLogic<TransformPattern, MultiToSingleCache> createLogic() {
        return new OverdriveMachineLogic<>(this, getMainNode(), TransformPattern.class, IActionSource.ofMachine(this));
    }

    @Override
    public @NotNull TransformPattern[] decodePattern(@Nullable GenericStack stack) {
        if (stack == null) {
            return new TransformPattern[] {};
        }
        return stack.what() instanceof AEItemKey itemKey ? decodePattern(itemKey) : new TransformPattern[] {};
    }

    public @NotNull TransformPattern[] decodePattern(AEItemKey itemKey) {
        if (AEItems.PROCESSING_PATTERN.is(itemKey)) {
            EncodedProcessingPattern pattern = itemKey.get(AEComponents.ENCODED_PROCESSING_PATTERN);
            if (pattern == null) {
                return new TransformPattern[] {};
            }
            TransformRecipe recipe = getRecipe(
                    AEKeyUtils.filterAndMapToItemStack(pattern.sparseInputs()),
                    AEKeyUtils.findFirstFluidStack(pattern.sparseInputs()).getFluid());
            return recipe == null || !isRecipeProcessable(recipe) ? new TransformPattern[] {}
                    : new TransformPattern[] {
                            new TransformPattern(itemKey, recipe),
                    };
        }
        return new TransformPattern[] {};
    }

    @Override
    public boolean isPatternAcceptable(@Nullable AEKey key) {
        if (key == null) {
            return false;
        }
        return key instanceof AEItemKey itemKey ? isPatternAcceptable(itemKey) : false;
    }

    public boolean isPatternAcceptable(AEItemKey itemKey) {
        if (AEItems.PROCESSING_PATTERN.is(itemKey)) {
            EncodedProcessingPattern pattern = itemKey.get(AEComponents.ENCODED_PROCESSING_PATTERN);
            if (pattern == null) {
                return false;
            }
            List<AEKey> keys = pattern.sparseInputs().stream().filter(Objects::nonNull).map(GenericStack::what)
                    .toList();
            return hasRecipe(
                    keys.stream()
                            .filter(AEItemKey.class::isInstance)
                            .map(v -> AEItemKey.class.cast(v).getReadOnlyStack())
                            .toArray(ItemStack[]::new),
                    keys.stream()
                            .filter(AEFluidKey.class::isInstance)
                            .findFirst()
                            .map(v -> AEFluidKey.class.cast(v).getFluid())
                            .orElse(null));
        }
        return false;
    }

    @Override
    public @NotNull MultiToSingleCache createCache(@NotNull TransformPattern pattern,
            @NotNull KeyCounter[] inputHolder) {
        return pattern.createCache(inputHolder);
    }

    @Override
    public boolean matchesCache(@NotNull MultiToSingleCache cache, @NotNull KeyCounter[] inputHolder) {
        return cache.matchesInput(inputHolder);
    }

    @Override
    public void consumeInputs(@NotNull TransformPattern pattern, @NotNull MultiToSingleCache cache, long times,
            KeyCounter[] inputHolder) {
        for (int i = 0; i < cache.inputs().length; i++) {
            inputHolder[i].remove(cache.inputs()[i].what(), times);
        }
    }

    @Override
    public void addOutputsToHolder(@NotNull TransformPattern pattern, @NotNull MultiToSingleCache cache, long times,
            BigGenericHolder outputHolder) {
        outputHolder.add(pattern.output.what(), pattern.output.amount() * times);
    }

    @Override
    public void saveAdditional(CompoundTag data, Provider registries) {
        super.saveAdditional(data, registries);
        catalystInv.writeToChildTag(data, NBT_CATALYST_INV, registries);
    }

    @Override
    public void loadTag(CompoundTag data, Provider registries) {
        super.loadTag(data, registries);
        catalystInv.readFromChildTag(data, NBT_CATALYST_INV, registries);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(OverdriveTransformChamberMenu.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(OverdriveTransformChamberMenu.TYPE, player, subMenu.getLocator());
    }

    @Override
    public Component getGuiTitle() {
        return hasCustomName() ? getCustomName() : OverdriveAEMachines.OVERDRIVE_TRANSFORM_CHAMBER.getBlock().getName();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(OverdriveAEMachines.OVERDRIVE_TRANSFORM_CHAMBER, 1);
    }

}
