package com.takenokoshi.overdriveae.main.datagen.lang;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.takenokoshi.overdriveae.main.OverdriveAE;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEItems;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEMachines;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

public class OverdriveAEEnglishLangProvider extends LanguageProvider {

    public OverdriveAEEnglishLangProvider(PackOutput output) {
        super(output, OverdriveAE.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        OverdriveAEMachines.MACHINES.getMachines().forEach(this::addMachine);
        OverdriveAEItems.ITEMS.getEntries().forEach(this::addItem);

        add("gui.overdriveae.element.upgrade_slots_label", "Upgrades");
        add("gui.overdriveae.element.catalyst_slots_label", "Catalysts");
        add("gui.overdriveae.element.back_to_pattern_slots", "Back to pattern inventory.");
        add("gui.overdriveae.element.open_upgrade_slots", "Open upgrade inventory.");
        add("gui.overdriveae.element.open_catalyst_slots", "Open catalyst inventory.");
        add("gui.overdriveae.element.prev_page", "Go to the previous page.");
        add("gui.overdriveae.element.next_page", "Go to the next page.");
    }

    private void addMachine(OverdriveAEDeferredMachine<?, ?> deferredMachine) {
        String name = format(deferredMachine.deferredBlock.getId().getPath());
        addBlock(deferredMachine.deferredBlock, name);
    }

    private void addItem(DeferredHolder<Item, ?> item) {
        addItem(item, format(item.getId().getPath()));
    }

    private String format(String name) {

        String[] split = name.split("_");

        return Arrays.stream(split)
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }

}
