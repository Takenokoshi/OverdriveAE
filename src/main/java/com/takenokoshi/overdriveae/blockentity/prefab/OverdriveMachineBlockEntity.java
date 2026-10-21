/*
 *Some part of this file is copy of https://github.com/taikun24/InsaneAE/blob/1.21.1/src/main/java/jp/main/taikun/insaneae/quantum/QuantumCpuBlockEntity.java
 */
package com.takenokoshi.overdriveae.blockentity.prefab;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener.State;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import jp.main.taikun.insaneae.integration.aco.OptionalAcoBigIntegerIntegration;
import jp.main.taikun.insaneae.integration.aco.PendingOutputLedger;
import jp.main.taikun.insaneae.integration.aco.PendingOutputNbt;
import jp.main.taikun.insaneae.quantum.batch.QuantumBatchReceipts;
import jp.main.taikun.insaneae.upgrade.SpeedBoost;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.takenokoshi.overdriveae.blockentity.logic.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.menu.OverdriveMachineMenu;
import com.takenokoshi.overdriveae.registration.OverdriveAEDeferredMachine;

import static jp.main.taikun.insaneae.quantum.QuantumCpuBlockEntity.BASE_CRAFTS_PER_TICK;
import static jp.main.taikun.insaneae.quantum.QuantumCpuBlockEntity.MULTIPLIER_PER_CARD;
import static jp.main.taikun.insaneae.quantum.QuantumCpuBlockEntity.MAX_ACCELERATION_CARDS;
import static jp.main.taikun.insaneae.quantum.QuantumCpuBlockEntity.UPGRADE_SLOTS;

import static jp.main.taikun.insaneae.registries.ModUpgrades.QUANTUM_ACCELERATION_CARD;
import static jp.main.taikun.insaneae.registries.ModUpgrades.TASK_FUSION_CARD;

import java.math.BigInteger;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

/**
 * OverdriveMachineBlockEntity
 */

