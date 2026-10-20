package com.takenokoshi.overdriveae.blockentity.machine;

import com.takenokoshi.overdriveae.blockentity.prefab.OverdriveCookingBlockEntity;
import com.takenokoshi.overdriveae.recipe.RecipeCacheHolder;
import com.takenokoshi.overdriveae.recipe.cache.CookingRecipeCache;
import com.takenokoshi.overdriveae.registration.OAEDeferredMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveEnergizedSmokerBlockEntity extends OverdriveCookingBlockEntity<SmokingRecipe> {

    public OverdriveEnergizedSmokerBlockEntity(OAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine, pos, blockState);
    }

    @Override
    public RecipeCacheHolder<?, SmokingRecipe, CookingRecipeCache<SmokingRecipe>> getCacheHolder() {
        return RecipeCacheHolder.SMOKING;
    }
    
}
