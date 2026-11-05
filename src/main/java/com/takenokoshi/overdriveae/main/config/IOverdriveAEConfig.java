package com.takenokoshi.overdriveae.main.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public interface IOverdriveAEConfig {

    ModConfig.Type getType();

    ModConfigSpec getSpec();

    default void register(ModContainer container) {
        container.registerConfig(getType(), getSpec());
    }
}
