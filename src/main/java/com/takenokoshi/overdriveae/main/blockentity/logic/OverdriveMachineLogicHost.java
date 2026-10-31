package com.takenokoshi.overdriveae.main.blockentity.logic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.main.blockentity.prefab.BigGenericHolder;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import net.minecraft.world.level.Level;

public interface OverdriveMachineLogicHost<PATTERN extends IPatternDetails, PATTERN_CACHE> {

    /**
     * GenericStackからパターンを読み取る
     * 一つから複数のパターンを生成してもよい(Mekanismの電解分離機等)
     * 
     * @param stack pattern
     * @return Zero-size Array or decoded Patterns Array
     */
    @NotNull
    PATTERN[] decodePattern(@Nullable GenericStack stack);

    /**
     * パターンとして有効か確認する
     * 
     * @param key
     * @return {@code true} if {@code key} is a pattern.
     */
    boolean isPatternAcceptable(@Nullable AEKey key);

    /**
     * パターンに対してこのtickの最大実行回数を返す
     * エネルギーを消費するなら、ここで計算しておく
     * 
     * @param pattern
     * @return
     */
    long budget(@NotNull PATTERN pattern);

    /**
     * @return 現在のゲーム内時刻
     */
    long currentTick();

    /**
     * @return このTickにどんなパターンも処理できないなら{@code true}
     */
    boolean isBusy();

    Level getLevel();

    /**
     * パターンとそれにあった材料を元にパターンキャッシュを作成する
     * 
     * @param pattern     パターン
     * @param inputHolder 材料(パターン自体は材料を一意に特定しない)
     * @return パターンキャッシュ
     */
    @NotNull
    PATTERN_CACHE createCache(@NotNull PATTERN pattern, @NotNull KeyCounter[] inputHolder);

    /**
     * パターンキャッシュと材料が一致するか確認する
     * 
     * @param cache       パターンキャッシュ
     * @param inputHolder 材料
     * @return
     */
    boolean matchesCache(@NotNull PATTERN_CACHE cache, @NotNull KeyCounter[] inputHolder);

    /**
     * 回数分以上の材料を、回数分のみ消費する
     * 
     * @param pattern     パターン
     * @param cache       パターンキャッシュ
     * @param times       回数
     * @param inputHolder 材料(これの中身を減らす)
     */
    void consumeInputs(@NotNull PATTERN pattern, @NotNull PATTERN_CACHE cache, long times, KeyCounter[] inputHolder);

    /**
     * パターンキャッシュを基に、回数分の成果物を搬出リストに追加する。
     * 材料の確率消費は、消費しなかった分をここで搬出リストに加える
     * サブ成果物の確率生成は、パターンには反映せず、ここで生成分を搬出リストに加える
     * エネルギーの消費がある場合、ここで消費する
     * 
     * @param pattern      パターン
     * @param cache        パターンキャッシュ
     * @param times        回数
     * @param outputHolder 搬出リスト(この中身を1tickに一度まとめてME倉庫に搬出する。)
     */
    void addOutputsToHolder(@NotNull PATTERN pattern, @NotNull PATTERN_CACHE cache, long times,
            BigGenericHolder outputHolder);

    void saveChanges();

    /**
     * 
     * @return クラフトCPUの並列数の消費数の減少が有効か
     */
    boolean compressTaskEnebled();

}
