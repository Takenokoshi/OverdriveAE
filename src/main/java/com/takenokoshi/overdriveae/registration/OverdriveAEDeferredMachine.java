package com.takenokoshi.overdriveae.registration;

import java.util.function.Function;

import com.mojang.datafixers.util.Function3;

import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.DeferredBlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * DeferredHolderのラッパー。レジストリの簡略化要因。
 */
public class OverdriveAEDeferredMachine<B extends AEBaseEntityBlock<E>, E extends AEBaseBlockEntity> implements ItemLike {

    public final DeferredBlock<B> deferredBlock;
    public final DeferredItem<BlockItem> deferredItem;
    public final DeferredBlockEntityType<E> deferredBlockEntityType;
    private final BlockEntityTicker<E> clientTicker, serverTicker;

    public OverdriveAEDeferredMachine(String name,
            Function<BlockBehaviour.Properties, B> blockFunc,
            Class<E> entityClass,
            Function3<OverdriveAEDeferredMachine<?, ?>, BlockPos, BlockState, E> blockEntityFunc,
            BlockEntityTicker<E> clientTicker, BlockEntityTicker<E> serverTicker,
            OverdriveAEMachineDeferredRegister register) {
        this.deferredBlock = register.blockRegister.registerBlock(name, blockFunc);
        this.deferredItem = register.itemRegister.registerSimpleBlockItem(deferredBlock);
        this.deferredBlockEntityType = new DeferredBlockEntityType<>(entityClass,
                register.blockEntityTypeRegister.register(name,
                        () -> BlockEntityType.Builder
                                .of(createBlockEntitySupplier(blockEntityFunc), new Block[] { deferredBlock.get(), })
                                .build(null)));
        this.clientTicker = clientTicker;
        this.serverTicker = serverTicker;
    }

    public BlockEntityType<E> getBlockEntityType() {
        return deferredBlockEntityType.get();
    }

    private BlockEntityType.BlockEntitySupplier<E> createBlockEntitySupplier(
            Function3<OverdriveAEDeferredMachine<?, ?>, BlockPos, BlockState, E> blockEntityFunc) {
        return (p, s) -> blockEntityFunc.apply(this, p, s);
    }

    @Override
    public Item asItem() {
        return deferredItem.asItem();
    }

    public void bind() {
        deferredBlock.get().setBlockEntity(deferredBlockEntityType.getBlockEntityClass(), getBlockEntityType(),
                clientTicker, serverTicker);
    }

    public ResourceLocation getId(){
        return deferredBlock.getId();
    }

    public B getBlock(){
        return deferredBlock.get();
    }

    public BlockItem getItem(){
        return deferredItem.get();
    }

}
