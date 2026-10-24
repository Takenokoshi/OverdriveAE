package com.takenokoshi.overdriveae.main.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

@Mixin(value = { AbstractContainerMenu.class }, remap = true)
public interface AbstractContainerMenuMixin {
    @Accessor("lastSlots")
    NonNullList<ItemStack> overdriveae$getLastSlots();

    @Accessor("remoteSlots")
    NonNullList<ItemStack> overdriveae$getRemoteSlots();
}
