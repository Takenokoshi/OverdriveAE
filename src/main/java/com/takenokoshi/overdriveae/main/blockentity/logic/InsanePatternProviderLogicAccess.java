package com.takenokoshi.overdriveae.main.blockentity.logic;

import java.util.Set;

import com.takenokoshi.overdriveae.main.mixin.InsanePatternProviderLogicMixin;

import appeng.api.crafting.IPatternDetails;
import jp.main.taikun.insaneae.provider.InsanePatternProviderLogic;

/** Mixinブリッジ */
public class InsanePatternProviderLogicAccess {
    public static Set<IPatternDetails> accessPatternSet(InsanePatternProviderLogic logic) {
        return ((InsanePatternProviderLogicMixin) logic).overdriveae$accessPatternSet();
    }

    public static void setPatternDirty(InsanePatternProviderLogic logic, boolean value) {
        ((InsanePatternProviderLogicMixin) logic).overdriveae$setPatternsDirty(value);
    }

    public static boolean getPatternDirty(InsanePatternProviderLogic logic){
        return ((InsanePatternProviderLogicMixin) logic).overdriveae$getPatternsDirty();
    }
}
