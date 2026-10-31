package appeng.crafting.execution;

import java.util.Iterator;
import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;

import com.takenokoshi.overdriveae.main.OverdriveAE;
import com.takenokoshi.overdriveae.main.blockentity.logic.ILargeCraftingProvider;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.execution.ExecutingCraftingJob.TaskProgress;
import appeng.crafting.inv.ICraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.service.CraftingService;
import net.minecraft.world.level.Level;

/**
 * 扱うクラスやフィールドの一部がpackage-privateなので、同一パッケージ内に実行部分を配置するゴリ押しを敢行
 * TaskProgress等はクラス自体が外部から見えない為、Mixinで処理しようとするとかなり面倒である。
 */
public class OverdriveAE$LargeCraftingExecuter {

    /**
     * 
     * @return 実際に使用したCPU並列数、実際の処理実行回数ではない
     */
    public static int executeLargeCrafting(
            final int remainingOperations,
            final @NotNull CraftingCPUCluster cluster,
            final @NotNull CraftingService craftingService,
            final @NotNull IEnergyService energyService,
            final @NotNull ExecutingCraftingJob job,
            final @NotNull ICraftingInventory inventory) {
        // 実行するべきタスクが無いなら何もしなくていい
        if (job.tasks.isEmpty() || remainingOperations <= 0L) {
            return 0;
        }

        Level level = cluster.getLevel();
        if (level == null) {
            return 0;
        }

        OverdriveAE.LOGGER.info("###LargeCraftingExecuter fired###");

        // 消費したCPU並列数
        int usedOperations = 0;

        Iterator<Entry<IPatternDetails, TaskProgress>> iterator = job.tasks.entrySet().iterator();
        taskLoop: while (iterator.hasNext()) {
            if (usedOperations >= remainingOperations) {
                break;
            }
            Entry<IPatternDetails, TaskProgress> task = iterator.next();
            TaskProgress progress = task.getValue();
            if (progress.value <= 0L) {
                iterator.remove();
                continue;
            }
            IPatternDetails patternDetails = task.getKey();
            KeyCounter expectedOutputs = new KeyCounter();
            KeyCounter expectedContainerItems = new KeyCounter();

            Iterable<ICraftingProvider> providers = craftingService.getProviders(patternDetails);
            for (ICraftingProvider craftingProvider : providers) {
                if (!(craftingProvider instanceof ILargeCraftingProvider largeCraftingProvider)) {
                    /**
                     * ILargeCraftingProvider でないプロバイダは飛ばす。彼らへの発注はAE2本来の経路に任せる。
                     * この影響で設定された優先度を無視して ILargeCraftingProvider に発注が入ってしまうが、
                     * そもそもpatternDetailsのインスタンスが通常のパターンプロバイダとOverdriveAEの機械では異なる為、
                     * 実際に問題になることはないものとして無視する。
                     */
                    continue;
                }
                // この時点で1回分の材料がinventoryから取り出されている
                KeyCounter[] craftingContainer = CraftingCpuHelper.extractPatternInputs(patternDetails, inventory,
                        level, expectedOutputs, expectedContainerItems);
                if (craftingContainer == null) {
                    break;
                }
                boolean compressTaskEnabled = largeCraftingProvider.compressTaskEnabled();
                long times = compressTaskEnabled
                        ? largeCraftingProvider.budget(patternDetails)
                        : Math.min(largeCraftingProvider.budget(patternDetails), remainingOperations - usedOperations);
                if (times <= 0L) {
                    CraftingCpuHelper.reinjectPatternInputs(inventory, craftingContainer);
                    continue;
                }
                times = Math.min(times, progress.value);
                double patternPower = CraftingCpuHelper.calculatePatternPower(craftingContainer);
                double extractablePower = energyService.extractAEPower(patternPower * times, Actionable.SIMULATE,
                        PowerMultiplier.CONFIG);
                times = (long) Math.min(extractablePower / patternPower, times);
                if (times <= 0L) {
                    CraftingCpuHelper.reinjectPatternInputs(inventory, craftingContainer);
                    break;
                }
                for (var entry : expectedOutputs) {
                    times = Math.min(times, Long.MAX_VALUE / entry.getLongValue());
                }
                for (var entry : expectedContainerItems) {
                    times = Math.min(times, Long.MAX_VALUE / entry.getLongValue());
                }
                times = extractInputs(craftingContainer, inventory, times - 1L) + 1L;
                long pushed = largeCraftingProvider.pushPatternLarge(patternDetails, craftingContainer, times);
                if (times != pushed) {
                    CraftingCpuHelper.reinjectPatternInputs(inventory, craftingContainer);
                }
                if (pushed > 0L) {
                    energyService.extractAEPower(patternPower * pushed, Actionable.MODULATE, PowerMultiplier.CONFIG);
                    usedOperations += compressTaskEnabled ? 1 : (int) pushed;
                    multiplyCounter(expectedOutputs, pushed).forEach(
                            entry -> job.waitingFor.insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE));
                    multiplyCounter(expectedContainerItems, pushed).forEach(
                            entry -> job.waitingFor.insert(entry.getKey(), entry.getLongValue(), Actionable.MODULATE));
                    cluster.markDirty();
                    progress.value -= pushed;
                    if (progress.value <= 0L) {
                        iterator.remove();
                        continue taskLoop;
                    }
                    if (remainingOperations == usedOperations) {
                        break taskLoop;
                    }
                }
                expectedOutputs.reset();
                expectedContainerItems.reset();
            }
        }
        return usedOperations;
    }

    /**
     * inputHolderの中身を可能な回数分craftingInventoryから取り出してinputHolderに加え、
     * 取り出せた回数を返す
     * 
     * @param inputHolder
     * @param craftingInventory
     * @param maximum
     * @return
     */
    public static long extractInputs(KeyCounter[] inputHolder, ICraftingInventory craftingInventory, long maximum) {
        long times = maximum;
        long[] extractedTimes = new long[inputHolder.length];
        for (int index = 0; index < inputHolder.length; index++) {
            if (inputHolder[index].isEmpty() || inputHolder[index].size() > 1) {
                // サイズが1以上ということは、パターンが示した使える材料の中から複数の材料を組合せて必要量を満たしている
                // どれか一つの材料はもう残ってないので最大回数を0にしてbreak
                times = 0L;
                break;
            }
            // 以降、最初の要素のみについて判定する
            var entry = inputHolder[index].getFirstEntry();
            long needAmount = entry.getLongValue();
            AEKey key = entry.getKey();
            // longのオーバーフローを防ぐために回数をキャップする
            times = Math.min(times, Long.MAX_VALUE / needAmount);
            // 一度実際に取り出す
            long extracted = craftingInventory.extract(key, needAmount * times, Actionable.MODULATE);
            // 必要量の倍数からずれている場合、ズレの分は返す
            long extra = extracted % needAmount;
            if (extra != 0L) {
                craftingInventory.insert(key, extra, Actionable.MODULATE);
            }

            // 取り出せた回数を記録する
            extractedTimes[index] = extracted / needAmount;
            times = Math.min(extractedTimes[index], times);
        }
        for (int index = 0; index < inputHolder.length; index++) {
            if (inputHolder[index].isEmpty()) {
                continue;
            }
            var entry = inputHolder[index].getFirstEntry();
            AEKey key = entry.getKey();
            long needAmount = entry.getLongValue();
            // 材料によって取り出せた回数に差が生じるので最小値に揃え、余剰を返す
            if (extractedTimes[index] > times) {
                long toReturn = (extractedTimes[index] - times) * needAmount;
                craftingInventory.insert(key, toReturn, Actionable.MODULATE);
            }
            if (times != 0L) {
                inputHolder[index].add(key, needAmount * times);
            }

        }
        return times;
    }

    private static KeyCounter multiplyCounter(KeyCounter counter, long multiplier) {
        counter.forEach(entry -> entry.setValue(entry.getLongValue() * multiplier));
        return counter;
    }
}
