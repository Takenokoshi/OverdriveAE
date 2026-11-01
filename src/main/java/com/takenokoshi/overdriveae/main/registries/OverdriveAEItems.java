package com.takenokoshi.overdriveae.main.registries;

import java.util.List;

import com.takenokoshi.overdriveae.main.OverdriveAE;
import com.takenokoshi.overdriveae.main.registration.ListBuilder;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class OverdriveAEItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OverdriveAE.MODID);

    public static final DeferredItem<Item> OVERDRIVE_CRYSTAL = ITEMS.registerSimpleItem("overdrive_crystal");
    public static final DeferredItem<Item> PRINTED_OVERDRIVE_PROCESSOR = ITEMS
            .registerSimpleItem("printed_overdrive_processor");
    public static final DeferredItem<Item> OVERDRIVE_PROCESSOR = ITEMS.registerSimpleItem("overdrive_processor");

    public static final List<DeferredItem<Item>> OVERDRIVE_SPEED_CARDS = ListBuilder.<DeferredItem<Item>>builder()
            .add(0, 6, i -> ITEMS.registerSimpleItem("t" + i + "_overdrive_speed_card"))
            .buildUnmodifiable();
    public static final List<DeferredItem<Item>> OVERDRIVE_ENERGY_CARDS = ListBuilder.<DeferredItem<Item>>builder()
            .add(0, 6, i -> ITEMS.registerSimpleItem("t" + i + "_overdrive_energy_card"))
            .buildUnmodifiable();
    public static final DeferredItem<Item> COPROCESSOR_USAGE_REDUCTION_CARD = ITEMS
            .registerSimpleItem("coprocessor_usage_reduction_card");
}
