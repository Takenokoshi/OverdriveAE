package com.takenokoshi.overdriveae.main.screen;

import java.util.List;

import com.takenokoshi.overdriveae.main.menu.OverdriveTransformChamberMenu;

import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.slot.AppEngSlot;
import jp.main.taikun.insaneae.mixin.SlotAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class OverdriveTransformChamberScreen extends OverdriveMachineScreen<OverdriveTransformChamberMenu> {

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
    public OverdriveTransformChamberScreen(OverdriveTransformChamberMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        // クラフトユニット枠の開閉。
        catalystsToggle = new IconTextButton(() -> showingCatalysts ? Icon.BACK : Icon.S_MACHINE,
                Component.empty(), btn -> toggleCatalysts());
        widgets.add("unitsToggle", catalystsToggle);
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
        List<Slot> slots = menu.getSlots(OverdriveTransformChamberMenu.CATALYST_INV);
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
        layoutPatternSlots(!showingCatalysts);
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
                Component.translatable("gui.overdriveae.overdrive_transform_chamber.catalyst_inv_label"));
        catalystsToggle.setMessage(Component.translatable(showingCatalysts
                ? "gui.insaneae.quantum_cpu.show_patterns"
                : "gui.overdriveae.overdrive_transform_chamber.show_catalysts"));
    }

}
