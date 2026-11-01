package com.takenokoshi.overdriveae.main.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.me.energy.StoredEnergyAmount;

@Mixin(value = { StoredEnergyAmount.class }, remap = false)
public class StoredEnergyAmountMixin {

    @Redirect(method = { "setMaximum(D)V" }, at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(DD)D"))
    private double overdriveae$removeMaximumLimit(double a, double b) {
        return b;
    }
}
