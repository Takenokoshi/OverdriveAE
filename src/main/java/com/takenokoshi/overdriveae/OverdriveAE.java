package com.takenokoshi.overdriveae;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.takenokoshi.overdriveae.registries.OAECapabilities;
import com.takenokoshi.overdriveae.registries.OAEMachines;
import com.takenokoshi.overdriveae.registries.OAEMenus;
import com.takenokoshi.overdriveae.registries.OAETabs;

import appeng.api.upgrades.Upgrades;
import jp.main.taikun.insaneae.quantum.QuantumCpuBlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import static jp.main.taikun.insaneae.registries.ModUpgrades.QUANTUM_ACCELERATION_CARD;
import static jp.main.taikun.insaneae.registries.ModUpgrades.TASK_FUSION_CARD;

@Mod(value = OverdriveAE.MODID,dist = Dist.DEDICATED_SERVER)
public class OverdriveAE {
    public static final String MODID = "overdriveae";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OverdriveAE(IEventBus modEventBus, ModContainer modContainer) {

        OAEMachines.MACHINES.register(modEventBus);
        OAETabs.TABS.register(modEventBus);
        OAEMenus.MENU_TYPES.register(modEventBus);


        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(OAECapabilities::register);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        OAEMachines.MACHINES.bind();

        Upgrades.add(QUANTUM_ACCELERATION_CARD.get(), OAEMachines.OVERDRIVE_CHARGER, QuantumCpuBlockEntity.MAX_ACCELERATION_CARDS);
        Upgrades.add(TASK_FUSION_CARD.get(), OAEMachines.OVERDRIVE_CHARGER, 1);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }
}
