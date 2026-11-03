package com.takenokoshi.overdriveae.main.blockentity.logic;

import java.util.Set;

import appeng.api.crafting.IPatternDetails;
import appeng.crafting.inv.ICraftingInventory;

public interface IJob {

    /**
     * タスク一覧を取得する
     * @return
     */
    Set<IPatternDetails> getTasks();

    /**
     * 現在のタスク進捗を取得する
     * @param patternDetails
     * @return
     */
    long getTaskProgress(IPatternDetails patternDetails);

    /**
     * タスクを削除する
     * @param patternDetails
     */
    void removeTask(IPatternDetails patternDetails);

    /**
     * タスク進捗を更新する
     * @param patternDetails
     * @param newProgress
     */
    void setTaskProgress(IPatternDetails patternDetails, long newProgress);

    /**
     * 成果物の受け皿のインベントリを取得する
     * @return
     */
    ICraftingInventory getWaitingInv();
}
