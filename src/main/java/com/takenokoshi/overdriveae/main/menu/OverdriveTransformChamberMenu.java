package com.takenokoshi.overdriveae.main.menu;

import java.util.List;

import com.takenokoshi.overdriveae.main.blockentity.machine.OverdriveTransformChamberBlockEntity;
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

public class OverdriveTransformChamberMenu extends OverdriveMachineMenu {

    public static final MenuType<OverdriveTransformChamberMenu> TYPE = IMenuTypeExtension
            .create(OverdriveTransformChamberMenu::fromNetwork);

    public static final SlotSemantic CATALYST_INV = SlotSemantics.register("OVERDRIVEAE_TRANSFORMER_CATALYST", false);

    static {
        MenuOpener.addOpener(TYPE, OverdriveTransformChamberMenu::open);
    }

    public OverdriveTransformChamberMenu(int id, Inventory playerInventory,
            OverdriveTransformChamberBlockEntity host) {
        this(TYPE, id, playerInventory, host);
    }

    public OverdriveTransformChamberMenu(MenuType<?> menuType, int id, Inventory playerInventory,
            OverdriveTransformChamberBlockEntity host) {
        super(menuType, id, playerInventory, host);

        var catalystInv = host.getCatalystInv().createMenuWrapper();
        List<Component> storageTooltip = List
                .of(Component.translatable("gui.overdriveae.overdrive_transform_chamber.catalyst_slots"));
        for (int i = 0; i < OverdriveTransformChamberBlockEntity.CATALYST_INV_SLOTS; i++) {
            if (i < catalystInv.size()) {
                AppEngSlot slot = new AppEngSlot(catalystInv, i);
                slot.setEmptyTooltip(() -> storageTooltip);
                this.addSlot(slot, CATALYST_INV);
            }
        }
    }

    private static OverdriveTransformChamberMenu fromNetwork(int containerId, Inventory playerInventory,
            RegistryFriendlyByteBuf buf) {
        MenuHostLocator locator = MenuLocators.readFromPacket(buf);
        OverdriveTransformChamberBlockEntity host = locator.locate(playerInventory.player,
                OverdriveTransformChamberBlockEntity.class);
        if (host == null) {
            throw new IllegalStateException("Couldn't find a Quantum CPU at " + locator + " on the client.");
        }
        OverdriveTransformChamberMenu menu = new OverdriveTransformChamberMenu(containerId, playerInventory, host);
        menu.setReturnedFromSubScreen(buf.readBoolean());
        return menu;
    }

    private static boolean open(Player player, MenuHostLocator locator, boolean fromSubMenu) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        OverdriveTransformChamberBlockEntity host = locator.locate(player, OverdriveTransformChamberBlockEntity.class);
        if (host == null) {
            return false;
        }
        Component title = host instanceof Nameable nameable && nameable.hasCustomName()
                ? nameable.getCustomName()
                : Component.empty();
        MenuProvider provider = new SimpleMenuProvider((containerId, inv, p) -> {
            OverdriveTransformChamberMenu menu = new OverdriveTransformChamberMenu(containerId, inv, host);
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
