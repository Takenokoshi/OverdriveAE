package com.takenokoshi.overdriveae.main.blockentity.prefab;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;

import appeng.api.crafting.IPatternDetails;
import net.minecraft.network.chat.Component;

/**
 * 
 * Gui用の情報をまとめたインターフェース
 */
public interface IOverdriveMachine<PATTERN extends IPatternDetails, PATTERN_CACHE> {
    OverdriveMachineLogic<PATTERN, PATTERN_CACHE> getLogic();

    Component getGuiTitle();
}
