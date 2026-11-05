package com.takenokoshi.overdriveae.main.config;

import com.takenokoshi.overdriveae.main.OverdriveAE;

import net.neoforged.fml.ModContainer;

public class OverdriveAEConfig {

    private static OverdriveAEConfig instance;

    public static void register(ModContainer container) {
        if (container.getModId().equals(OverdriveAE.MODID)) {
            instance = new OverdriveAEConfig(container);
        }
    }

    public static OverdriveAEConfig instance() {
        return instance;
    }

    public final OverdriveAEStartupConfig startupConfig;

    private OverdriveAEConfig(ModContainer container) {
        this.startupConfig = new OverdriveAEStartupConfig();
        this.startupConfig.register(container);
    }
}
