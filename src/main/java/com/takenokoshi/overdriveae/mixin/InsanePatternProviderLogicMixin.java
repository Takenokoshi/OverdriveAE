package com.takenokoshi.overdriveae.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import appeng.api.crafting.IPatternDetails;
import jp.main.taikun.insaneae.provider.InsanePatternProviderLogic;

@Mixin(value = { InsanePatternProviderLogic.class }, remap = false)
public interface InsanePatternProviderLogicMixin {

    @Accessor("patternSet")
    Set<IPatternDetails> overdriveae$accessPatternSet();
}
