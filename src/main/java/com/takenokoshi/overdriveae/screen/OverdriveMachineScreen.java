/*
 * Some part of this file is copy of https://github.com/taikun24/InsaneAE/blob/1.21.1/src/main/java/jp/main/taikun/insaneae/client/QuantumCpuScreen.java
 */
package com.takenokoshi.overdriveae.screen;

import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.takenokoshi.overdriveae.menu.OverdriveMachineMenu;

import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.IconButton;
import appeng.client.gui.widgets.ServerSettingToggleButton;
import appeng.client.gui.widgets.SettingToggleButton;
import appeng.client.gui.widgets.ToggleButton;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.core.localization.GuiText;
import appeng.core.network.ServerboundPacket;
import appeng.core.network.serverbound.ConfigButtonPacket;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.AppEngSlot;
import jp.main.taikun.insaneae.menu.QuantumCpuMenu;
import jp.main.taikun.insaneae.mixin.SlotAccessor;
import jp.main.taikun.insaneae.quantum.QuantumCpuBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.network.PacketDistributor;

public class OverdriveMachineScreen extends AEBaseScreen<OverdriveMachineMenu> {

    

    private static final Logger LOG = LogUtils.getLogger();

    private static final int COLUMNS = QuantumCpuBlockEntity.PATTERN_COLUMNS;
    private static final int PER_PAGE = QuantumCpuBlockEntity.PATTERN_SLOTS_PER_PAGE;
    private static final int PAGES = QuantumCpuBlockEntity.PATTERN_PAGES;

    /** パターン枠の左上と間隔 (quantum_cpu.json の ENCODED_PATTERN と同じ値)。 */
    private static final int GRID_LEFT = 8;
    private static final int GRID_TOP = 22;
    private static final int SLOT_SIZE = 18;


    private final SettingToggleButton<YesNo> blockingModeButton;
    private final SettingToggleButton<LockCraftingMode> lockCraftingModeButton;
    private final ToggleButton showInPatternAccessTerminalButton;
    private final OverdriveMachineLockReason lockReason;
    private final IconTextButton prevPage;
    private final IconTextButton nextPage;

    private int page;

    public OverdriveMachineScreen(OverdriveMachineMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);

        this.blockingModeButton = new ServerSettingToggleButton<>(Settings.BLOCKING_MODE, YesNo.NO);
        this.addToLeftToolbar(this.blockingModeButton);

        lockCraftingModeButton = new ServerSettingToggleButton<>(Settings.LOCK_CRAFTING_MODE, LockCraftingMode.NONE);
        this.addToLeftToolbar(lockCraftingModeButton);

        widgets.addOpenPriorityButton();

        this.showInPatternAccessTerminalButton = new ToggleButton(Icon.PATTERN_ACCESS_SHOW,
                Icon.PATTERN_ACCESS_HIDE,
                GuiText.PatternAccessTerminal.text(), GuiText.PatternAccessTerminalHint.text(),
                btn -> selectNextPatternProviderMode());
        this.addToLeftToolbar(this.showInPatternAccessTerminalButton);

        this.lockReason = new OverdriveMachineLockReason(this);
        widgets.add("lockReason", this.lockReason);

        

        if (menu.getTarget() instanceof IUpgradeableObject upgradeable) {
            addUpgradesPanel(upgradeable);
        }

