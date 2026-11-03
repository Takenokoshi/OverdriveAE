package com.takenokoshi.overdriveae.main.menu;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.AppEngSlot;

public class PagedAppEngSlot extends AppEngSlot {

    private final int pageIndex;

    public PagedAppEngSlot(InternalInventory inv, int invSlot, int pageIndex) {
        super(inv, invSlot);
        this.pageIndex = pageIndex;
    }

    public void setPage(int page) {
        setActive(pageIndex == page);
    }

}
