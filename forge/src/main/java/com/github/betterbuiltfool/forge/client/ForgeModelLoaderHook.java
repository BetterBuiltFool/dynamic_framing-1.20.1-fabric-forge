package com.github.betterbuiltfool.forge.client;

import com.github.betterbuiltfool.client.BaseModelCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeModelLoaderHook {
    
    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();
        
        for (var entry : models.entrySet()) {
            var modelId = entry.getKey();
            
            var baseId = ResourceLocation.fromNamespaceAndPath(modelId.getNamespace(), modelId.getPath());
            
            if (!BaseModelCache.CHECK_BLOCKS.contains(baseId)) {
                return;
            }
            
            entry.setValue(BaseModelCache.getOrCreate(entry.getValue()));
        }
    }
}