public abstract class OverdriveMachineBlockEntity<ASSEMBLY extends OverdriveMachineLogic.Assembly, PATTERN extends IPatternDetails, MACHINE extends OverdriveMachineBlockEntity<ASSEMBLY, PATTERN, MACHINE>>
        extends AENetworkedBlockEntity
        implements PatternProviderLogicHost, IUpgradeableObject, ServerTickingBlockEntity,
        InternalInventoryHost {

    protected static final String NBT_UPGRADES = "upgrades";
    protected static final String NBT_PENDING = "pendingOutputs";
    protected static final String NBT_PENDING_BIG = "pendingOutputsBig";
    protected static final String NBT_USED_ENERGY = "usedEnergy";

    /** 完成品が詰まっている間、何 tick おきに保存するか。 */
    protected static final int PENDING_SAVE_INTERVAL = 20;

    protected final OverdriveMachineLogic<ASSEMBLY, PATTERN, MACHINE> logic;

    protected final IUpgradeInventory upgrades;

    /**
     * 組み上がったが、まだネットワークに入れていない完成品。
     * ME への挿入は 1 tick に 1 回だけまとめて行うので、その間ここに溜まる。
     */
    protected final PendingOutputLedger pendingOutputs;

    /**
     * ACO の craftingtable batch レシート台帳。
     *
     * <p>
     * ACO 未導入でも<b>ただの入れ物として存在する</b> (ACO の型に触れないクラスなので、
     * ここに持っても未導入環境のクラスロードに影響しない)。中身を触るのは
     * ACO 連携の Mixin だけ。
     * </p>
     */
    protected final QuantumBatchReceipts batchReceipts = new QuantumBatchReceipts();

    protected long processPerTick;

    /** このtickに消費した電力量 */
    protected double usedEnergyThisTick;
    /** 直近の保存時点で {@link #usedEnergyThisTick} が0でなかったか。 */
    protected boolean energyWasSaved;
    /** エネルギー借金状態を最後に保存してからのtick数。 */
    protected int ticksSinceEnergySave;

    /** 直近の保存時点で {@link #pendingOutputs} が空でなかったか。 */
    protected boolean pendingWasSaved;
    /** 詰まっている状態を最後に保存してからの tick 数。 */
    protected int ticksSincePendingSave;

    protected final IActionSource actionSource;

    protected final OverdriveAEDeferredMachine<?, ?> deferredMachine;

    public OverdriveMachineBlockEntity(OverdriveAEDeferredMachine<?, ?> deferredMachine, BlockPos pos, BlockState blockState,
            int additionalUpgradeSlots) {
        super(deferredMachine.getBlockEntityType(), pos, blockState);
        getMainNode().setIdlePowerUsage(6.0);
        this.deferredMachine = deferredMachine;
        this.logic = createLogic();
        this.upgrades = UpgradeInventories.forMachine(deferredMachine, UPGRADE_SLOTS + additionalUpgradeSlots,
                this::onUpgradeChange);
        this.pendingOutputs = OptionalAcoBigIntegerIntegration.createOutputLedger();
        this.processPerTick = BASE_CRAFTS_PER_TICK;

        this.actionSource = IActionSource.ofMachine(this);
    }

    /**
     * サブクラス側でロジックのクラスを変更できるようにしておく。
     */
    protected abstract OverdriveMachineLogic<ASSEMBLY, PATTERN, MACHINE> createLogic();

    // ---------------------------------------------------- パターンの取り扱い
    /** パターンは必ずしもAE2のパターンアイテムである必要は無い。 */

    /**
     * 処理パターンをキャッシュ化する。
     * 実在するレシピのいずれかと適合するか確認し、適合するレシピに合わせたパターンを返す。
     * 同じパターンでも機械によって異なる読み方がありえる。
     * OverdriveMachineLogic側から呼ばれる。
     */
    @Nullable
    public abstract PATTERN tryDecodePattern(ItemStack itemStack);

    /**
     * 処理パターンを受け入れられるかどうか確認する。
     * 実在するレシピのいずれかと適合するか確認すること。
     * OverdriveMachineLogic側から呼ばれる。
     */
    public abstract boolean isPatternAcceptable(ItemStack itemStack);

    /**
     * 一回分の処理を実行し、OverdriveMachineLogic側でキャッシュする。
     * 
     */
    public abstract ASSEMBLY assemble(PATTERN pattern, KeyCounter[] inputHolder);

    /**
     * OverdriveMachineLogic側でキャッシュされた情報と材料が一致するか判定する。
     */
    public abstract boolean matchesCache(ASSEMBLY assembly, KeyCounter[] inputHolder);

    // ------------------------------------------------------- エネルギー消費

    /**
     * @return 現在ME倉庫が持つエネルギー量
     */
    public double getEnergyCanUseThisTick() {
        IGrid grid = getMainNode().getGrid();
        return grid != null ? grid.getEnergyService().getStoredPower() - usedEnergyThisTick : 0.0D;
    }

    /**
     * このtickの消費エネルギーを蓄積する。
     * 
     * @param amount
     */
    public void addUsedEnergyThisTick(double amount) {
        this.usedEnergyThisTick += amount;
    }

    /**
     * 蓄積された消費エネルギーの分、ME倉庫のエネルギーを消費する。
     * 正常に消費できれば {@link #usedEnergyThisTick} が0になる。
     * 
     * @return エネルギーを消費したか
     */
    boolean consumeEnergy() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) {
            return false;
        }
        boolean v = usedEnergyThisTick > 0.0D;
        usedEnergyThisTick -= grid.getEnergyService().extractAEPower(usedEnergyThisTick,
                Actionable.MODULATE, PowerMultiplier.ONE);
        return v;
    }

    /**
     * @return 処理1回あたりの消費エネルギー
     */
    public abstract double getEnergyPerProcess(PATTERN pattern);

    // ---------------------------------------------------------------- 速度

    /**
     * 加工速度が変化するのはアップグレードに変化があったときのみなのでキャッシュしておく
     */
    protected void onUpgradeChange() {
        int cards = upgrades.getInstalledUpgrades(QUANTUM_ACCELERATION_CARD.get());
        long speed = BASE_CRAFTS_PER_TICK;
        for (int i = 0; i < cards; i++) {
            speed = SpeedBoost.saturatingMultiply(speed, MULTIPLIER_PER_CARD);
        }
        this.processPerTick = speed;
        this.saveChanges();
    }

    public long getCraftsPerTick() {
        return processPerTick;
    }

    public boolean isTaskFusionInstalled() {
        return upgrades.getInstalledUpgrades(TASK_FUSION_CARD.get()) > 0;
    }

    public boolean isWindowRepeatEnabled() {
        return upgrades.getInstalledUpgrades(QUANTUM_ACCELERATION_CARD.get()) >= MAX_ACCELERATION_CARDS;
    }
    // ------------------------------------------------------- 完成品の受け渡し

    /**
     * 完成品・端材を溜める。実際の ME への挿入は {@link #serverTick()} でまとめて行う。
     *
     * <p>
     * ここで {@code alertDevice} は呼ばない。このブロックはブロック側の ticker で
     * <b>毎 tick 必ず動く</b>ので起こしてもらう必要が無く、
     * 呼ぶとクラフト 1 回ごとにグリッドのティックキューを並べ替えることになる
     * ({@code TickManagerService#alertDevice} は {@code updateQueuePosition} まで走る)。
     * </p>
     */
    void addPendingOutput(@Nullable AEKey what, long amount) {
        if (what == null || amount <= 0L) {
            return;
        }
        addPendingOutput(what, BigInteger.valueOf(amount));
    }

    /** 掛け算結果をlongへ戻さず、完成品の正確な量を台帳へ加える。 */
    public void addPendingOutput(@Nullable AEKey what, BigInteger amount) {
        if (what == null || amount == null || amount.signum() <= 0) {
            return;
        }
        pendingOutputs.add(what, amount);
    }

    /** ACO 連携用のレシート台帳。 */
    public QuantumBatchReceipts getBatchReceipts() {
        return batchReceipts;
    }

    /** 完成品待ちの現在の中身 (コピー)。ゲームテスト用。 */
    public Map<AEKey, BigInteger> getPendingOutputs() {
        return pendingOutputs.snapshot();
    }

    @Override
    public void serverTick() {

        // 溜めておいたパターン更新をここで流す (遅れは最大 1 tick)。
        logic.flushPatternUpdate();

        if (flushPendingOutputs()) {
            savePendingAndEnergyIfNeeded();
        }
        consumeEnergy();
    }

    /**
     * 溜まっている完成品をネットワークへ流す。
     *
     * <p>
     * 毎 tick の {@link #serverTick()} からだけでなく、加速カード満載時は
     * <b>まとめ処理の窓と窓の間</b>からも呼ばれる ({@code QuantumBulkCrafting})。
     * ネットワークへ入れた瞬間にクラフト CPU が完成待ちから差し引くので、
     * これを挟むと 1 tick の合計が long を超えても帳簿が溢れない。
     * </p>
     *
     * @return 流すものがあったか (無ければ保存も要らない)
     */
    public boolean flushPendingOutputs() {
        if (pendingOutputs.isEmpty()) {
            return false;
        }
        IGrid grid = getMainNode().getGrid();
        if (grid == null || !getMainNode().isActive()) {
            return false;
        }

        MEStorage storage = grid.getStorageService().getInventory();
        for (var entry : pendingOutputs.snapshot().entrySet()) {
            // AE2 の insert は long API なので、BigInteger をこの一回分だけ安全に窓化する。
            long amount = pendingOutputs.drain(entry.getKey(), Long.MAX_VALUE);
            if (amount <= 0L) {
                continue;
            }
            long inserted = storage.insert(entry.getKey(), amount, Actionable.MODULATE, actionSource);
            if (inserted < amount) {
                // 部分搬入分だけを正確に戻し、搬入済み分を二重計上しない。
                pendingOutputs.add(entry.getKey(), BigInteger.valueOf(amount - inserted));
            }
        }
        return true;
    }

    /**
     * 溜まっている完成品をディスクに残す必要があるときだけ {@code saveChanges()} する。
     *
     * <p>
     * {@code saveChanges()} はチャンクに dirty を立てるので、
     * 毎 tick 呼ぶと<b>オートセーブのたびに 1620 枠ぶんのパターン NBT を書き出す</b>ことになる。
     * 普段は溜めた完成品をその tick のうちに全部 ME に入れられる = 保存すべき内容が
     * 「空のまま」で変わらないので、保存自体が要らない。
     * </p>
     *
     * <p>
     * 入りきらずに残っている間だけ保存し (詰まっている間は {@value #PENDING_SAVE_INTERVAL} tick に 1 回)、
     * 捌けきったら「空になった」ことを 1 回だけ保存する。
     * </p>
     */
    private void savePendingAndEnergyIfNeeded() {
        boolean shouldSaveChanges = false;
        if (!pendingOutputs.isEmpty()) {
            if (!pendingWasSaved || ++ticksSincePendingSave >= PENDING_SAVE_INTERVAL) {
                pendingWasSaved = true;
                ticksSincePendingSave = 0;
                shouldSaveChanges = true;
            }
        } else if (pendingWasSaved) {
            pendingWasSaved = false;
            ticksSincePendingSave = 0;
            shouldSaveChanges = true;
        }

        if (usedEnergyThisTick > 0.0D) {
            if (!energyWasSaved || ++ticksSinceEnergySave >= PENDING_SAVE_INTERVAL) {
                energyWasSaved = true;
                ticksSinceEnergySave = 0;
                shouldSaveChanges = true;
            }
        } else if (energyWasSaved) {
            energyWasSaved = false;
            ticksSinceEnergySave = 0;
            shouldSaveChanges = true;
        }

        if (shouldSaveChanges) {
            saveChanges();
        }
    }

    // ------------------------------------------------- PatternProviderLogicHost

    @Override
    public OverdriveMachineLogic<ASSEMBLY, PATTERN, MACHINE> getLogic() {
        return logic;
    }

    @Override
    public EnumSet<Direction> getTargets() {
        // 加工パターンのフォールバック用。全方向に押し出せる。
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(deferredMachine);
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(deferredMachine, 1);
    }

    /**
     * パターンアクセス端末での表示。隣接機械ではなく自分で組むので、
     * 「隣は何の機械か」ではなく自分自身と速度を出す。
     */
    @Override
    public PatternContainerGroup getTerminalGroup() {
        AEItemKey icon = getTerminalIcon();
        Component name = hasCustomName() ? getCustomName() : icon.getDisplayName();
        int cards = upgrades.getInstalledUpgrades(QUANTUM_ACCELERATION_CARD.get());
        List<Component> tooltip = cards == 0
                ? List.of()
                : List.of(GuiText.CompatibleUpgrade.text(
                        Tooltips.of(QUANTUM_ACCELERATION_CARD.get().getDescription()),
                        Tooltips.ofUnformattedNumber(cards)));
        return new PatternContainerGroup(icon, name, tooltip);
    }

    @Override
    public void openMenu(Player player, MenuHostLocator locator) {
        MenuOpener.open(OverdriveMachineMenu.TYPE, player, locator);
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(OverdriveMachineMenu.TYPE, player, subMenu.getLocator());
    }

    // ------------------------------------------------------------ アップグレード

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.UPGRADES)) {
            return upgrades;
        }
        return super.getSubInventory(id);
    }

    // ------------------------------------------------------------ BlockEntity

    @Override
    public void onMainNodeStateChanged(State reason) {
    }

    @Override
    public void onReady() {
        super.onReady();
        this.onUpgradeChange();
        logic.updateAllPatterns();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        logic.writeToNBT(data, registries);
        upgrades.writeToNBT(data, NBT_UPGRADES, registries);

        // BigInteger は byte[] として保存する。旧 ListTag は loadTag 側で移行する。
        // 形式は台帳の実装 (ACO / 内蔵) に任せず、常に InsaneAE 側で固定する。
        data.put(NBT_PENDING_BIG, PendingOutputNbt.save(pendingOutputs, registries));
        // ACO の craftingtable batch レシート。ACO 未導入でも読み書きするだけなので、
        // 一度連携で使ったワールドから ACO を抜いてもレシートは消えない。
        batchReceipts.save(data, registries);

        data.putDouble(NBT_USED_ENERGY, usedEnergyThisTick);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        logic.readFromNBT(data, registries);
        upgrades.readFromNBT(data, NBT_UPGRADES, registries);

        batchReceipts.load(data, registries);

        pendingOutputs.clear();
        PendingOutputNbt.load(pendingOutputs, data.getCompound(NBT_PENDING_BIG), registries);

        // 読み込んだ時点の中身は「保存済み」。空になったときに 1 回だけ保存すればよい。
        pendingWasSaved = !pendingOutputs.isEmpty();
        ticksSincePendingSave = 0;

        usedEnergyThisTick = data.getDouble(NBT_USED_ENERGY);
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        logic.addDrops(drops);
        for (ItemStack upgrade : upgrades) {
            drops.add(upgrade);
        }
        for (var entry : pendingOutputs.snapshot().entrySet()) {
            BigInteger amount = entry.getValue();
            if (amount.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) <= 0) {
                entry.getKey().addDrops(amount.longValueExact(), drops, level, pos);
            } else {
                // AE2 のドロップ API 自体が long なので、これ以上は物理ドロップに変換できない
                // (どのみち addDrops は 1000 スタックで打ち切る)。
                entry.getKey().addDrops(Long.MAX_VALUE, drops, level, pos);
            }
        }
    }

    @Override
    public void clearContent() {
        super.clearContent();
        logic.clearContent();
        upgrades.clear();
        pendingOutputs.clear();
    }

    /** {@link InternalInventoryHost}: 中身が変わったので保存する。 */
    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        saveChanges();
    }

    public Component getGuiTitle() {
        return Component.translatable(
                "block." + deferredMachine.getId().getNamespace() + "." + deferredMachine.getId().getPath());
    }

}
