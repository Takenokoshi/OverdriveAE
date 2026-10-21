package com.takenokoshi.overdriveae.blockentity.machine;

import com.takenokoshi.overdriveae.blockentity.prefab.OverdriveCookingBlockEntity;
import com.takenokoshi.overdriveae.recipe.RecipeCacheHolder;
import com.takenokoshi.overdriveae.recipe.cache.CookingRecipeCache;
import com.takenokoshi.overdriveae.registration.OverdriveAEDeferredMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.state.BlockState;

public class OverdriveEnergizedFurnaceBlockEntity extends OverdriveCookingBlockEntity<SmeltingRecipe> {

    public OverdriveEnergizedFurnaceBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos,
            BlockState blockState) {
        super(deferredMachine, pos, blockState);
    }

    @Override
    public RecipeCacheHolder<?, SmeltingRecipe, CookingRecipeCache<SmeltingRecipe>> getCacheHolder() {
        return RecipeCacheHolder.SMELTING;
    }
    
}
