package com.takenokoshi.overdriveae.main.registration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.mojang.datafixers.util.Function3;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * OAEDeferredMachineをつくる為のDeferredRegisterのラッパー。レジストリ簡略化要因。
 */
public class OverdriveAEMachineDeferredRegister {
    public final String modid;
    public final DeferredRegister.Blocks blockRegister;
    public final DeferredRegister.Items itemRegister;
    public final DeferredRegister<BlockEntityType<?>> blockEntityTypeRegister;

    private final List<OverdriveAEDeferredMachine<?, ?>> machines;

    public OverdriveAEMachineDeferredRegister(String modid) {
        this.modid = modid;
        this.blockRegister = DeferredRegister.createBlocks(modid);
        this.itemRegister = DeferredRegister.createItems(modid);
        this.blockEntityTypeRegister = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, modid);
        this.machines = new ArrayList<>();
    }

    public <B extends AEBaseEntityBlock<E>, E extends AEBaseBlockEntity> OverdriveAEDeferredMachine<B, E> register(String name,
            Function<BlockBehaviour.Properties, B> blockFunc,
            Class<E> entityClass,
            Function3<OverdriveAEDeferredMachine<?, ?>, BlockPos, BlockState, E> blockEntityFunc,
            BlockEntityTicker<E> clientTicker, BlockEntityTicker<E> serverTicker) {
        var result = new OverdriveAEDeferredMachine<>(name, blockFunc, entityClass, blockEntityFunc, clientTicker, serverTicker,
                this);
        machines.add(result);
        return result;
    }

    public void register(IEventBus bus) {
        blockRegister.register(bus);
        itemRegister.register(bus);
        blockEntityTypeRegister.register(bus);
    }

    public List<OverdriveAEDeferredMachine<?, ?>> getMachines() {
        return Collections.unmodifiableList(machines);
    }

    public void bind(){
        machines.forEach(OverdriveAEDeferredMachine::bind);
    }
}
