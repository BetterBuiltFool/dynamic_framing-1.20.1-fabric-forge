package com.github.betterbuiltfool.fabric.client;

import com.github.betterbuiltfool.client.BaseModelCache;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

public class FabricModelLoaderHook {
    public static void register() {
        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.modifyModelAfterBake()
                         .register((model, context) -> {
                             if (!BaseModelCache.CHECK_BLOCKS.contains(context.id())) return model;
                             return BaseModelCache.getOrCreate(model);
                         });
        });
    }
}
