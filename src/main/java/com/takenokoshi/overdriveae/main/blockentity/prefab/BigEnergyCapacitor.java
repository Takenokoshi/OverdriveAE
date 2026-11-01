package com.takenokoshi.overdriveae.main.blockentity.prefab;

import appeng.api.config.Actionable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

/**
 * AE based Energy Capacitor.
 */
public class BigEnergyCapacitor {

    public static final String NBT_STORED = "stored";

    private final double defaultCapacity;
    private double capacity, stored;

    public BigEnergyCapacitor(double defaultCapacity) {
        this.defaultCapacity = defaultCapacity > 0.0D ? defaultCapacity : 10000.0D;
        this.capacity = this.defaultCapacity;
    }

    public double getDefaultCapacity() {
        return defaultCapacity;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getStored() {
        return stored;
    }

    public double getNeeded() {
        return capacity - stored;
    }

    public double insert(double toInsert, Actionable actionable) {
        double needed = getNeeded();
        if (toInsert > needed) {
            toInsert = needed;
            if (!actionable.isSimulate()) {
                stored = capacity;
            }
        } else {
            if (!actionable.isSimulate()) {
                stored += toInsert;
            }
        }
        return toInsert;
    }

    public double extract(double toExtract, Actionable actionable) {
        if (toExtract > stored) {
            toExtract = stored;
            if (!actionable.isSimulate()) {
                stored = 0.0D;
            }
        } else {
            if (!actionable.isSimulate()) {
                stored -= toExtract;
            }
        }
        return toExtract;
    }

    public void setCapacity(double multiplier) {
        if (multiplier > 0.0D) {
            capacity = defaultCapacity * multiplier;
            if (capacity < stored) {
                stored = capacity;
            }
        }
    }

    public void readFromNBT(CompoundTag data, String name, HolderLookup.Provider registries) {
        CompoundTag tag = data.getCompound(name);
        stored = tag.getDouble(NBT_STORED);
    }

    public void writeToNBT(CompoundTag data, String name, HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(NBT_STORED, stored);
        data.put(name, tag);
    }

}
