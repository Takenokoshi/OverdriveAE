package com.takenokoshi.overdriveae.main.blockentity.logic;

import java.util.Set;

import com.takenokoshi.overdriveae.main.mixin.ExecutingCraftingJobMixin;
import com.takenokoshi.overdriveae.main.mixin.TaskProgressMixin;

import appeng.api.crafting.IPatternDetails;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.crafting.inv.ICraftingInventory;

public class ExecutingCraftingJobBridge {
    public static IJob getJob(ExecutingCraftingJob job) {
        ExecutingCraftingJobMixin jobMixin = (ExecutingCraftingJobMixin) job;
        return new IJob() {

            @Override
            public Set<IPatternDetails> getTasks() {
                return jobMixin.overdriveae$getTasks().keySet();
            }

            @Override
            public long getTaskProgress(IPatternDetails patternDetails) {
                return jobMixin.overdriveae$getTasks().get(patternDetails) instanceof TaskProgressMixin progressMixin
                        ? progressMixin.overdriveae$getValue()
                        : 0L;
            }

            @Override
            public void setTaskProgress(IPatternDetails patternDetails, long newProgress) {
                if (jobMixin.overdriveae$getTasks().get(patternDetails) instanceof TaskProgressMixin progressMixin) {
                    progressMixin.overdriveae$setValue(newProgress);
                }
            }

            @Override
            public ICraftingInventory getWaitingInv() {
                return jobMixin.overdriveae$getWaitingFor();
            }

        };
    }
}
