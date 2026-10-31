package com.takenokoshi.overdriveae.main.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;

import appeng.api.networking.energy.IEnergyService;
import appeng.crafting.execution.CraftingCpuLogic;
import appeng.crafting.execution.ExecutingCraftingJob;
import appeng.crafting.execution.OverdriveAE$LargeCraftingExecuter;
import appeng.crafting.inv.ListCraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;

@Mixin(value = { CraftingCpuLogic.class }, remap = false)
public abstract class CraftingCpuLogicMixin {

    @Shadow
    @Final
    CraftingCPUCluster cluster;

    @Shadow
    private ExecutingCraftingJob job;

    @Shadow
    @Final
    private ListCraftingInventory inventory;

    @Shadow
    public abstract boolean isJobSuspended();

    @Inject(
            /**
             * 対象メソッドがexecuteCraftingではなくtickCraftingLogicなのは、
             * OverdriveAE及びそのアドオンの機械によって多くの処理を高速化するにあたってjobの中身を先に減らした方がいいと判断した為
             */
            method = {
                    "tickCraftingLogic(Lappeng/api/networking/energy/IEnergyService;Lappeng/me/service/CraftingService;)V"
            },
            // ローカル変数startedにremainingOperationsが保存された直後を狙って実行している
            at = {
                    @At(value = "STORE", opcode = Opcodes.ISTORE, ordinal = 1, shift = At.Shift.AFTER)
            })
    private void overdriveae$executeMachines(
            IEnergyService eg,
            CraftingService cc,
            CallbackInfo ci,
            @Local(index = 3) LocalIntRef remainingOperations) {
        /**
         * ここに到達した時点で
         * cluster.isActive()はtrueであり、
         * jobはnullではなく
         * job.link.isCanceled()とjob.suspendedはfalseである
         */
        int v = remainingOperations.get();
        remainingOperations
                .set(v - OverdriveAE$LargeCraftingExecuter.executeLargeCrafting(v, cluster, cc, eg, job, inventory));
    }
}
