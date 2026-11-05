package com.takenokoshi.overdriveae.main.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class OverdriveAEStartupConfig implements IOverdriveAEConfig {

    public final ModConfigSpec.BooleanValue advancedAEIntegration;
    public final ModConfigSpec.BooleanValue extendedAEIntegration;

    private final ModConfigSpec spec;

    public OverdriveAEStartupConfig() {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        /**
         * AdvancedAEとExtendedAEが追加する加工手段はそれぞれ1つ/2つであり、
         * 個別の連携アドオンを作るほどでは無いのでOverdriveAE本体に連携要素を内包する。
         * AdvancedAE/ExtendedAE側の更新で連携が壊れてもコンフィグを切り替えれば
         * 連携モジュールが読み込まれなくなるだけでOverdriveAE本体に影響は無い。
         */
        builder.push("addon_compatibility");
        this.advancedAEIntegration = builder
                .comment("Enables compatibility features for AdvancedAE.")
                .define("advancedAEIntegration", true);
        this.extendedAEIntegration = builder
                .comment("Enables compatibility features for ExtendedAE.")
                .define("extendedAEIntegration", true);
        builder.pop();

        this.spec = builder.build();
    }

    @Override
    public ModConfigSpec getSpec() {
        return spec;
    }

    @Override
    public ModConfig.Type getType() {
        return ModConfig.Type.STARTUP;
    }
}
