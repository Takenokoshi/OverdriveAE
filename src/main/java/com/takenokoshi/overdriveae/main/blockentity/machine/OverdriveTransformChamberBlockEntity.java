package com.takenokoshi.overdriveae.main.blockentity.machine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveMachineBlockEntity;
import com.takenokoshi.overdriveae.main.menu.OverdriveTransformChamberMenu;
import com.takenokoshi.overdriveae.main.pattern.MultiToSinglePattern;
import com.takenokoshi.overdriveae.main.pattern.SingleOutputAssembly;
import com.takenokoshi.overdriveae.main.recipe.lookup.TransformRecipeLookupHandler;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;

import appeng.api.ids.AEComponents;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

public class OverdriveTransformChamberBlockEntity extends
        OverdriveMachineBlockEntity<SingleOutputAssembly, MultiToSinglePattern, OverdriveTransformChamberBlockEntity>
        implements TransformRecipeLookupHandler {

    public static final String NBT_CATALYST_INV = "catalystInv";

    public static final int CATALYST_INV_SLOTS = 45;

    private final GenericStackInv catalystInv;

    private final Set<Fluid> availableCatalysts = new HashSet<>();

    public OverdriveTransformChamberBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine, pos, blockState);
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
        logic.markShouldDecodePatternAgain();
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
    protected OverdriveMachineLogic<SingleOutputAssembly, MultiToSinglePattern, OverdriveTransformChamberBlockEntity> createLogic() {
        return new OverdriveMachineLogic<>(getMainNode(), this, MultiToSinglePattern[]::new,
                MultiToSinglePattern.class);
    }

    @Override
    public @Nullable MultiToSinglePattern decodePattern(ItemStack itemStack) {
        if (AEItems.PROCESSING_PATTERN.is(itemStack)) {
            EncodedProcessingPattern pattern = itemStack.get(AEComponents.ENCODED_PROCESSING_PATTERN);
            if (pattern == null) {
                return null;
            }
            List<AEKey> keys = pattern.sparseInputs().stream().filter(Objects::nonNull).map(GenericStack::what)
                    .toList();
            TransformRecipe recipe = getRecipe(
                    keys.stream()
                            .filter(AEItemKey.class::isInstance)
                            .map(v -> AEItemKey.class.cast(v).getReadOnlyStack())
                            .toArray(ItemStack[]::new),
                    keys.stream()
                            .filter(AEFluidKey.class::isInstance)
                            .findFirst()
                            .map(v -> AEFluidKey.class.cast(v).getFluid())
                            .orElse(null));
            return recipe == null || !isRecipeProcessable(recipe) ? null
                    : MultiToSinglePattern.fromTransformRecipe(AEItemKey.of(itemStack), recipe);
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
    public SingleOutputAssembly assemble(MultiToSinglePattern pattern, KeyCounter[] inputHolder) {
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
    public double getEnergyPerProcess(MultiToSinglePattern pattern) {
        return 0.0D;
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

}
