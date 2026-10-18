package com.takenokoshi.overdriveae.registries;

import com.takenokoshi.overdriveae.OverdriveAE;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class OAETabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            OverdriveAE.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(OAEMachines.OVERDRIVE_CHARGER, 1))
                    .title(Component.literal("OverdriveAE"))
                    .displayItems(OAEMachines.MACHINES.itemRegister.getEntries()).build());
}
