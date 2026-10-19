/*
 *Some part of this file is copy of https://github.com/taikun24/InsaneAE/blob/1.21.1/src/main/java/jp/main/taikun/insaneae/quantum/QuantumCpuLogic.java
 */

package com.takenokoshi.overdriveae.blockentity.prefab;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;

import appeng.api.config.LockCraftingMode;
import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IManagedGridNode;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import jp.main.taikun.insaneae.menu.PatternPagingHandshake;
import jp.main.taikun.insaneae.mixin.PatternProviderLogicAccessor;
import jp.main.taikun.insaneae.provider.InsanePatternProviderLogic;
import jp.main.taikun.insaneae.quantum.IBulkCraftingProvider;
import jp.main.taikun.insaneae.quantum.QuantumCpuBlockEntity;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class OverdriveMachineLogic<ASSEMBLY extends OverdriveMachineLogic.Assembly, PATTERN extends IPatternDetails, MACHINE extends OverdriveMachineBlockEntity<ASSEMBLY, PATTERN, MACHINE>>
        extends InsanePatternProviderLogic implements IBulkCraftingProvider {

    protected static final Logger LOGGER = LogUtils.getLogger();
    /** 組み立て結果を覚えておくパターンの数。同時に走る小レシピの数より多ければよい。 */
    protected static final int ASSEMBLY_CACHE_SIZE = 64;

    /**
     * 1 回の組み立ての結果。<b>材料の並びも一緒に覚える</b>のは、同じパターンでも
     * 代替材料が違えば完成品が変わりうるため ({@link #matchesCache} で照合する)。
     */
    public static interface Assembly {
        GenericStack[] inputs();

        GenericStack[] outputs();
    }

    public static final String NBT_OVERDRIVE_PATTERN_INV = "overdrivePatterns";

    protected final MACHINE host;
    protected final IManagedGridNode mainNode;
    protected final Class<PATTERN> patternClass;

    /**
     * 実際に組み立てた結果を<b>パターンごとに</b>覚えておく表。
     *
     * <p>
     * 以前はここが 1 枠しか無く、直前に組んだパターンとしか照合できなかった。
     * ところが 1 つのクラフトジョブには普通いくつもの小レシピが同時に走っていて、
     * CPU 側はタスクの表を舐めながら順に押し出してくる。<b>別のパターンが交互に来ると
     * 1 枠のキャッシュは毎回無効化される</b>ので、命中率が 0 に落ちて
     * {@link #assembleAndCache} を毎クラフト走らせていた (レシピの実行と
     * {@code fireAutoCraftingEvent} の発火が丸ごと乗る)。
     * </p>
     *
     * <p>
     * 上限付きの LRU にしてある。パターンは 1620 枠あるが、<b>同時に走る小レシピは
     * せいぜい数十</b>なので {@link #ASSEMBLY_CACHE_SIZE} で足りる。
     * </p>
     */
    protected final Map<PATTERN, ASSEMBLY> assemblyCache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<PATTERN, ASSEMBLY> eldest) {
            return size() > ASSEMBLY_CACHE_SIZE;
        }
    };

    /** この tick の残り組み立て回数。 */
    protected long remainingCrafts;
    protected long budgetTick = Long.MIN_VALUE;

    /** この tick の残り消費可能エネルギー(AE)。 */
    protected double remainingEnergy;
    protected long energyTick = Long.MIN_VALUE;

    protected boolean warnedAboutFailedAssembly;
    /** AE2のパターンアイテム以外をパターンとして使用できるよう、独自のパターンインベントリを保持。 */
    protected final OverdrivePatternInv patternInv;
    protected final PATTERN[] patterns;

    public OverdriveMachineLogic(IManagedGridNode mainNode, MACHINE host,
            IntFunction<PATTERN[]> patternArrayConstructor, Class<PATTERN> patternClass) {
        /** InsanePatternProviderLogic側のパターンスロットは不要なので1枠のみにしておく。バグらないなら0でいい。 */
        super(mainNode, host, 1);
        this.host = host;
        this.mainNode = mainNode;
        this.patternClass = patternClass;
        this.patternInv = new OverdrivePatternInv(host, QuantumCpuBlockEntity.PATTERN_SLOTS,
                this.host::isPatternAcceptable, this::updatePattern);
        this.patterns = patternArrayConstructor.apply(QuantumCpuBlockEntity.PATTERN_SLOTS);
    }

    @Override
    public InternalInventory getPatternInv() {
        return PatternPagingHandshake.windowFor(host, patternInv);
    }

    @Override
    public void writeToNBT(CompoundTag tag, Provider registries) {
        super.writeToNBT(tag, registries);
        /** 独自のパターンインベントリの保存 */
        patternInv.writeToNBT(tag, NBT_OVERDRIVE_PATTERN_INV, registries);
    }

    @Override
    public void readFromNBT(CompoundTag tag, Provider registries) {
        super.readFromNBT(tag, registries);
        /** 独自のパターンインベントリの読み出し */
        patternInv.readFromNBT(tag, NBT_OVERDRIVE_PATTERN_INV, registries);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        patternInv.clear();
    }

    /**
     * 独自のパターンインベントリの更新メソッド
     * パターンキャッシュは対応するもののみ更新
     */
    protected void updatePattern(int index, ItemStack stack) {
        if (stack.isEmpty()) {
            patterns[index] = null;
        } else if (host.isPatternAcceptable(stack)) {
            patterns[index] = host.tryDecodePattern(stack);
        } else {
            patterns[index] = null;
        }
        /**
         * InsanePatternProviderLogic側のパターン更新を呼ぶ。
         */
        updatePatterns();
    }

    /**
     * 独自のパターンインベントリの更新メソッド
     * 外部から実行し、全て更新させる用
     */
    public void updateAllPatterns() {
        for (int i = 0; i < patterns.length; i++) {
            ItemStack stack = patternInv.getStackInSlot(i);
            if (stack.isEmpty()) {
                patterns[i] = null;
            } else if (host.isPatternAcceptable(stack)) {
                patterns[i] = host.tryDecodePattern(stack);
            } else {
                patterns[i] = null;
            }
        }
        /**
         * InsanePatternProviderLogic側のパターン更新を呼ぶ。
         */
        updatePatterns();
    }

    /**
     * ME倉庫側に渡すパターン。
     * パターンキャッシュは実在するレシピによる検証が行われているものである為、
     * パターンインベントリの中身ではなくパターンキャッシュから渡す。
     * 実行毎にリストを生成しているが、InsanePatternProviderLogic 側で
     * ME倉庫側の更新が1tickに最大1回に抑えられているのでこれで問題ない。
     */
    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        /**
         * InsanePatternProviderLogic#getAvailablePatterns()を呼ばないのでInsanePatternProviderLogic#flushPatternUpdate()をこちらで呼ぶ。
         */
        flushPatternUpdate();
        List<IPatternDetails> result = new ArrayList<>();
        for (PATTERN cache : patterns) {
            if (cache != null) {
                result.add(cache);
            }
        }
        return Collections.unmodifiableList(result);
    }

    // -------------------------------------------------------------- パターン管理

    @Override
    protected void onPatternsFlushed() {
        super.onPatternsFlushed();
        /** InsanePatternProviderLogic.patternSetの中身をこちらで置き換えてhasPatternの返り値を矯正する。 */
        /** Mixinを介してInsanePatternProviderLogicAccessのprivateフィールドを持ってくる */
        Set<IPatternDetails> patternSet = InsanePatternProviderLogicAccess.accessPatternSet(this);
        patternSet.clear();
        for (PATTERN cache : patterns) {
            if (cache != null) {
                patternSet.add(cache);
            }
        }
        /**
         * パターン一覧が変わった: レシピ自体が差し替わっている可能性がある
         * (データパックの再読み込みなど) ので、覚えている組み立て結果は捨てる。
         */
        assemblyCache.clear();
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
        if (getCraftingLockedReason() != LockCraftingMode.NONE) {
            return false;
        }
        if (budget() <= 0) {
            return false;
        }
        if (!patternClass.isInstance(patternDetails)) {
            return false;
        }
        ASSEMBLY assembly = resolveAssembly(patternClass.cast(patternDetails), inputHolder);
        if (assembly == null || energyBudget(assembly) <= 0) {
            return true;
        }
        remainingCrafts--;
        double v = host.getEnergyPerProcess(assembly);
        remainingEnergy -= v;
        consumeInputs(assembly, inputHolder);
        host.addUsedEnergyThisTick(v);
        storeOutputs(assembly, 1L);
        ((PatternProviderLogicAccessor) this).insaneae$onPushPatternSuccess(patternDetails);
        return super.pushPattern(patternDetails, inputHolder);
    }

    // ------------------------------------------------------------ まとめ処理

    /**
     * この tick にあとどれだけまとめて組めるか。
     */

    @Override
    public long getBulkCapacity(IPatternDetails details) {
        Level level = host.getLevel();
        if (level == null || level.isClientSide()) {
            return 0L;
        }
        if (!mainNode.isActive() || !hasPattern(details)) {
            return 0L;
        }
        if (getCraftingLockedReason() != LockCraftingMode.NONE || super.isBusy()) {
            return 0L;
        }
        if (!patternClass.isInstance(details)) {
            return 0L;
        }
        return budget();
    }

    /**
     * {@code times} 回ぶんの材料を受け取り、<b>組み立ては 1 回だけ</b>行って結果を {@code times} 倍する。
     *
     * <p>
     * {@code inputHolder} の扱いに注意。{@link #fillGrid} がグリッドに載せるのは 1 回ぶんだけで、
     * 残りの {@code times - 1} 回ぶんは {@code inputHolder} に残ったままになる。
     * これを<b>消費済みとして捨てられるのは組み立てが成功したときだけ</b>で、
     * 失敗したときは呼び出し側 ({@code QuantumBulkCrafting}) が CPU の在庫へ戻せるよう
     * <b>そのまま残して返す</b>。
     * </p>
     */
    @Override
    public long pushPatternBulk(IPatternDetails details, KeyCounter[] inputHolder, long times) {
        if (!patternClass.isInstance(details) || times <= 0L) {
            return 0L;
        }
        Level level = host.getLevel();
        if (level == null || budget() < times) {
            return 0L;
        }
        KeyCounter[] originalInputs = copyInputHolder(inputHolder);

        ASSEMBLY assembly = resolveAssembly(patternClass.cast(details), inputHolder);
        if (assembly == null) {
            // 組立失敗時はpushPatternBulkの外側が材料を戻すため、ここでは
            // inputHolderを呼出し前の状態へ戻し、グリッドを空にする。
            // 一部だけをネットワークへ返すと、外側のRollbackと二重計上になる。
            restoreInputHolder(inputHolder, originalInputs);
            return 0L;
        }

        // エネルギーによって実行回数を制御
        times = Math.min(times, energyBudget(assembly));
        consumeInputs(assembly, inputHolder, times);

        remainingCrafts -= times;
        storeOutputs(assembly, times);
        double usedEnergy = host.getEnergyPerProcess(assembly) * times;
        host.addUsedEnergyThisTick(usedEnergy);
        remainingEnergy -= usedEnergy;
        ((PatternProviderLogicAccessor) this).insaneae$onPushPatternSuccess(details);
        return times;
    }

    /** 材料を 1 回ぶん消費する。 */
    protected void consumeInputs(ASSEMBLY assembly, KeyCounter[] inputHolder) {
        GenericStack[] inputs = assembly.inputs();
        for (int index = 0; index < inputs.length; index++) {
            long toConsume = inputs[index].amount();
            for (AEKey key : inputHolder[index].keySet()) {
                long has = inputHolder[index].get(key);
                if (has >= toConsume) {
                    inputHolder[index].remove(key, toConsume);
                    inputHolder[index].removeZeros();
                    break;
                } else {
                    inputHolder[index].remove(key);
                    toConsume -= has;
                }
            }
        }
    }

    /** 材料を {@code times} 回ぶん消費する。 */
    protected void consumeInputs(ASSEMBLY assembly, KeyCounter[] inputHolder, long times) {
        GenericStack[] inputs = assembly.inputs();
        for (int index = 0; index < inputs.length; index++) {
            long toConsume = inputs[index].amount() * times;
            for (AEKey key : inputHolder[index].keySet()) {
                long has = inputHolder[index].get(key);
                if (has >= toConsume) {
                    inputHolder[index].remove(key, toConsume);
                    inputHolder[index].removeZeros();
                    break;
                } else {
                    inputHolder[index].remove(key);
                    toConsume -= has;
                }
            }
        }
    }

    /** 完成品と端材を {@code times} 回ぶん貯める。 */
    protected void storeOutputs(ASSEMBLY assembly, long times) {
        BigInteger factor = BigInteger.valueOf(times);
        for (GenericStack output : assembly.outputs()) {
            host.addPendingOutput(output.what(), BigInteger.valueOf(output.amount()).multiply(factor));
        }
    }

    @Override
    public boolean fusesOperations() {
        return host.isTaskFusionInstalled();
    }

    @Override
    public boolean repeatsWindowsWithinTick() {
        return host.isWindowRepeatEnabled();
    }

    @Override
    public void settleCompletedOutputs() {
        host.flushPendingOutputs();
    }

    /**
     * 加工パターンの押し出し待ちが残っているか、この tick の組み立て回数を使い切っていれば忙しい。
     * CPU 側はここで true を見ると他のプロバイダに回るので、無駄な {@code pushPattern} が減る。
     */
    @Override
    public boolean isBusy() {
        return super.isBusy() || budget() <= 0;
    }

    /** この tick に残っている組み立て回数。tick が変わっていれば補充する。 */
    protected long budget() {
        Level level = host.getLevel();
        if (level == null) {
            return 0L;
        }
        long now = level.getGameTime();
        if (now != budgetTick) {
            budgetTick = now;
            remainingCrafts = host.getCraftsPerTick();
        }
        return remainingCrafts;
    }

    /** この tick に残っているエネルギー依存の組み立て回数。tick が変わっていれば{@link #remainingEnergy}を補充する。 */
    protected long energyBudget(ASSEMBLY assembly) {
        Level level = host.getLevel();
        if (level == null) {
            return 0L;
        }
        long now = level.getGameTime();
        if (now != energyTick) {
            energyTick = now;
            remainingEnergy = host.getEnergyCanUseThisTick();
        }
        double energyPerProcess = host.getEnergyPerProcess(assembly);
        if (energyPerProcess <= 0.0d) {
            return Long.MAX_VALUE;
        } else {
            double v = remainingEnergy / energyPerProcess;
            return v > Long.MAX_VALUE ? Long.MAX_VALUE : (long) v;
        }
    }

    /** ACO がターゲットとして掴む BlockEntity ({@code ProviderOwnedPatternBatchTarget})。 */
    public MACHINE getHostBlockEntity() {
        return host;
    }

    /**
     * 材料に対する組み立て結果を返す。
     * 覚えていればそれを使い、無ければ実際にレシピを回す。組めなければ null。
     */
    protected ASSEMBLY resolveAssembly(PATTERN pattern, KeyCounter[] inputHolder) {
        ASSEMBLY cached = assemblyCache.get(pattern);
        if (cached != null && host.matchesCache(cached, inputHolder)) {
            return cached;
        }
        return host.assemble(pattern, inputHolder);
    }

    /** まとめ処理失敗時に、fillCraftingGrid前の入力所有量を複製する。 */
    protected static KeyCounter[] copyInputHolder(KeyCounter[] inputHolder) {
        KeyCounter[] copy = new KeyCounter[inputHolder.length];
        for (int index = 0; index < inputHolder.length; index++) {
            copy[index] = new KeyCounter();
            copy[index].addAll(inputHolder[index]);
        }
        return copy;
    }

    /** 呼出し前の入力へ戻し、BulkCraftingHook側のRollbackへ所有権を返す。 */
    protected static void restoreInputHolder(KeyCounter[] inputHolder, KeyCounter[] originalInputs) {
        for (int index = 0; index < inputHolder.length; index++) {
            inputHolder[index].reset();
            inputHolder[index].addAll(originalInputs[index]);
        }
    }

}