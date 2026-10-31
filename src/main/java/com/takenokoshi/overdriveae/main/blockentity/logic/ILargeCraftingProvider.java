package com.takenokoshi.overdriveae.main.blockentity.logic;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.KeyCounter;

public interface ILargeCraftingProvider extends ICraftingProvider {
    /**
     * このtickに実行可能な回数を取得する
     * 
     * @param patternDetails
     * @return
     */
    long budget(IPatternDetails patternDetails);

    /**
     * 実際にまとめて処理する
     * inputHolderの中身を実際に処理できた分だけ減らすこと。
     * (確率消費は、実際に処理できた回数における最大消費量を減らし、別経路で実際に消費しなかった分をMEに返却すること)
     * 
     * @param patternDetails
     * @param inputHolder
     * @param times
     * @return 実際に処理できた回数
     */
    long pushPatternLarge(IPatternDetails patternDetails, KeyCounter[] inputHolder, long times);

    /**
     * 
     * @return クラフトCPUの並列数の消費数の減少が有効か
     */
    boolean compressTaskEnabled();
}
