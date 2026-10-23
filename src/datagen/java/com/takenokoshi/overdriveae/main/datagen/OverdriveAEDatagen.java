package com.takenokoshi.overdriveae.main.datagen;

import com.takenokoshi.overdriveae.main.OverdriveAE;
import com.takenokoshi.overdriveae.main.datagen.lang.OverdriveAEEnglishLangProvider;
import com.takenokoshi.overdriveae.main.datagen.model.OverdriveAEBlockStateProvider;
import com.takenokoshi.overdriveae.main.datagen.model.OverdriveAEItemModelProvider;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = OverdriveAE.MODID, bus = EventBusSubscriber.Bus.MOD)
public class OverdriveAEDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFiles = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new OverdriveAEBlockStateProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new OverdriveAEItemModelProvider(output, existingFiles));

        generator.addProvider(event.includeClient(), new OverdriveAEEnglishLangProvider(output));
    }
}
