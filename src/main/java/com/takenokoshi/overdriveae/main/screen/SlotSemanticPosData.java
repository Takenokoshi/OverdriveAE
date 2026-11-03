package com.takenokoshi.overdriveae.main.screen;

import appeng.menu.SlotSemantic;

public record SlotSemanticPosData(SlotSemantic semantic, int leftPos, int topPos,int columns) {
    @Override
    public final int hashCode() {
        return semantic.hashCode() * 961 + leftPos * 31 + topPos;
    }
}
