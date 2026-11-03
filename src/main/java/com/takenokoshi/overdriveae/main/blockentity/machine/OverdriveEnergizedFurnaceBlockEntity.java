package com.takenokoshi.overdriveae.main.blockentity.machine;

import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveCookingBlockEntity;
import com.takenokoshi.overdriveae.main.recipe.RecipeCacheHolder;
import com.takenokoshi.overdriveae.main.recipe.recipecache.CookingRecipeCache;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;

import appeng.api.inventories.InternalInventory;
import appeng.core.AEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveEnergizedFurnaceBlockEntity extends OverdriveCookingBlockEntity<SmeltingRecipe> {

    public OverdriveEnergizedFurnaceBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine, pos, blockState,
                () -> AEConfig.instance().getVibrationChamberBaseEnergyPerFuelTick() * 512_000.0D);
    }

    @Override
    public RecipeCacheHolder<?, SmeltingRecipe, CookingRecipeCache<SmeltingRecipe>> getCacheHolder() {
        return RecipeCacheHolder.SMELTING;
    }

    @Override
    public InternalInventory getInternalInventory() {
        return InternalInventory.empty();
    }

}
