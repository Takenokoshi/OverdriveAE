package com.takenokoshi.overdriveae.main.blockentity.logic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;

public interface OverdriveMachineLogicHost<PATTERN extends IPatternDetails> {

    /**
     * GenericStackからパターンを読み取る
     * 
     * @param stack pattern
     * @return Zero-size Array or decoded Patterns Array
     */
    @NotNull
    PATTERN[] decodePattern(@Nullable  GenericStack stack);

    /**
     * GenericStackがパターンとして有効か確認する
     * @param key 
     * @return {@code true} if {@code key} is a pattern.
     */
    boolean isPatternAcceptable(AEKey key);

}
