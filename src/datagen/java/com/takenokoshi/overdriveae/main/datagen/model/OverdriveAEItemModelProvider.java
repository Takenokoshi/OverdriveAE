package com.takenokoshi.overdriveae.main.datagen.model;

import com.takenokoshi.overdriveae.main.OverdriveAE;
import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEItems;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEMachines;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class OverdriveAEItemModelProvider extends ItemModelProvider {

    private static final String LAYER0 = "layer0";

    public OverdriveAEItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OverdriveAE.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (OverdriveAEDeferredMachine<?, ?> machine : OverdriveAEMachines.MACHINES.getMachines()) {
            getBuilder(machine.deferredItem.getId().getPath())
                    .parent(new ModelFile.UncheckedModelFile(
                            OverdriveAE.makeId("block/" + machine.deferredBlock.getId().getPath())));
        }

        basicItem(OverdriveAEItems.OVERDRIVE_CRYSTAL.get()).texture(LAYER0, modLoc("item/overdrive_crystal"));
        basicItem(OverdriveAEItems.PRINTED_OVERDRIVE_PROCESSOR.get()).texture(LAYER0,
                modLoc("item/printed_overdrive_processor"));
        basicItem(OverdriveAEItems.OVERDRIVE_PROCESSOR.get()).texture(LAYER0, modLoc("item/overdrive_processor"));
    }

}
