package com.takenokoshi.overdriveae.main.registries;

import java.util.ArrayList;
import java.util.List;

import com.takenokoshi.overdriveae.main.OverdriveAE;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class OverdriveAETabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB,
            OverdriveAE.MODID);

    private static final List<Holder<Item>> displayItems = new ArrayList<>();
    static {
        displayItems.addAll(OverdriveAEItems.ITEMS.getEntries());
        displayItems.addAll(OverdriveAEMachines.MACHINES.itemRegister.getEntries());
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(OverdriveAEItems.OVERDRIVE_PROCESSOR.get(), 1))
                    .title(Component.literal("OverdriveAE"))
                    .displayItems(displayItems)
                    .build());
}
