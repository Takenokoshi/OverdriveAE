package com.takenokoshi.overdriveae.main.blockentity.prefab;

import java.math.BigInteger;
import java.util.LinkedHashMap;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class BigGenericHolder extends LinkedHashMap<AEKey, BigInteger> {

    public static final String NBT_KEY = "key";
    public static final String NBT_VALUE = "value";

    public void add(GenericStack stack) {
        if (stack == null) {
            return;
        }
        merge(stack.what(), BigInteger.valueOf(stack.amount()), BigInteger::add);
    }

    public void add(AEKey what, long amount) {
        if (what == null || amount <= 0L) {
            return;
        }
        merge(what, BigInteger.valueOf(amount), BigInteger::add);
    }

    public void add(AEKey what, BigInteger value) {
        if (value == null || what == null || value.signum() <= 0) {
            return;
        }
        merge(what, value, BigInteger::add);
    }

    public long drain(AEKey what, long maximum) {
        if (what == null || maximum < 1L) {
            return 0L;
        }
        if (containsKey(what)) {
            BigInteger current = get(what);
            if (current.signum() <= 0) {
                return 0L;
            }
            BigInteger drained = current.min(BigInteger.valueOf(maximum));
            BigInteger remaining = current.subtract(drained);
            if (remaining.signum() == 0) {
                remove(what);
            } else {
                put(what, remaining);
            }
            return drained.longValueExact();
        } else {
            return 0L;
        }
    }

    public BigInteger drain(AEKey what, BigInteger maximum) {
        if (what == null || maximum == null || maximum.signum() <= 0) {
            return BigInteger.ZERO;
        }
        if (containsKey(what)) {
            BigInteger current = get(what);
            if (current.signum() <= 0) {
                return BigInteger.ZERO;
            }
            if (current.compareTo(maximum) <= 0) {
                remove(what);
                return current;
            } else {
                put(what, current.subtract(maximum));
                return maximum;
            }
        } else {
            return BigInteger.ZERO;
        }
    }

    public void readFromNBT(CompoundTag data, String name, HolderLookup.Provider registries) {
        ListTag listTag = data.getList(name, Tag.TAG_COMPOUND);
        if (listTag.isEmpty()) {
            return;
        }
        clear();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompound(i);
            byte[] byteArray = tag.getByteArray(NBT_VALUE);
            if (byteArray.length > 0) {
                add(AEKey.fromTagGeneric(registries, tag.getCompound(NBT_KEY)), new BigInteger(byteArray));
            }
        }
    }

    public void writeToNBT(CompoundTag data, String name, HolderLookup.Provider registries) {
        ListTag listTag = new ListTag();
        forEach((key, value) -> {
            CompoundTag tag = new CompoundTag();
            tag.put(NBT_KEY, key.toTagGeneric(registries));
            tag.putByteArray(NBT_VALUE, value.toByteArray());
            listTag.add(tag);
        });
        data.put(name, listTag);
    }
}
