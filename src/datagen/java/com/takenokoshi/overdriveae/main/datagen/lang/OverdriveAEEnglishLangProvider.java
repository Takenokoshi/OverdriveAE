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

        add("gui.overdriveae.overdrive_inscriber.catalyst_slots", "Slots for catalyst items.");
        add("gui.overdriveae.overdrive_inscriber.catalyst_inv_label", "Catalyst items");
        add("gui.overdriveae.overdrive_inscriber.show_catalysts", "Show catalyst item slots");
        add("gui.overdriveae.overdrive_transform_chamber.catalyst_slots", "Tanks for catalyst fluids.");
        add("gui.overdriveae.overdrive_transform_chamber.catalyst_inv_label", "Catalyst fluids");
        add("gui.overdriveae.overdrive_transform_chamber.show_catalysts", "Show catalyst fluid tanks");
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
