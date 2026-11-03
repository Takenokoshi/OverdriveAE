package com.takenokoshi.overdriveae.main.screen;

import java.util.HashSet;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.takenokoshi.overdriveae.main.menu.OverdriveMachineMenu;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantic;
import appeng.menu.SlotSemantics;
import appeng.menu.slot.AppEngSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class OverdriveMachineScreen<M extends OverdriveMachineMenu> extends AEBaseScreen<M> {

    protected static final Logger LOG = LogUtils.getLogger();

    protected final BasicIconButton backToPatternSlots, openUpgradeSlots, prevPage, nextPage;

    /**
     * このスクリーンで管理するSlotSemanticのデータ
     * ここに追加していないSlotSemantic所属のSlotは影響を受けない
     */
    protected final HashSet<SlotSemanticPosData> semanticPosDatas;

    /**
     * 現在表示中のSlotSemantic
     * {@link #layoutSlots()}で参照し、
     * {@link #semanticPosDatas}に登録されたSlotSemantic全てに対して描画のon/offを切り替える
     */
    @NotNull
    protected SlotSemantic visibleSemantic;

    public OverdriveMachineScreen(M menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        this.backToPatternSlots = new BasicIconButton(this::backToPatternSlots, () -> Icon.BACK)
                .setText(Component.translatable("gui.overdriveae.element.back_to_pattern_slots"));
        this.openUpgradeSlots = new BasicIconButton(this::openUpgradeSlots, () -> Icon.BACKGROUND_UPGRADE)
                .setText(Component.translatable("gui.overdriveae.element.open_upgrade_slots"));
        this.prevPage = new BasicIconButton(this::prevPage, () -> Icon.ARROW_LEFT)
                .setText(Component.translatable("gui.overdriveae.element.prev_page"));
        this.nextPage = new BasicIconButton(this::nextPage, () -> Icon.ARROW_RIGHT)
                .setText(Component.translatable("gui.overdriveae.element.next_page"));
        widgets.addOpenPriorityButton();
        widgets.add("backToPatternSlots", backToPatternSlots);
        widgets.add("openUpgradeSlots", openUpgradeSlots);
        widgets.add("prevPage", prevPage);
        widgets.add("nextPage", nextPage);
        this.semanticPosDatas = new HashSet<>();
        semanticPosDatas.add(new SlotSemanticPosData(SlotSemantics.ENCODED_PATTERN, 8, 22, 9));
        semanticPosDatas.add(new SlotSemanticPosData(SlotSemantics.UPGRADE, 8, 34, 9));
        this.visibleSemantic = SlotSemantics.ENCODED_PATTERN;
    }

    @Override
    protected void init() {
        super.init();
        layoutSlots();
    }

    protected void prevPage(Button btn) {
        menu.prevPage();
        layoutSlots();
    }

    protected void nextPage(Button btn) {
        menu.nextPage();
        layoutSlots();
    }

    protected void backToPatternSlots(Button btn) {
        setVisibleSemantic(SlotSemantics.ENCODED_PATTERN);
    }

    protected void openUpgradeSlots(Button btn) {
        setVisibleSemantic(SlotSemantics.UPGRADE);
    }

    protected void setVisibleSemantic(SlotSemantic visibleSemantic) {
        this.visibleSemantic = visibleSemantic;
        layoutSlots();
    }

    protected void layoutSlots() {
        for (SlotSemanticPosData posData : semanticPosDatas) {
            if (posData.semantic().id().equals(visibleSemantic.id())) {
                List<Slot> slots = menu.getSlots(visibleSemantic);
                int leftPos = posData.leftPos(), topPos = posData.topPos(), columns = posData.columns();
                for (int i = 0; i < slots.size(); i++) {
                    Slot slot = slots.get(i);
                    if (slot instanceof AppEngSlot appEngSlot) {
                        appEngSlot.setActive(true);
                    }
                    // accesstransformerでfinal制限を外している
                    slot.x = leftPos + 18 * (i % columns);
                    slot.y = topPos + 18 * (i / columns);
                }
            } else {
                for (Slot slot : menu.getSlots(posData.semantic())) {
                    if (slot instanceof AppEngSlot appEngSlot) {
                        appEngSlot.setActive(false);
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double deltaX, double deltaY) {
        if (super.mouseScrolled(x, y, deltaX, deltaY)) {
            return true;
        }
        if (deltaY != 0 && isOverPatternGrid(x, y)) {
            if (deltaY > 0) {
                menu.nextPage();
                layoutSlots();
            } else {
                menu.prevPage();
                layoutSlots();
            }
            return true;
        }
        return false;
    }

    protected boolean isOverPatternGrid(double x, double y) {
        int left = leftPos + 8;
        int top = topPos + 22;
        return x >= left && x < left + 162
                && y >= top && y < top + 324;
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();

        setTextContent("dialog_title", this.menu.host.getGuiTitle());
        boolean pageVisible = visibleSemantic.id().equals(SlotSemantics.ENCODED_PATTERN.id());
        setTextHidden("page", !pageVisible);
        prevPage.active = pageVisible;
        nextPage.active = pageVisible;
        setTextContent("page", Component.literal((menu.getPage() + 1) + "/30"));
    }

    @Override
    public void drawBG(GuiGraphics guiGraphics, int offsetX, int offsetY, int mouseX, int mouseY, float partialTicks) {
        super.drawBG(guiGraphics, offsetX, offsetY, mouseX, mouseY, partialTicks);
        for (Slot slot : menu.getSlots(visibleSemantic)) {
            Icon.SLOT_BACKGROUND.getBlitter()
                    .dest(offsetX + slot.x - 1, offsetY + slot.y - 1)
                    .blit(guiGraphics);
        }
        for (Slot slot : menu.getSlots(SlotSemantics.PLAYER_HOTBAR)) {
            Icon.SLOT_BACKGROUND.getBlitter()
                    .dest(offsetX + slot.x - 1, offsetY + slot.y - 1)
                    .blit(guiGraphics);
        }
        for (Slot slot : menu.getSlots(SlotSemantics.PLAYER_INVENTORY)) {
            Icon.SLOT_BACKGROUND.getBlitter()
                    .dest(offsetX + slot.x - 1, offsetY + slot.y - 1)
                    .blit(guiGraphics);
        }
    }

}
