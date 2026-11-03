package com.takenokoshi.overdriveae.main.blockentity.logic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

import org.jetbrains.annotations.Nullable;

import com.takenokoshi.overdriveae.main.blockentity.prefab.BigGenericHolder;
import com.takenokoshi.overdriveae.main.blockentity.prefab.OverdriveAEInv;
import com.takenokoshi.overdriveae.main.util.LRUCacheMap;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class OverdriveMachineLogic<PATTERN extends IPatternDetails, PATTERN_CACHE>
        implements IOverdriveCraftingProvider {

    public static final int PATTERN_INV_SIZE = 1_620;
    public static final String NBT_PATTERN_INV = "patternInv", NBT_OUTPUT_HOLDER = "outputHolder";

    public final OverdriveMachineLogicHost<PATTERN, PATTERN_CACHE> host;
    public final IManagedGridNode mainNode;
    public final Class<PATTERN> patternClass;
    protected final PATTERN[][] decodedPatterns;
    protected final IntFunction<PATTERN[]> patternArrayConstructor;
    protected final Set<IPatternDetails> availablePatterns;
    protected final OverdriveAEInv patternInv;
    protected final LRUCacheMap<PATTERN, PATTERN_CACHE> patternCacheMap;
    protected final BigGenericHolder outputHolder;
    protected final IActionSource actionSource;

    protected boolean patternsDirty, availablePatternsDirty;

    protected long lastDecodeTick, lastUpdateTick;

    public OverdriveMachineLogic(
            OverdriveMachineLogicHost<PATTERN, PATTERN_CACHE> host,
            IManagedGridNode mainNode,
            Class<PATTERN> patternClass,
            IntFunction<PATTERN[][]> pattern2DArrayConstructor,
            IntFunction<PATTERN[]> patternArrayConstructor,
            IActionSource actionSource) {
        this.host = host;
        this.mainNode = mainNode
                .setFlags(GridFlags.REQUIRE_CHANNEL)
                .addService(ICraftingProvider.class, this);
        this.patternClass = patternClass;
        this.decodedPatterns = pattern2DArrayConstructor.apply(PATTERN_INV_SIZE);
        this.patternArrayConstructor = patternArrayConstructor;
        PATTERN[] emptyPatternArray = this.patternArrayConstructor.apply(0);
        Arrays.fill(this.decodedPatterns, emptyPatternArray);
        this.availablePatterns = new HashSet<>();
        this.patternInv = new OverdriveAEInv(PATTERN_INV_SIZE, this::onPatternInvChange);
        this.patternInv.setFilter(this.host::isPatternAcceptable);
        this.patternCacheMap = new LRUCacheMap<>(16, 0.75f, true, 64);
        this.outputHolder = new BigGenericHolder();

        this.lastDecodeTick = Long.MIN_VALUE;
        this.lastUpdateTick = Long.MIN_VALUE;
        this.actionSource = actionSource;
    }

    public OverdriveAEInv getPatternInv() {
        return patternInv;
    }

    public boolean isOutputEmpty() {
        return outputHolder.isEmpty();
    }

    public void setPatternInvCapacity(AEKeyType space, long capacity) {
        patternInv.setCapacity(space, capacity);
    }

    public void markPatternsDirty() {
        patternsDirty = true;
        if (lastDecodeTick != host.currentTick()) {
            updatePatterns();
        }
    }

    public void markAvailablePatternsDirty() {
        availablePatternsDirty = true;
        if (lastUpdateTick != host.currentTick()) {
            updateAvailablePatterns();
        }
    }

    public void updatePatterns() {
        if (patternsDirty) {
            for (int i = 0; i < PATTERN_INV_SIZE; i++) {
                decodedPatterns[i] = host.decodePattern(patternInv.getStack(i));
            }
            patternsDirty = false;
            availablePatternsDirty = true;
            lastDecodeTick = host.currentTick();
        }
    }

    protected void onPatternInvChange(int slotIndex, @Nullable GenericStack stack) {
        this.decodedPatterns[slotIndex] = host.decodePattern(stack);
        markAvailablePatternsDirty();
        host.saveChanges();
    }

    public void updateAvailablePatterns() {
        updatePatterns();
        if (availablePatternsDirty) {
            this.availablePatterns.clear();
            for (PATTERN[] patterns : this.decodedPatterns) {
                for (PATTERN pattern : patterns) {
                    availablePatterns.add(pattern);
                }
            }
            availablePatternsDirty = false;
            lastUpdateTick = host.currentTick();
            patternCacheMap.clear();
            ICraftingProvider.requestUpdate(this.mainNode);
        }
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        updateAvailablePatterns();
        return List.copyOf(availablePatterns);
    }

    @Override
    public boolean isBusy() {
        return host.isBusy();
    }

    @Override
    public long budget(IPatternDetails patternDetails) {
        Level level = host.getLevel();
        if (level == null || level.isClientSide()) {
            return 0L;
        }
        if (!mainNode.isActive() || !hasPattern(patternDetails)) {
            return 0L;
        }
        if (host.isBusy()) {
            return 0L;
        }
        if (!patternClass.isInstance(patternDetails)) {
            return 0L;
        }
        return host.budget(patternClass.cast(patternDetails));
    }

    protected final boolean hasPattern(IPatternDetails patternDetails) {
        updateAvailablePatterns();
        return availablePatterns.contains(patternDetails);
    }

    protected PATTERN_CACHE getCache(PATTERN pattern, KeyCounter[] inputHolder) {
        PATTERN_CACHE cache = patternCacheMap.get(pattern);
        if (cache == null || !host.matchesCache(cache, inputHolder)) {
            cache = host.createCache(pattern, inputHolder);
            patternCacheMap.put(pattern, cache);
        }
        return cache;
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        Level level = host.getLevel();
        if (level == null || level.isClientSide()) {
            return false;
        }
        if (!mainNode.isActive() || !hasPattern(patternDetails)) {
            return false;
        }
        if (!patternClass.isInstance(patternDetails)) {
            return false;
        }
        PATTERN pattern = patternClass.cast(patternDetails);
        if (host.budget(pattern) <= 0L) {
            return false;
        }
        PATTERN_CACHE cache = getCache(pattern, inputHolder);
        host.addOutputsToHolder(pattern, cache, 1L, outputHolder);
        host.consumeInputs(pattern, cache, 1L, inputHolder);
        return true;
    }

    @Override
    public long pushPatternOverdrive(IPatternDetails patternDetails, KeyCounter[] inputHolder, long times) {
        Level level = host.getLevel();
        if (level == null || level.isClientSide()) {
            return 0L;
        }
        if (!mainNode.isActive() || !hasPattern(patternDetails)) {
            return 0L;
        }
        if (!patternClass.isInstance(patternDetails)) {
            return 0L;
        }
        PATTERN pattern = patternClass.cast(patternDetails);
        long budget = host.budget(pattern);
        if (budget < times) {
            PATTERN_CACHE cache = getCache(pattern, inputHolder);
            host.addOutputsToHolder(pattern, cache, budget, outputHolder);
            host.consumeInputs(pattern, cache, budget, inputHolder);
            host.onProcessAdditional(pattern, cache, budget);
            return budget;
        } else {
            PATTERN_CACHE cache = getCache(pattern, inputHolder);
            host.addOutputsToHolder(pattern, cache, times, outputHolder);
            for (KeyCounter counter : inputHolder) {
                counter.clear();
            }
            host.onProcessAdditional(pattern, cache, times);
            return times;
        }
    }

    public boolean sendOutputs() {
        if (outputHolder.isEmpty()) {
            return false;
        }
        IGrid grid = mainNode.getGrid();
        if (grid == null || !mainNode.isActive()) {
            return false;
        }
        MEStorage storage = grid.getStorageService().getInventory();
        for (AEKey key : outputHolder.keySet()) {
            long amount = outputHolder.drain(key, Long.MAX_VALUE);
            if (amount <= 0L) {
                continue;
            }
            long inserted = storage.insert(key, amount, Actionable.MODULATE, actionSource);
            if (inserted < amount) {
                outputHolder.add(key, amount - inserted);
            }
        }
        return true;
    }

    public void readFromTag(CompoundTag data, String name, HolderLookup.Provider registries) {
        CompoundTag tag = data.getCompound(name);
        patternInv.readFromChildTag(tag, NBT_PATTERN_INV, registries);
        outputHolder.readFromNBT(tag, NBT_OUTPUT_HOLDER, registries);
    }

    public void writeToNBT(CompoundTag data, String name, HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        patternInv.writeToChildTag(tag, NBT_PATTERN_INV, registries);
        outputHolder.writeToNBT(tag, NBT_OUTPUT_HOLDER, registries);
        data.put(name, tag);
    }

    @Override
    public boolean coprocessorUsageReductionEnabled() {
        return host.coprocessorUsageReductionEnabled();
    }

    public void addDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        patternInv.addDrops(drops, level, pos);
        outputHolder.addDrops(drops, level, pos);
    }

    public void clearContent() {
        patternInv.clear();
        outputHolder.clear();
    }

}
