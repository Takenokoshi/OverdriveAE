package com.takenokoshi.overdriveae.main.registries;

import com.takenokoshi.overdriveae.main.OverdriveAE;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class OverdriveAEItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OverdriveAE.MODID);

    public static final DeferredItem<Item> OVERDRIVE_CRYSTAL = ITEMS.registerSimpleItem("overdrive_crystal");
    public static final DeferredItem<Item> PRINTED_OVERDRIVE_PROCESSOR = ITEMS
            .registerSimpleItem("printed_overdrive_processor");
    public static final DeferredItem<Item> OVERDRIVE_PROCESSOR = ITEMS.registerSimpleItem("overdrive_processor");
}
