package com.takenokoshi.overdriveae.main.menu;

import appeng.api.inventories.InternalInventory;
import net.minecraft.world.item.ItemStack;

public class PagedMenuInv<INV extends InternalInventory> implements InternalInventory {

    private final INV delegate;
    private final int slotsPerPage, totalPage;
    private int page;

    public PagedMenuInv(INV delegate, int slotsPerPage) {
        this.delegate = delegate;
        this.slotsPerPage = slotsPerPage;
        this.totalPage = this.delegate.size() / this.slotsPerPage;
    }

    public INV getDelegate() {
        return delegate;
    }

    public int getPage() {
        return page;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setPage(int page) {
        this.page = Math.floorMod(page, totalPage);
    }

    @Override
    public int size() {
        return slotsPerPage;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        return delegate.getStackInSlot(page * slotsPerPage + Math.floorMod(slotIndex, slotsPerPage));
    }

    @Override
    public void setItemDirect(int slotIndex, ItemStack stack) {
        delegate.setItemDirect(page * slotsPerPage + Math.floorMod(slotIndex, slotsPerPage), stack);
    }

    @Override
    public boolean isItemValid(int slotIndex, ItemStack stack) {
        return delegate.isItemValid(page * slotsPerPage + Math.floorMod(slotIndex, slotsPerPage), stack);
    }

    @Override
    public int getSlotLimit(int slotIndex) {
        return delegate.getSlotLimit(page * slotsPerPage + Math.floorMod(slotIndex, slotsPerPage));
    }
}
