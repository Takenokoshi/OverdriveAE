package com.takenokoshi.overdriveae.main.blockentity.prefab;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.takenokoshi.overdriveae.main.blockentity.logic.OverdriveMachineLogic;
import com.takenokoshi.overdriveae.main.registries.OverdriveAEItems;

import appeng.api.crafting.IPatternDetails;
import appeng.api.inventories.ISegmentedInventory;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNodeListener.State;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.util.AECableType;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class OverdriveMachineBlockEntity<PATTERN extends IPatternDetails, PATTERN_CACHE>
        extends AENetworkedBlockEntity
        implements ServerTickingBlockEntity,
        IOverdriveMachine<PATTERN, PATTERN_CACHE> {

    public static final String NBT_LOGIC = "logic", NBT_UPGRADES = "upgrades", NBT_PRIORITY = "priority";
    protected static final int PENDING_SAVE_INTERVAL = 20;

    protected final OverdriveMachineLogic<PATTERN, PATTERN_CACHE> logic;
    protected final IUpgradeInventory upgradeInventory;

    protected boolean pendingWasSaved;
    protected int ticksSincePendingSave;

    protected long budget, remainingBudgetThisTick;
    protected boolean coprocessorUsageReductionEnabled;

    protected int priority;

    public OverdriveMachineBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState,
            ItemLike upgradeHost) {
        super(blockEntityType, pos, blockState);
        this.logic = createLogic();
        this.upgradeInventory = createUpgradeInv(upgradeHost);
    }

    protected abstract OverdriveMachineLogic<PATTERN, PATTERN_CACHE> createLogic();

    protected IUpgradeInventory createUpgradeInv(ItemLike upgradeHost) {
        return UpgradeInventories.forMachine(upgradeHost, 15, this::onUpgradeChanged);
    };

    @Override
    public long budget(@NotNull PATTERN pattern) {
        return remainingBudgetThisTick;
    }

    @Override
    public boolean isBusy() {
        return remainingBudgetThisTick <= 0L;
    }

    @Override
    public void serverTick() {
        logic.updatePatterns();
        if (logic.sendOutputs()) {
            saveOutputsIfNeeded();
        }
        remainingBudgetThisTick = budget;
    }

    protected void saveOutputsIfNeeded() {
        boolean shouldSaveChanges = false;
        if (!logic.isOutputEmpty()) {
            if (!pendingWasSaved || ++ticksSincePendingSave >= PENDING_SAVE_INTERVAL) {
                ticksSincePendingSave = 0;
                pendingWasSaved = true;
                shouldSaveChanges = true;
            }
        } else if (pendingWasSaved) {
            ticksSincePendingSave = 0;
            pendingWasSaved = false;
            shouldSaveChanges = true;
        }
        if (shouldSaveChanges) {
            saveChanges();
        }
    }

    @Override
    public void onProcessAdditional(@NotNull PATTERN pattern, @NotNull PATTERN_CACHE cache, long times) {
        remainingBudgetThisTick -= times;
    }

    // ------------------------------------------------------------ アップグレード

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgradeInventory;
    }

    @Override
    public InternalInventory getSubInventory(ResourceLocation id) {
        if (id.equals(ISegmentedInventory.UPGRADES)) {
            return upgradeInventory;
        }
        return super.getSubInventory(id);
    }

    protected void onUpgradeChanged() {
        this.coprocessorUsageReductionEnabled = upgradeInventory
                .isInstalled(OverdriveAEItems.COPROCESSOR_USAGE_REDUCTION_CARD);
        int speedCards = 0;
        for (int i = 0; i < 7; i++) {
            if (upgradeInventory.isInstalled(OverdriveAEItems.OVERDRIVE_SPEED_CARDS.get(i))) {
                speedCards++;
            }
        }
        budget = speedCards >= 7 ? Long.MAX_VALUE : (1L << speedCards * 8 + 7) - 1L;
        saveChanges();
    }

    @Override
    public boolean coprocessorUsageReductionEnabled() {
        return coprocessorUsageReductionEnabled;
    }

    // ------------------------------------------------------------ BlockEntity

    @Override
    public void onMainNodeStateChanged(State reason) {
    }

    @Override
    public void onReady() {
        super.onReady();
        logic.updateAvailablePatterns();
        onUpgradeChanged();
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.SMART;
    }

    @Override
    public void saveAdditional(CompoundTag data, Provider registries) {
        super.saveAdditional(data, registries);
        logic.writeToNBT(data, NBT_LOGIC, registries);
        upgradeInventory.writeToNBT(data, NBT_UPGRADES, registries);
        data.putInt(NBT_PRIORITY, priority);
    }

    @Override
    public void loadTag(CompoundTag data, Provider registries) {
        super.loadTag(data, registries);
        logic.readFromTag(data, NBT_LOGIC, registries);
        upgradeInventory.readFromNBT(data, NBT_UPGRADES, registries);
        priority = data.getInt(NBT_PRIORITY);

        pendingWasSaved = !logic.isOutputEmpty();
        ticksSincePendingSave = 0;
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        logic.addDrops(level, pos, drops);
        upgradeInventory.forEach(drops::add);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        logic.clearContent();
        upgradeInventory.clear();
    }

    // ------------------------------------- Gui表示用

    public OverdriveMachineLogic<PATTERN, PATTERN_CACHE> getLogic() {
        return logic;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;

    }

}
