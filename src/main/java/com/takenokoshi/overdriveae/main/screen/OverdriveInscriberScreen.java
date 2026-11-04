package com.takenokoshi.overdriveae.main.screen;

import com.takenokoshi.overdriveae.main.menu.OverdriveInscriberMenu;

import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class OverdriveInscriberScreen extends OverdriveMachineScreen<OverdriveInscriberMenu> {

    private final BasicIconButton openCatalystSlots;

    public OverdriveInscriberScreen(OverdriveInscriberMenu menu, Inventory playerInventory, Component title,
            ScreenStyle style) {
        super(menu, playerInventory, title, style);
        semanticPosDatas.add(new SlotSemanticPosData(OverdriveInscriberMenu.CATALYST_INV, 8, 34, 9));
        openCatalystSlots = new BasicIconButton(this::openCatalystSlots, () -> Icon.BACKGROUND_PLATE)
                .setText(Component.translatable("gui.overdriveae.element.open_catalyst_slots"));
        widgets.add("openCatalystSlots", openCatalystSlots);
    }

    protected void openCatalystSlots(Button btn) {
        setVisibleSemantic(OverdriveInscriberMenu.CATALYST_INV);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        boolean catalystSlotsVisible = visibleSemantic.id().equals(OverdriveInscriberMenu.CATALYST_INV.id());
        openCatalystSlots.active = !catalystSlotsVisible;
        openCatalystSlots.visible = !catalystSlotsVisible;
        setTextHidden("catalystSlotsLabel", !catalystSlotsVisible);
    }

}
