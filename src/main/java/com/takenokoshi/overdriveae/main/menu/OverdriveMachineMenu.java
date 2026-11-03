package com.takenokoshi.overdriveae.main.menu;

import com.takenokoshi.overdriveae.main.blockentity.prefab.IOverdriveMachine;

import appeng.api.stacks.AEItemKey;
import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.slot.AppEngSlot;
import appeng.util.ConfigMenuInventory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public class OverdriveMachineMenu extends AEBaseMenu {

    public static final MenuType<OverdriveMachineMenu> TYPE = IMenuTypeExtension
            .create(OverdriveMachineMenu::fromNetwork);

    public static final int PATTERN_SLOTS_PER_PAGE = 54;

    public static final String PATTERN_SLOT_PAGE_CHANGE = "overdriveaePatternSlotPageChange";

    static {
        MenuOpener.addOpener(TYPE, OverdriveMachineMenu::open);
    }

    public final IOverdriveMachine<?, ?> host;
    protected final PagedMenuInv<ConfigMenuInventory> patternInv;

    public OverdriveMachineMenu(MenuType<?> menuType, int id, Inventory playerInventory, IOverdriveMachine<?, ?> host) {
        super(menuType, id, playerInventory, host);
        this.host = host;
        this.createPlayerInventorySlots(playerInventory);

        this.patternInv = new PagedMenuInv<>(this.host.getLogic().getPatternInv().createMenuWrapper(),
                PATTERN_SLOTS_PER_PAGE);

        for (int i = 0; i < patternInv.size(); i++) {
            addSlot(new AppEngSlot(patternInv, i), SlotSemantics.ENCODED_PATTERN);
        }
        setupUpgrades(this.host.getUpgrades());
        registerClientAction(PATTERN_SLOT_PAGE_CHANGE, Integer.class, this::applyPage);

    }

    public OverdriveMachineMenu(int id, Inventory playerInventory, IOverdriveMachine<?, ?> host) {
        this(TYPE, id, playerInventory, host);
    }

    public int getPage() {
        return patternInv.getPage();
    }

    public void setPage(int page) {
        applyPage(page);
        if (isClientSide()) {
            sendClientAction(PATTERN_SLOT_PAGE_CHANGE, page);
        }
    }

    public void prevPage() {
        setPage(getPage() - 1);
    }

    public void nextPage() {
        setPage(getPage() + 1);
    }

    private void applyPage(int page) {
        patternInv.setPage(page);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (isClientSide()) {
            return super.quickMoveStack(player, slotIndex);
        }
        Slot slot = slots.get(slotIndex);
        if (!isPlayerInventorySlot(slot)) {
            return super.quickMoveStack(player, slotIndex);
        }

        ItemStack moving = slot.getItem();

        if (!host.isPatternAcceptable(AEItemKey.of(moving))) {
            return super.quickMoveStack(player, slotIndex);
        }
        ItemStack remain = patternInv.getDelegate().addItems(moving.copy());
        if (remain.getCount() != moving.getCount()) {
            slot.set(remain);
            slot.setChanged();
            broadcastChanges();
        }
        return ItemStack.EMPTY;
    }

    private boolean isPlayerInventorySlot(Slot slot) {
        SlotSemantic semantic = getSlotSemantic(slot);
        return semantic == SlotSemantics.PLAYER_INVENTORY || semantic == SlotSemantics.PLAYER_HOTBAR;
    }

    private static OverdriveMachineMenu fromNetwork(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        MenuHostLocator locator = MenuLocators.readFromPacket(buf);
        IOverdriveMachine<?, ?> host = locator.locate(playerInventory.player, IOverdriveMachine.class);
        if (host == null) {
            throw new IllegalStateException("Couldn't find a Overdrive Machine at " + locator + " on the client.");
        }
        OverdriveMachineMenu menu = new OverdriveMachineMenu(containerId, playerInventory, host);
        menu.setReturnedFromSubScreen(buf.readBoolean());
        return menu;
    }

    private static boolean open(Player player, MenuHostLocator locator, boolean fromSubMenu) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        IOverdriveMachine<?, ?> host = locator.locate(player, IOverdriveMachine.class);
        if (host == null) {
            return false;
        }
        Component title = host instanceof Nameable nameable && nameable.hasCustomName()
                ? nameable.getCustomName()
                : Component.empty();
        MenuProvider provider = new SimpleMenuProvider((containerId, inv, p) -> {
            OverdriveMachineMenu menu = new OverdriveMachineMenu(containerId, inv, host);
            menu.setLocator(locator);
            return menu;
        }, title);
        serverPlayer.openMenu(provider, buf -> {
            MenuLocators.writeToPacket(buf, locator);
            buf.writeBoolean(fromSubMenu);
        });
        return true;
    }

}
