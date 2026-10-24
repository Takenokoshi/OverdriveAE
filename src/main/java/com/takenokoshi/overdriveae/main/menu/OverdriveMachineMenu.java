
package com.takenokoshi.overdriveae.main.menu;

import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveMachineBlockEntity;

import appeng.api.inventories.InternalInventory;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantics;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.slot.AppEngSlot;
import appeng.util.ConfigMenuInventory;
import jp.main.taikun.insaneae.menu.QuantumCpuMenu;
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

public class OverdriveMachineMenu extends QuantumCpuMenu {

    public static final MenuType<OverdriveMachineMenu> TYPE = IMenuTypeExtension
            .create(OverdriveMachineMenu::fromNetwork);

    static {
        MenuOpener.addOpener(TYPE, OverdriveMachineMenu::open);
    }

    public final OverdriveMachineBlockEntity<?, ?, ?> host;

    public OverdriveMachineMenu(int id, Inventory playerInventory,
            OverdriveMachineBlockEntity<?, ?, ?> host) {
        this(TYPE, id, playerInventory, host);
    }

    public OverdriveMachineMenu(MenuType<? extends QuantumCpuMenu> menuType, int id, Inventory playerInventory,
            OverdriveMachineBlockEntity<?, ?, ?> host) {
        super(menuType, id, playerInventory, host);
        this.host = host;
        /**
         * PatternProviderMenu側が用意するパターンスロットはパターンアイテムしか受け付けない。
         * これはOverdriveAEの意向に沿わないので一度パターンスロットを全て削除し、
         * こちらで追加し直す。
         * 
         * 本来はMixinでPatternProviderMenuのコンストラクタを改変して対応したかったが、失敗した
         * パターンスロットを追加し直す影響で、アイテムのクイック移動の優先度が変わってしまうので他のスロットも追加をやり直す
         * ついでに、触媒アイテムが返却インベントリではなく触媒アイテムスロットにクイック移動できるようにしておく
         * 
         * 全部空にしているので、AppFluxのMixin由来のアップグレードスロット等も消えている
         */
        AEBaseMenuBridge.getSlotsBySemantic(this).clear();
        AEBaseMenuBridge.getSemanticBySlot(this).clear();
        AEBaseMenuBridge.getLastSlots(this).clear();
        AEBaseMenuBridge.getRemoteSlots(this).clear();
        slots.clear();

        this.createPlayerInventorySlots(playerInventory);
        InternalInventory patternInv = this.host.getLogic().getPatternInv();
        for (int x = 0; x < patternInv.size(); x++) {
            this.addSlot(new AppEngSlot(patternInv, x), SlotSemantics.ENCODED_PATTERN);
        }

        addAdditionalSlotsInConstructor();

        this.setupUpgrades(this.host.getUpgrades());

        ConfigMenuInventory returnInv = this.logic.getReturnInv().createMenuWrapper();
        for (int i = 0; i < PatternProviderReturnInventory.NUMBER_OF_SLOTS; i++) {
            if (i < returnInv.size()) {
                this.addSlot(new AppEngSlot(returnInv, i), SlotSemantics.STORAGE);
            }
        }

    }

    protected void addAdditionalSlotsInConstructor() {
    }

    // 1.20.5 で画面を開くときのバッファがレジストリ参照つき (RegistryFriendlyByteBuf) になった。
    private static OverdriveMachineMenu fromNetwork(int containerId, Inventory playerInventory,
            RegistryFriendlyByteBuf buf) {
        MenuHostLocator locator = MenuLocators.readFromPacket(buf);
        OverdriveMachineBlockEntity<?, ?, ?> host = locator.locate(playerInventory.player,
                OverdriveMachineBlockEntity.class);
        if (host == null) {
            throw new IllegalStateException("Couldn't find a Quantum CPU at " + locator + " on the client.");
        }
        OverdriveMachineMenu menu = new OverdriveMachineMenu(containerId, playerInventory, host);
        menu.setReturnedFromSubScreen(buf.readBoolean());
        return menu;
    }

    private static boolean open(Player player, MenuHostLocator locator, boolean fromSubMenu) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        OverdriveMachineBlockEntity<?, ?, ?> host = locator.locate(player, OverdriveMachineBlockEntity.class);
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
        // NetworkHooks は NeoForge で廃止され、ServerPlayer#openMenu が
        // 追加データ書き込み用のコールバックを直接受け取るようになった。
        serverPlayer.openMenu(provider, buf -> {
            MenuLocators.writeToPacket(buf, locator);
            buf.writeBoolean(fromSubMenu);
        });
        return true;
    }

}
