package com.takenokoshi.overdriveae.main;

import com.takenokoshi.overdriveae.main.config.OverdriveAEConfig;

import net.neoforged.fml.ModList;

public class OverdriveAEHook {

    public static boolean shouldLoadAdvancedAEModule() {
        return ModList.get().isLoaded("advanced_ae")
                ? OverdriveAEConfig.instance().startupConfig.advancedAEIntegration.getAsBoolean()
                : false;
    }

    public static boolean shouldLoadExtendedAEModule() {
        return ModList.get().isLoaded("extendedae")
                ? OverdriveAEConfig.instance().startupConfig.extendedAEIntegration.getAsBoolean()
                : false;
    }
}
