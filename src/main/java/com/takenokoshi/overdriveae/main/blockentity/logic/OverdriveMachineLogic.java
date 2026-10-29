package com.takenokoshi.overdriveae.main.blockentity.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveAEInv;

import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.helpers.externalstorage.GenericStackInv;

public class OverdriveMachineLogic<PATTERN extends IPatternDetails> implements ICraftingProvider, IGridTickable {

    public static final int PATTERN_INV_SIZE = 1_620;

    protected final OverdriveMachineLogicHost<PATTERN> host;
    protected final IManagedGridNode mainNode;
    protected final Class<PATTERN> patternClass;
    protected final PATTERN[][] decodedPatterns;
    protected final IntFunction<PATTERN[]> patternArrayConstructor;
    protected final List<IPatternDetails> availablePatterns;
    protected final OverdriveAEInv patternInv;

    GenericStackInv inv;

    protected boolean needDecodeAgain;
    protected boolean needUpdateAvailablePatterns;

    public OverdriveMachineLogic(
            OverdriveMachineLogicHost<PATTERN> host,
            IManagedGridNode mainNode,
            Class<PATTERN> patternClass,
            IntFunction<PATTERN[][]> pattern2DArrayConstructor,
            IntFunction<PATTERN[]> patternArrayConstructor) {
        this.host = host;
        this.mainNode = mainNode
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(IGridTickable.class, this)
                .addService(ICraftingProvider.class, this);
        this.patternClass = patternClass;
        this.decodedPatterns = pattern2DArrayConstructor.apply(PATTERN_INV_SIZE);
        this.patternArrayConstructor = patternArrayConstructor;
        PATTERN[] emptyPatternArray = this.patternArrayConstructor.apply(0);
        Arrays.fill(this.decodedPatterns, emptyPatternArray);
        this.availablePatterns = new ArrayList<>();
        this.patternInv = new OverdriveAEInv(null, PATTERN_INV_SIZE, this::onPatternInvChange);
        this.patternInv.setFilter(host::isPatternAcceptable);
    }

    protected void needDecodeAllPatternIfNeed() {
        if (needDecodeAgain) {
            for (int i = 0; i < PATTERN_INV_SIZE; i++) {
                decodedPatterns[i] = host.decodePattern(patternInv.getStack(i));
            }
            needDecodeAgain = false;
        }
    }

    protected void onPatternInvChange(int slotIndex, @Nullable GenericStack stack) {
        this.decodedPatterns[slotIndex] = host.decodePattern(stack);
        needUpdateAvailablePatterns = true;
    }

    protected void updateAvailablePatternsIfNeed() {
        if (needUpdateAvailablePatterns) {
            this.availablePatterns.clear();
            for (PATTERN[] patterns : this.decodedPatterns) {
                for (PATTERN pattern : patterns) {
                    availablePatterns.add(pattern);
                }
            }
            needUpdateAvailablePatterns = false;
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        updateAvailablePatternsIfNeed();
        return availablePatterns;
    }

    @Override
    public boolean isBusy() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isBusy'");
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pushPattern'");
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTickingRequest'");
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'tickingRequest'");
    }
}
