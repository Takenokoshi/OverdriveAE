package com.takenokoshi.overdriveae.main.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.api.crafting.IPatternDetails;
import appeng.crafting.execution.ElapsedTimeTracker;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.crafting.inv.ListCraftingInventory;

@Mixin(value = { ExecutingCraftingJob.class, }, remap = false)
public interface ExecutingCraftingJobMixin {
    @Accessor("tasks")
    Map<IPatternDetails, ?> overdriveae$getTasks();

    @Accessor("waitingFor")
    ListCraftingInventory overdriveae$getWaitingFor();

    @Accessor("timeTracker")
    ElapsedTimeTracker overdriveae$getTimeTracker();
}
