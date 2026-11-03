package com.takenokoshi.overdriveae.main.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = { "appeng.crafting.execution.ExecutingCraftingJob$TaskProgress", }, remap = false)
public interface TaskProgressMixin {

    @Accessor("value")
    long overdriveae$getValue();

    @Accessor("value")
    void overdriveae$setValue(long value);
}
