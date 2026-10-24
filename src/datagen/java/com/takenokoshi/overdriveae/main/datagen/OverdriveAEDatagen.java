package com.takenokoshi.overdriveae.main.datagen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.takenokoshi.overdriveae.main.OverdriveAE;
import com.takenokoshi.overdriveae.main.datagen.lang.OverdriveAEEnglishLangProvider;
import com.takenokoshi.overdriveae.main.datagen.loot.OverdriveAEBlockLootProvider;
import com.takenokoshi.overdriveae.main.datagen.model.OverdriveAEBlockStateProvider;
import com.takenokoshi.overdriveae.main.datagen.model.OverdriveAEItemModelProvider;
import com.takenokoshi.overdriveae.main.datagen.recipe.OverdriveAERecipeProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@SuppressWarnings("removal")
@EventBusSubscriber(modid = OverdriveAE.MODID, bus = EventBusSubscriber.Bus.MOD)
public class OverdriveAEDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFiles = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new OverdriveAEBlockStateProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new OverdriveAEItemModelProvider(output, existingFiles));

        generator.addProvider(event.includeClient(), new OverdriveAEEnglishLangProvider(output));

        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(OverdriveAEBlockLootProvider::new, LootContextParamSets.BLOCK)),
                lookupProvider));
        generator.addProvider(event.includeServer(), new OverdriveAERecipeProvider(output, lookupProvider));
    }
}
