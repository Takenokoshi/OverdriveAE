package com.takenokoshi.overdriveae;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.takenokoshi.overdriveae.recipe.RecipeCacheReloadLister;
import com.takenokoshi.overdriveae.registration.OverdriveAEDeferredMachine;
import com.takenokoshi.overdriveae.registries.OverdriveAECapabilities;
import com.takenokoshi.overdriveae.registries.OverdriveAEMachines;
import com.takenokoshi.overdriveae.registries.OverdriveAEMenus;
import com.takenokoshi.overdriveae.registries.OverdriveAETabs;

import appeng.api.upgrades.Upgrades;
import jp.main.taikun.insaneae.InsaneAE;
import jp.main.taikun.insaneae.quantum.QuantumCpuBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import static jp.main.taikun.insaneae.registries.ModUpgrades.QUANTUM_ACCELERATION_CARD;
import static jp.main.taikun.insaneae.registries.ModUpgrades.TASK_FUSION_CARD;

import java.util.List;

@Mod(value = OverdriveAE.MODID, dist = Dist.DEDICATED_SERVER)
public class OverdriveAE {
    public static final String MODID = "overdriveae";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation makeId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static ResourceLocation makeInsaneId(String path){
        return  ResourceLocation.fromNamespaceAndPath(InsaneAE.MODID, path);
    }

    public OverdriveAE(IEventBus modEventBus, ModContainer modContainer) {

        OverdriveAEMachines.MACHINES.register(modEventBus);
        OverdriveAETabs.TABS.register(modEventBus);
        OverdriveAEMenus.MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(OverdriveAECapabilities::register);
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.addListener(this::registerReloadListeners);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        OverdriveAEMachines.MACHINES.bind();

        List.of(new OverdriveAEDeferredMachine[] {
                OverdriveAEMachines.OVERDRIVE_CHARGER,
                OverdriveAEMachines.OVERDRIVE_INSCRIBER,
                OverdriveAEMachines.OVERDRIVE_ENERGIZED_BLAST_FURNACE,
                OverdriveAEMachines.OVERDRIVE_ENERGIZED_FURNACE,
                OverdriveAEMachines.OVERDRIVE_ENERGIZED_SMOKER,
        }).forEach(this::addQuantumUpgrade);
    }

    private void addQuantumUpgrade(OverdriveAEDeferredMachine<?, ?> machine) {
        Upgrades.add(QUANTUM_ACCELERATION_CARD.get(), machine, QuantumCpuBlockEntity.MAX_ACCELERATION_CARDS);
        Upgrades.add(TASK_FUSION_CARD.get(), machine, 1);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    private void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RecipeCacheReloadLister());
    }
}
