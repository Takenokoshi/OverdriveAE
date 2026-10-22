package com.takenokoshi.overdriveae.main.blockentity.machine;

import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveCookingBlockEntity;
import com.takenokoshi.overdriveae.main.recipe.RecipeCacheHolder;
import com.takenokoshi.overdriveae.main.recipe.cache.CookingRecipeCache;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;

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
