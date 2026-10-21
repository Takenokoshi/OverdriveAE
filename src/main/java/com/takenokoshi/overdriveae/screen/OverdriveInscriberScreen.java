package com.takenokoshi.overdriveae.screen;

import java.util.List;

import com.takenokoshi.overdriveae.menu.OverdriveInscriberMenu;

import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.AppEngSlot;
import jp.main.taikun.insaneae.mixin.SlotAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class OverdriveInscriberScreen extends OverdriveMachineScreen<OverdriveInscriberMenu> {

    /** クラフトユニット枠の 1 行の数。 */
    private static final int UNIT_COLUMNS = 9;
    /** クラフトストレージ枠の 1 行目の y (見出しのぶん下げてある)。 */
    private static final int STORAGE_UNITS_TOP = GRID_TOP + 12;

    private final IconTextButton catalystsToggle;

    /**
     * 触媒アイテムの枠を出しているか。
     *
     * <p>
     * ユニット枠は常設せず、ボタンでパターン枠と<b>入れ替えて</b>出す
     * (常設すると画面がそのぶん縦に伸びて、返却インベントリやページ送りが下に押し出される)。
     * </p>
     */
    private boolean showingCatalysts;

    public OverdriveInscriberScreen(OverdriveInscriberMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
        // クラフトユニット枠の開閉。CPU を持たない特大パターンプロバイダーでは
        // ユニット枠のスロットが 1 つも無いので、ボタンも出さない。
        catalystsToggle = new IconTextButton(() -> showingCatalysts ? Icon.BACK : Icon.S_MACHINE,
                Component.empty(), btn -> toggleCatalysts());
        widgets.add("unitsToggle", catalystsToggle);

        LOG.debug("OverdriveInscriberScreen Opened");
    }

    private void toggleCatalysts() {
        showingCatalysts = !showingCatalysts;
        layoutSlots();
    }

    /**
     * 触媒アイテムの枠を並べる。
     *
     * <p>
     * 出していない間は {@code setActive(false)} で消す
     * ({@code setSlotEnabled} ではない理由は {@link #layoutSlots()} と同じ)。
     * </p>
     */
    private void layoutCatalystSlots(boolean visible) {
        List<Slot> slots = menu.getSlots(OverdriveInscriberMenu.CATALYST_INV);
        for (int index = 0; index < slots.size(); index++) {
            Slot slot = slots.get(index);
            if (slot instanceof AppEngSlot appEngSlot) {
                appEngSlot.setActive(visible);
            }
            if (visible) {
                ((SlotAccessor) slot).insaneae$setX(GRID_LEFT + index % UNIT_COLUMNS * SLOT_SIZE);
                ((SlotAccessor) slot).insaneae$setY(STORAGE_UNITS_TOP + index / UNIT_COLUMNS * SLOT_SIZE);
            }
        }
    }

    @Override
    protected void layoutSlots() {
        layoutCatalystSlots(showingCatalysts);

        List<Slot> slots = menu.getSlots(SlotSemantics.ENCODED_PATTERN);
        int first = menu.isServerPaged() ? 0 : page * PER_PAGE;

        for (int index = 0; index < slots.size(); index++) {
            Slot slot = slots.get(index);
            int inPage = index - first;
            boolean visible = !showingCatalysts && inPage >= 0 && inPage < PER_PAGE;

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
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        // ユニット枠を出している間、パターン枠まわりの表示は引っ込める。
        prevPage.visible = !showingCatalysts;
        nextPage.visible = !showingCatalysts;
        setTextHidden("page", showingCatalysts);
        setTextHidden("storageUnitsLabel", !showingCatalysts);
        setTextContent("storageUnitsLabel",
                Component.translatable("gui.overdriveae.overdrive_inscriber.catalyst_inv_label"));
        catalystsToggle.setMessage(Component.translatable(showingCatalysts
                ? "gui.insaneae.quantum_cpu.show_patterns"
                : "gui.overdriveae.overdrive_inscriber.show_catalysts"));
    }

}
