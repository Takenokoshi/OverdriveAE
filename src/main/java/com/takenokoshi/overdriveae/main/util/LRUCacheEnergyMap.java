package com.takenokoshi.overdriveae.main.util;

import java.util.function.ToDoubleFunction;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2DoubleLinkedOpenHashMap;

/**
 * 一回あたりの消費エネルギーを保存しておくLRU
 * 
 * {@code put}と{@code computeIfAbsentElseMoveToLast}以外のメソッドはこの目的では不要なので
 * LRUとしての容量制限はこの2つのメソッドにしかついていない
 */
public class LRUCacheEnergyMap<PATTERN> extends Object2DoubleLinkedOpenHashMap<PATTERN> {

    private final int cacheSize;

    public LRUCacheEnergyMap(int initialCapacity, float loadFactor, int cacheSize) {
        super(initialCapacity, loadFactor);
        this.cacheSize = cacheSize;
    }

    @Override
    public double put(PATTERN k, double v) {
        double value = super.putAndMoveToLast(k, v);
        if (size() > cacheSize) {
            removeFirstDouble();
        }
        return value;
    }

    public double computeIfAbsentElseMoveToLast(PATTERN key, @NotNull ToDoubleFunction<PATTERN> mappingFunction) {
        if (containsKey(key)) {
            return getAndMoveToLast(key);
        } else {
            double v = mappingFunction.applyAsDouble(key);
            put(key, v);
            return v;
        }
    }

}
