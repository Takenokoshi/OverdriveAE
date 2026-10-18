package com.takenokoshi.overdriveae.blockentity.prefab;

import java.util.Set;

import com.takenokoshi.overdriveae.mixin.InsanePatternProviderLogicMixin;

import appeng.api.crafting.IPatternDetails;
import jp.main.taikun.insaneae.provider.InsanePatternProviderLogic;
/** Mixinブリッジ */
public class InsanePatternProviderLogicAccess {
    public static Set<IPatternDetails> accessPatternSet(InsanePatternProviderLogic logic) {
        return ((InsanePatternProviderLogicMixin) logic).overdriveae$accessPatternSet();
    }
}
