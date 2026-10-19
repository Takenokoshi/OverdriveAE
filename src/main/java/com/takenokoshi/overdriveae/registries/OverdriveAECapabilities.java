package com.takenokoshi.overdriveae.registries;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class OverdriveAECapabilities {
    public static void register(RegisterCapabilitiesEvent event) {
        OverdriveAEMachines.MACHINES.getMachines().forEach(def -> {
            registerGridNodeHost(event, def.getBlockEntityType());
        });
        
    }

    /**
     * ワールド上のグリッドノードとして自分を公開する。
     *
     * <p>
     * 型引数を受ける別メソッドにしているのは、{@code BlockEntityType<?>} のままだと
     * {@code registerBlockEntity} の型が合わないため (ワイルドカードの捕捉)。
     * </p>
     */
    private static <T extends BlockEntity> void registerGridNodeHost(RegisterCapabilitiesEvent event,
            BlockEntityType<T> type) {
        event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, type,
                (blockEntity, context) -> blockEntity instanceof IInWorldGridNodeHost host ? host : null);
    }
}
