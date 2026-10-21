/*
 * Some part of this file is copy of https://github.com/taikun24/InsaneAE/blob/1.21.1/src/main/java/jp/main/taikun/insaneae/menu/QuantumCpuMenu.java
 */
package com.takenokoshi.overdriveae.menu;

import java.util.List;

import com.takenokoshi.overdriveae.blockentity.prefab.OverdriveMachineBlockEntity;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.GenericStack;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderReturnInventory;
import appeng.menu.AEBaseMenu;
import appeng.menu.MenuOpener;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.guisync.GuiSync;
import appeng.menu.locator.MenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.menu.slot.AppEngSlot;
import jp.main.taikun.insaneae.config.InsaneAEConfig;
import jp.main.taikun.insaneae.menu.PagedPatternInventory;
import jp.main.taikun.insaneae.menu.PatternPagingHandshake;
import jp.main.taikun.insaneae.quantum.QuantumCpuBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

    /** 表示ページの変更をサーバに伝えるアクション。 */
    private static final String ACTION_SET_PAGE = "insaneaeSetPage";

    static {
        MenuOpener.addOpener(TYPE, OverdriveMachineMenu::open);
    }
    protected final PatternProviderLogic logic;

    public final OverdriveMachineBlockEntity<?, ?, ?> host;

    @GuiSync(3)
    public YesNo blockingMode = YesNo.NO;
    @GuiSync(4)
    public YesNo showInAccessTerminal = YesNo.YES;
    @GuiSync(5)
    public LockCraftingMode lockCraftingMode = LockCraftingMode.NONE;
    @GuiSync(6)
    public LockCraftingMode craftingLockedReason = LockCraftingMode.NONE;
    @GuiSync(7)
    public GenericStack unlockStack = null;

    /**
     * パターン枠の窓。super のコンストラクタの中で作られたものを
     * {@link PatternPagingHandshake#finish()} で受け取る。
     *
     * <p>
     * ページングを切っている場合はここが null のままで、パターン枠は従来どおり全枠並ぶ。
     * </p>
     */
    private PagedPatternInventory patternWindow;

    public OverdriveMachineMenu(int id, Inventory playerInventory,
            OverdriveMachineBlockEntity<?, ?, ?> host) {
        this(TYPE, id, playerInventory, host);
    }

    public OverdriveMachineMenu(MenuType<?> menuType, int id, Inventory playerInventory,
            OverdriveMachineBlockEntity<?, ?, ?> host) {
        super(menuType, id, playerInventory,
                PatternPagingHandshake.begin(host, InsaneAEConfig.serverSidePatternPaging()));
        this.createPlayerInventorySlots(playerInventory);
        this.logic = host.getLogic();
        this.host = host;

        var patternInv = logic.getPatternInv();
        for (int x = 0; x < patternInv.size(); x++) {
            this.addSlot(new AppEngSlot(patternInv, x), SlotSemantics.ENCODED_PATTERN);
        }

        // Show first few entries of the return inv
        var returnInv = logic.getReturnInv().createMenuWrapper();
        for (int i = 0; i < PatternProviderReturnInventory.NUMBER_OF_SLOTS; i++) {
            if (i < returnInv.size()) {
                this.addSlot(new AppEngSlot(returnInv, i), SlotSemantics.STORAGE);
            }
        }
        patternWindow = PatternPagingHandshake.finish();

        registerClientAction(ACTION_SET_PAGE, Integer.class, this::applyPage);
        // アップグレードスロット。
        // 他 Mod が PatternProviderMenu のコンストラクタに Mixin で
        // setupUpgrades(host.getUpgrades()) を注入していることがある
        // (AppliedFlux の MixinPatternProviderMenu が実際にやっている)。
        // 相手が使うのは<b>こちらと同じインベントリ</b>なので、素直に足すと
        // 同じ中身のスロットが 2 セット並び、上下が連動する見た目になる。
        // 既に UPGRADE スロットがあるならそちらに任せる。
        if (host instanceof IUpgradeableObject upgradeable
                && getSlots(SlotSemantics.UPGRADE).isEmpty()) {
            setupUpgrades(upgradeable.getUpgrades());
        }

        // 返却インベントリには見出しを出していないので、
        // 空スロットのツールチップで何の枠か分かるようにする。
        List<Component> returnInvTooltip = List.of(Component.translatable("gui.insaneae.quantum_cpu.return_inventory"));
        for (Slot slot : getSlots(SlotSemantics.STORAGE)) {
            if (slot instanceof AppEngSlot appEngSlot) {
                appEngSlot.setEmptyTooltip(() -> returnInvTooltip);
            }
        }
    }

    @Override
    public void broadcastChanges() {
        if (isServerSide()) {
            blockingMode = logic.getConfigManager().getSetting(Settings.BLOCKING_MODE);
            showInAccessTerminal = logic.getConfigManager().getSetting(Settings.PATTERN_ACCESS_TERMINAL);
            lockCraftingMode = logic.getConfigManager().getSetting(Settings.LOCK_CRAFTING_MODE);
            craftingLockedReason = logic.getCraftingLockedReason();
            unlockStack = logic.getUnlockStack();
        }

        super.broadcastChanges();
    }

    public GenericStackInv getReturnInv() {
        return logic.getReturnInv();
    }

    public YesNo getBlockingMode() {
        return blockingMode;
    }

    public LockCraftingMode getLockCraftingMode() {
        return lockCraftingMode;
    }

    public LockCraftingMode getCraftingLockedReason() {
        return craftingLockedReason;
    }

    public GenericStack getUnlockStack() {
        return unlockStack;
    }

    public YesNo getShowInAccessTerminal() {
        return showInAccessTerminal;
    }

    // ------------------------------------------------------------ ページ送り

    /** パターン枠がサーバ側でページ分割されているか。 */
    public boolean isServerPaged() {
        return patternWindow != null;
    }

    public int getPageCount() {
        return patternWindow != null
                ? patternWindow.getPageCount()
                : QuantumCpuBlockEntity.PATTERN_PAGES;
    }

    public int getPage() {
        return patternWindow != null ? patternWindow.getPage() : 0;
    }

    /**
     * 表示ページを変える。クライアントから呼ぶとサーバにも伝わり、
     * サーバは次の {@code broadcastChanges} で新しいページの中身を送ってくる
     * (＝ページ送りの直後だけ 1 往復ぶん古い中身が見えることがある)。
     */
    public void setPage(int page) {
        if (patternWindow == null) {
            return;
        }
        applyPage(page);
        if (isClientSide()) {
            sendClientAction(ACTION_SET_PAGE, page);
        }
    }

    private void applyPage(int page) {
        if (patternWindow != null) {
            patternWindow.setPage(page);
        }
    }

    /**
     * シフトクリックでパターンを入れるときは<b>表示中のページに限らず全枠</b>を使う。
     *
     * <p>
     * メニューには 1 ページぶんのスロットしか無いので、
     * バニラ／AE2 の処理に任せると表示中のページが埋まった時点で止まってしまう。
     * パターン以外・パターン枠から取り出す側は AE2 の処理のまま。
     * </p>
     */
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (patternWindow == null || isClientSide()) {
            return super.quickMoveStack(player, slotIndex);
        }

        Slot clicked = slots.get(slotIndex);
        if (!isPlayerInventorySlot(clicked) || !PatternDetailsHelper.isEncodedPattern(clicked.getItem())) {
            return super.quickMoveStack(player, slotIndex);
        }

        ItemStack moving = clicked.getItem();
        ItemStack remainder = patternWindow.getBacking().addItems(moving.copy());
        if (remainder.getCount() != moving.getCount()) {
            clicked.set(remainder);
            clicked.setChanged();
            broadcastChanges();
        }
        // 何枚入ったかに関わらず、ここで打ち止め (空を返すとバニラのループが終わる)。
        return ItemStack.EMPTY;
    }

    protected boolean isPlayerInventorySlot(Slot slot) {
        SlotSemantic semantic = getSlotSemantic(slot);
        return semantic == SlotSemantics.PLAYER_INVENTORY || semantic == SlotSemantics.PLAYER_HOTBAR;
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
