package com.takenokoshi.overdriveae.recipe;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class RecipeCacheReloadLister implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager arg0) {
        RecipeCacheHolder.clearAllCaches();
    }

}
