package com.takenokoshi.overdriveae.lang;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.takenokoshi.overdriveae.OverdriveAE;
import com.takenokoshi.overdriveae.registration.OAEDeferredMachine;
import com.takenokoshi.overdriveae.registries.OverdriveAEMachines;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class OverdriveAEEnglishLangProvider extends LanguageProvider {

    public OverdriveAEEnglishLangProvider(PackOutput output) {
        super(output, OverdriveAE.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        OverdriveAEMachines.MACHINES.getMachines().forEach(this::addMachine);
    }

    private void addMachine(OAEDeferredMachine<?, ?> deferredMachine) {
        String name = format(deferredMachine.deferredBlock.getId().getPath());
        addBlock(deferredMachine.deferredBlock, name);
    }

    private String format(String name) {

        String[] split = name.split("_");

        return Arrays.stream(split)
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }

}
