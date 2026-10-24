package com.takenokoshi.overdriveae.main.datagen.loot;

import java.util.Set;

import com.takenokoshi.overdriveae.main.registration.OverdriveAEDeferredMachine;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEMachines;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class OverdriveAEBlockLootProvider extends BlockLootSubProvider {

    public OverdriveAEBlockLootProvider(
            Provider registries) {
        super(Set.of(), FeatureFlags.VANILLA_SET, registries);
    }

    @Override
    protected void generate() {
        for (OverdriveAEDeferredMachine<?, ?> machine : OverdriveAEMachines.MACHINES.getMachines()) {
            dropOther(machine.getBlock(), machine);
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return OverdriveAEMachines.MACHINES.getMachines().stream().<Block>map(OverdriveAEDeferredMachine::getBlock)
                .toList();
    }

}
