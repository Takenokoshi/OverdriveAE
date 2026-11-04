package com.takenokoshi.overdriveae.main.screen;

import com.takenokoshi.overdriveae.main.menu.OverdriveTransformChamberMenu;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class OverdriveTransformChamberScreen extends OverdriveMachineScreen<OverdriveTransformChamberMenu> {

    private final BasicIconButton openCatalystSlots;
    public OverdriveTransformChamberScreen(OverdriveTransformChamberMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
        semanticPosDatas.add(new SlotSemanticPosData(OverdriveTransformChamberMenu.CATALYST_INV, 8, 34, 9));
        openCatalystSlots = new BasicIconButton(this::openCatalystSlots, () -> Icon.TYPE_FILTER_ALL)
                .setText(Component.translatable("gui.overdriveae.element.open_catalyst_slots"));
        widgets.add("openCatalystSlots", openCatalystSlots);
    }

    protected void openCatalystSlots(Button btn) {
        setVisibleSemantic(OverdriveTransformChamberMenu.CATALYST_INV);
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        boolean catalystSlotsVisible = visibleSemantic.id().equals(OverdriveTransformChamberMenu.CATALYST_INV.id());
        openCatalystSlots.active = !catalystSlotsVisible;
        openCatalystSlots.visible = !catalystSlotsVisible;
        setTextHidden("catalystSlotsLabel", !catalystSlotsVisible);
    }

}
