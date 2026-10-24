package com.takenokoshi.overdriveae.main.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.google.common.collect.ArrayListMultimap;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantic;
import net.minecraft.world.inventory.Slot;

@Mixin(value = { AEBaseMenu.class }, remap = false)
public interface AEBaseMenuMixin {
    @Accessor("semanticBySlot")
    Map<Slot, SlotSemantic> overdriveae$getSemanticBySlot();

    @Accessor("slotsBySemantic")
    ArrayListMultimap<SlotSemantic, Slot> overdriveae$getSlotsBySemantic();
}
