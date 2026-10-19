package com.takenokoshi.overdriveae.model;

import com.takenokoshi.overdriveae.OverdriveAE;
import com.takenokoshi.overdriveae.registration.OAEDeferredMachine;
import com.takenokoshi.overdriveae.registries.OverdriveAEMachines;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class OverdriveAEItemModelProvider extends ItemModelProvider {

    public OverdriveAEItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OverdriveAE.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (OAEDeferredMachine<?, ?> machine : OverdriveAEMachines.MACHINES.getMachines()) {
            getBuilder(machine.deferredItem.getId().getPath())
                    .parent(new ModelFile.UncheckedModelFile(
                            OverdriveAE.makeId("block/" + machine.deferredBlock.getId().getPath())));
        }
    }

}
