package com.takenokoshi.overdriveae.main.blockentity.machine;

import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveCookingBlockEntity;
import com.takenokoshi.overdriveae.main.recipe.RecipeCacheHolder;
import com.takenokoshi.overdriveae.main.recipe.pattern.CookingPattern;
import com.takenokoshi.overdriveae.main.recipe.recipecache.CookingRecipeCache;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;

import appeng.core.AEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveEnergizedSmokerBlockEntity extends OverdriveCookingBlockEntity<SmokingRecipe> {

    public OverdriveEnergizedSmokerBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine, pos, blockState,
                () -> AEConfig.instance().getVibrationChamberBaseEnergyPerFuelTick() * 512_000.0D);
    }

    @Override
    public RecipeCacheHolder<?, SmokingRecipe, CookingRecipeCache<SmokingRecipe>> getCacheHolder() {
        return RecipeCacheHolder.SMOKING;
    }

    @Override
    protected double getEnergyPerProcess(CookingPattern pattern) {
        return super.getEnergyPerProcess(pattern) * 2.0D;
    }

}
