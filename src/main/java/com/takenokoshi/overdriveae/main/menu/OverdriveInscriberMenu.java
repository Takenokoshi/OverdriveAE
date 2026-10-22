package com.takenokoshi.overdriveae.main.menu;

import java.util.List;

import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveInscriberBlockEntity;

import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;

public class OverdriveInscriberMenu extends OverdriveMachineMenu {

    public static final MenuType<OverdriveInscriberMenu> TYPE = IMenuTypeExtension
            .create(OverdriveInscriberMenu::fromNetwork);

    public static final SlotSemantic CATALYST_INV = SlotSemantics.register("OVERDRIVEAE_INSCRIBER_CATALYST", false);

    static {
        MenuOpener.addOpener(TYPE, OverdriveInscriberMenu::open);
    }

    public OverdriveInscriberMenu(int id, Inventory playerInventory,
            OverdriveInscriberBlockEntity host) {
        this(TYPE, id, playerInventory, host);
    }

    public OverdriveInscriberMenu(MenuType<?> menuType, int id, Inventory playerInventory,
            OverdriveInscriberBlockEntity host) {
        super(menuType, id, playerInventory, host);
        List<Component> storageTooltip = List
                .of(Component.translatable("gui.overdriveae.overdrive_inscriber.catalyst_slots"));
        for (int i = 0; i < OverdriveInscriberBlockEntity.CATALYST_INV_SLOTS; i++) {
            AppEngSlot slot = new AppEngSlot(host.getCatalystInv(), i);
            slot.setEmptyTooltip(() -> storageTooltip);
            addSlot(slot, CATALYST_INV);
        }
    }

    // 1.20.5 で画面を開くときのバッファがレジストリ参照つき (RegistryFriendlyByteBuf) になった。
    private static OverdriveInscriberMenu fromNetwork(int containerId, Inventory playerInventory,
            RegistryFriendlyByteBuf buf) {
        MenuHostLocator locator = MenuLocators.readFromPacket(buf);
        OverdriveInscriberBlockEntity host = locator.locate(playerInventory.player,
                OverdriveInscriberBlockEntity.class);
        if (host == null) {
            throw new IllegalStateException("Couldn't find a Quantum CPU at " + locator + " on the client.");
        }
        OverdriveInscriberMenu menu = new OverdriveInscriberMenu(containerId, playerInventory, host);
        menu.setReturnedFromSubScreen(buf.readBoolean());
        return menu;
    }

    private static boolean open(Player player, MenuHostLocator locator, boolean fromSubMenu) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        OverdriveInscriberBlockEntity host = locator.locate(player, OverdriveInscriberBlockEntity.class);
        if (host == null) {
            return false;
        }
        Component title = host instanceof Nameable nameable && nameable.hasCustomName()
                ? nameable.getCustomName()
                : Component.empty();
        MenuProvider provider = new SimpleMenuProvider((containerId, inv, p) -> {
            OverdriveInscriberMenu menu = new OverdriveInscriberMenu(containerId, inv, host);
            menu.setLocator(locator);
            return menu;
        }, title);
        // NetworkHooks は NeoForge で廃止され、ServerPlayer#openMenu が
        // 追加データ書き込み用のコールバックを直接受け取るようになった。
        serverPlayer.openMenu(provider, buf -> {
            MenuLocators.writeToPacket(buf, locator);
            buf.writeBoolean(fromSubMenu);
        });
        return true;
    }

}
