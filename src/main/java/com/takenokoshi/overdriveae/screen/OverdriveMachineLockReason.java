package com.takenokoshi.overdriveae.screen;

import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.menu.OverdriveMachineMenu;

import appeng.api.client.AEKeyRendering;
import appeng.api.config.LockCraftingMode;
import appeng.api.stacks.AmountFormat;
import appeng.client.Point;
import appeng.client.gui.ICompositeWidget;
import appeng.client.gui.Icon;
import appeng.client.gui.Tooltip;
import appeng.core.localization.GuiText;
import appeng.core.localization.InGameTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;

public class OverdriveMachineLockReason implements ICompositeWidget {
   protected boolean visible = false;
   protected int x;
   protected int y;
   private final OverdriveMachineScreen<?> screen;

   public OverdriveMachineLockReason(OverdriveMachineScreen<?> screen){
    this.screen = screen;}

   public void setPosition(Point position) {
      this.x = position.getX();
      this.y = position.getY();
   }

   public void setSize(int width, int height) {
   }

   public Rect2i getBounds() {
      return new Rect2i(this.x, this.y, 126, 16);
   }

   public final boolean isVisible() {
      return this.visible;
   }

   public void setVisible(boolean visible) {
      this.visible = visible;
   }

   public void drawForegroundLayer(GuiGraphics guiGraphics, Rect2i bounds, Point mouse) {
      OverdriveMachineMenu menu = this.screen.getMenu();
      Icon icon;
      Component lockStatusText;
      if (menu.getCraftingLockedReason() == LockCraftingMode.NONE) {
         icon = Icon.UNLOCKED;
         lockStatusText = GuiText.CraftingLockIsUnlocked.text().setStyle(Style.EMPTY.withColor(Mth.color(0.49019608F, 0.6627451F, 0.8235294F)));
      } else {
         icon = Icon.LOCKED;
         lockStatusText = GuiText.CraftingLockIsLocked.text().setStyle(Style.EMPTY.withColor(Mth.color(0.75686276F, 0.25882354F, 0.29411766F)));
      }

      icon.getBlitter().dest(this.x, this.y).blit(guiGraphics);
      int var10003 = this.x + 15;
      int var10004 = this.y + 5;
      guiGraphics.drawString(Minecraft.getInstance().font, lockStatusText, var10003, var10004, -1, false);
   }

    @Nullable
    @Override
    public Tooltip getTooltip(int mouseX, int mouseY) {
        var menu = screen.getMenu();
        var tooltip = switch (menu.getCraftingLockedReason()) {
            case NONE -> null;
            case LOCK_UNTIL_PULSE -> InGameTooltip.CraftingLockedUntilPulse.text();
            case LOCK_WHILE_HIGH -> InGameTooltip.CraftingLockedByRedstoneSignal.text();
            case LOCK_WHILE_LOW -> InGameTooltip.CraftingLockedByLackOfRedstoneSignal.text();
            case LOCK_UNTIL_RESULT -> {
                var stack = menu.getUnlockStack();
                Component stackName;
                Component stackAmount;
                if (stack != null) {
                    stackName = AEKeyRendering.getDisplayName(stack.what());
                    stackAmount = Component.literal(stack.what().formatAmount(stack.amount(), AmountFormat.FULL));
                } else {
                    stackName = Component.literal("ERROR");
                    stackAmount = Component.literal("ERROR");

                }
                yield InGameTooltip.CraftingLockedUntilResult.text(stackName, stackAmount);
            }
        };

        return tooltip != null ? new Tooltip(tooltip) : null;
    }
}
