package com.takenokoshi.overdriveae.main.menu;

import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.takenokoshi.overdriveae.main.mixin.AEBaseMenuMixin;
import com.takenokoshi.overdriveae.main.mixin.AbstractContainerMenuMixin;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AEBaseMenuBridge {
    public static Map<Slot, SlotSemantic> getSemanticBySlot(AEBaseMenu aeBaseMenu) {
        return ((AEBaseMenuMixin) aeBaseMenu).overdriveae$getSemanticBySlot();
    }

    public static ArrayListMultimap<SlotSemantic, Slot> getSlotsBySemantic(AEBaseMenu aeBaseMenu) {
        return ((AEBaseMenuMixin) aeBaseMenu).overdriveae$getSlotsBySemantic();
    }

    public static NonNullList<ItemStack> getLastSlots(AbstractContainerMenu abstractContainerMenu) {
        return ((AbstractContainerMenuMixin) abstractContainerMenu).overdriveae$getLastSlots();
    }

    public static NonNullList<ItemStack> getRemoteSlots(AbstractContainerMenu abstractContainerMenu) {
        return ((AbstractContainerMenuMixin) abstractContainerMenu).overdriveae$getRemoteSlots();
    }
}
