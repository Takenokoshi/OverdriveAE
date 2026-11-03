package com.takenokoshi.overdriveae.main.blockentity.prefab;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogicHost;

import appeng.api.crafting.IPatternDetails;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.menu.ISubMenu;
import appeng.menu.locator.MenuHostLocator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public interface IOverdriveMachine<PATTERN extends IPatternDetails, PATTERN_CACHE> extends OverdriveMachineLogicHost<PATTERN,PATTERN_CACHE>,IUpgradeableObject {
    OverdriveMachineLogic<PATTERN, PATTERN_CACHE> getLogic();

    Component getGuiTitle();

    void openMenu(Player player, MenuHostLocator locator);

    void returnToMainMenu(Player player, ISubMenu subMenu);
}
