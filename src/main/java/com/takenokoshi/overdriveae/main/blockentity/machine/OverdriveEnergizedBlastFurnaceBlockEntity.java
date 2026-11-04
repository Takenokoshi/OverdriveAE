package com.takenokoshi.overdriveae.main.blockentity.machine;

import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveCookingBlockEntity;
import com.takenokoshi.overdriveae.main.recipe.RecipeCacheHolder;
import com.takenokoshi.overdriveae.main.recipe.pattern.CookingPattern;
import com.takenokoshi.overdriveae.main.recipe.recipecache.CookingRecipeCache;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;

import appeng.core.AEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveEnergizedBlastFurnaceBlockEntity extends OverdriveCookingBlockEntity<BlastingRecipe> {

    public OverdriveEnergizedBlastFurnaceBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine, pos, blockState,
                () -> AEConfig.instance().getVibrationChamberBaseEnergyPerFuelTick() * 512_000.0D);
    }

    @Override
    public RecipeCacheHolder<?, BlastingRecipe, CookingRecipeCache<BlastingRecipe>> getCacheHolder() {
        return RecipeCacheHolder.BLASTING;
    }

    @Override
    protected double getEnergyPerProcess(CookingPattern pattern) {
        return super.getEnergyPerProcess(pattern) * 2.0D;
    }

}
