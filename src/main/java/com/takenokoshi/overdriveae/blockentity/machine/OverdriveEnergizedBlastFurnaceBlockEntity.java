package com.takenokoshi.overdriveae.blockentity.machine;

import com.takenokoshi.overdriveae.blockentity.prefab.OverdriveCookingBlockEntity;
import com.takenokoshi.overdriveae.recipe.RecipeCacheHolder;
import com.takenokoshi.overdriveae.recipe.cache.CookingRecipeCache;
import com.takenokoshi.overdriveae.registration.OverdriveAEDeferredMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveEnergizedBlastFurnaceBlockEntity extends OverdriveCookingBlockEntity<BlastingRecipe> {

    public OverdriveEnergizedBlastFurnaceBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine, pos, blockState);
    }

    @Override
    public RecipeCacheHolder<?, BlastingRecipe, CookingRecipeCache<BlastingRecipe>> getCacheHolder() {
        return RecipeCacheHolder.BLASTING;
    }
    
}
