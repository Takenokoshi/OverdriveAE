package com.takenokoshi.overdriveae.main.recipe;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class RecipeCacheReloadLister implements ResourceManagerReloadListener {

    @Override
    public void onResourceManagerReload(ResourceManager arg0) {
        RecipeCacheHolder.clearAllCaches();
    }

}
