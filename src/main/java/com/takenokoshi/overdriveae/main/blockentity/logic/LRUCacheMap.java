package com.takenokoshi.overdriveae.main.blockentity.logic;

import java.util.LinkedHashMap;

public class LRUCacheMap<KEY, VALUE> extends LinkedHashMap<KEY, VALUE> {

    private final int cacheSize;

    public LRUCacheMap(int initialCapacity, float loadFactor, boolean accessOrder, int cacheSize) {
        super(initialCapacity, loadFactor, accessOrder);
        this.cacheSize = cacheSize;
    }

    @Override
    protected boolean removeEldestEntry(java.util.Map.Entry<KEY, VALUE> eldest) {
        return size() > cacheSize;
    }
}