        // ウィジェットの追加は init() ではなくコンストラクタで行うこと
        // (WidgetContainer#add は同じ ID の二重登録で落ちるため。init は画面サイズ変更のたびに走る)
        prevPage = new IconTextButton(() -> Icon.ARROW_LEFT,
                Component.translatable("gui.insaneae.quantum_cpu.previous_page"), btn -> turnPage(-1));
        nextPage = new IconTextButton(() -> Icon.ARROW_RIGHT,
                Component.translatable("gui.insaneae.quantum_cpu.next_page"), btn -> turnPage(1));
        widgets.add("prevPage", prevPage);
        widgets.add("nextPage", nextPage);
    }

    /**
     * アップグレードカードのパネル。
     *
     * <p><b>他 Mod が同じものを先に足していることがある。</b> 例えば ExtendedAE Plus は
     * {@code PatternProviderScreen} のコンストラクタに Mixin で
     * {@code widgets.add("upgrades", new UpgradesPanel(...))} を注入する。
     * こちらは {@code super(...)} の後に同じ ID で足すので、
     * {@code WidgetContainer} の重複チェック ({@code "%s already used for widget"}) に当たって
     * {@code IllegalStateException} になり、<b>画面が開けなくなる</b>
     * (相手は自分の追加だけ try/catch で守っているのでログにも出ない)。</p>
     *
     * <p>中身はどちらも同じ {@code UPGRADE} スロットなので、既にあるなら相手のパネルに任せる。</p>
     */
    private void addUpgradesPanel(IUpgradeableObject upgradeable) {
        try {
            widgets.add("upgrades", new UpgradesPanel(menu.getSlots(SlotSemantics.UPGRADE), upgradeable));
        } catch (IllegalStateException alreadyAdded) {
            LOG.debug("InsaneAE: upgrades パネルは他 Mod が追加済みなので任せる ({})",
                    alreadyAdded.getMessage());
        }
    }

    @Override
    protected void init() {
        super.init();
        // super.init() がスタイルどおりに全スロットを並べてしまうので、ここで並べ直す。
        layoutSlots();
    }

    // ------------------------------------------------------------------ ページ送り

    private void turnPage(int delta) {
        setPage(page + delta);
    }

    private void setPage(int newPage) {
        int clamped = Math.max(0, Math.min(pageCount() - 1, newPage));
        if (clamped != page) {
            page = clamped;
            menu.setPage(page);
            layoutSlots();
        }
    }

    private int pageCount() {
        return menu.isServerPaged() ? menu.getPageCount() : PAGES;
    }

    /**
     * 現在のページのパターン枠と、クラフトユニット枠を並べる。
     *
     * <p>パターン枠とユニット枠は<b>同じ場所を取り合う</b>ので、出しているのはどちらか一方だけ。</p>
     *
     * <p>サーバ側でページ分割されている場合 ({@link QuantumCpuMenu#isServerPaged()})、
     * メニューには 1 ページぶんのスロットしか無いので素直に並べるだけ。
     * サーバ側のページングを切っている場合は全枠 (1620) 並んでいるので、
     * 表示するページぶんだけ位置を決めて残りは非表示にする<b>旧来のやり方</b>に落ちる。</p>
     */
    private void layoutSlots() {

        List<Slot> slots = menu.getSlots(SlotSemantics.ENCODED_PATTERN);
        int first = menu.isServerPaged() ? 0 : page * PER_PAGE;

        for (int index = 0; index < slots.size(); index++) {
            Slot slot = slots.get(index);
            int inPage = index - first;
            boolean visible =  inPage >= 0 && inPage < PER_PAGE;

            if (slot instanceof AppEngSlot appEngSlot) {
                // setSlotEnabled ではなく setActive。
                // 無効化すると mayPlace/mayPickup まで殺してしまい、Shift クリックが
                // 表示中のページにしか入らなくなる。
                appEngSlot.setActive(visible);
            }
            if (visible) {
                // Slot#x/y は final なので Mixin のアクセサ経由で書く (AE2 は AT で外している)。
                ((SlotAccessor) slot).insaneae$setX(GRID_LEFT + inPage % COLUMNS * SLOT_SIZE);
                ((SlotAccessor) slot).insaneae$setY(GRID_TOP + inPage / COLUMNS * SLOT_SIZE);
            }
        }
    }

    @Override
    // 1.21 で横スクロール (scrollX) が引数に加わった。ページ送りは縦だけ見る。
    public boolean mouseScrolled(double x, double y, double scrollX, double wheelDelta) {
        if (super.mouseScrolled(x, y, scrollX, wheelDelta)) {
            return true;
        }
        if ( wheelDelta != 0 && isOverPatternGrid(x, y)) {
            turnPage(wheelDelta > 0 ? -1 : 1);
            return true;
        }
        return false;
    }

    private boolean isOverPatternGrid(double x, double y) {
        int left = leftPos + GRID_LEFT;
        int top = topPos + GRID_TOP;
        return x >= left && x < left + COLUMNS * SLOT_SIZE
                && y >= top && y < top + PER_PAGE / COLUMNS * SLOT_SIZE;
    }

    // -------------------------------------------------------------------- 描画

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        this.lockReason.setVisible(menu.getLockCraftingMode() != LockCraftingMode.NONE);
        this.blockingModeButton.set(this.menu.getBlockingMode());
        this.lockCraftingModeButton.set(this.menu.getLockCraftingMode());
        this.showInPatternAccessTerminalButton.setState(this.menu.getShowInAccessTerminal() == YesNo.YES);
        setTextContent("dialog_title", this.menu.host.getName());
        setTextContent("page", Component.literal((page + 1) + "/" + pageCount()));
        prevPage.active = page > 0;
        nextPage.active = page < pageCount() - 1;
        // ロック表示 (lockReason) はタイトルと同じ行に出るので、出ている間はタイトルを引っ込める。
        setTextHidden("dialog_title", menu.getLockCraftingMode() != LockCraftingMode.NONE);
        setTextHidden("storageUnitsLabel", true);
        setTextHidden("acceleratorUnitsLabel", true);
    }

    /**
     * 背景。{@code generatedBackground} は枠しか描かないので、スロットのくぼみは自分で敷く
     * (専用の背景テクスチャを持たないぶん、ページ送りで枠数が変わっても崩れない)。
     */
    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY,
            float partialTicks) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks);

        for (Slot slot : menu.slots) {
            // アップグレードとツールボックスは専用パネルが自前の枠を描くので敷かない。
            SlotSemantic semantic = menu.getSlotSemantic(slot);
            if (slot.isActive() && semantic != SlotSemantics.UPGRADE && semantic != SlotSemantics.TOOLBOX) {
                Icon.SLOT_BACKGROUND.getBlitter()
                        .dest(offsetX + slot.x - 1, offsetY + slot.y - 1)
                        .blit(guiGraphics);
            }
        }
    }


    private void selectNextPatternProviderMode() {
        final boolean backwards = isHandlingRightClick();
        ServerboundPacket message = new ConfigButtonPacket(Settings.PATTERN_ACCESS_TERMINAL, backwards);
        PacketDistributor.sendToServer(message);
    }
    

    /**
     * アイコンだけのボタン。{@link IconButton} はアイコンを差し替えれば使える。
     *
     * <p>アイコンを供給側から取るのは、開閉ボタンが状態で見た目を変えるため
     * ({@code getIcon} は毎フレーム呼ばれる)。</p>
     */
    private static final class IconTextButton extends IconButton {

        private final Supplier<Icon> icon;

        private IconTextButton(Supplier<Icon> icon, Component message, Button.OnPress onPress) {
            super(onPress);
            this.icon = icon;
            setMessage(message);
        }

        @Override
        protected Icon getIcon() {
            return icon.get();
        }
    }
}
