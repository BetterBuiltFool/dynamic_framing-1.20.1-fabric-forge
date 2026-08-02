package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentHashMap;

public class ModelCache {
    
    private static final ConcurrentHashMap<CacheKey, BakedModel> CACHE = new ConcurrentHashMap<>();
    
    public static BakedModel getOrCreateModel(BlockState material,
                                              Size size,
                                              Alignment x,
                                              Alignment y,
                                              Alignment z
    ) {
        var key = new CacheKey(material, size, x, y, z);
        
        return CACHE.computeIfAbsent(key, cacheKey -> {
                                         BakedModel materialModel = Minecraft.getInstance()
                                                                             .getBlockRenderer()
                                                                             .getBlockModel(material);
                                         
                                         return new ProceduralFrameModel(
                                                 materialModel
                                         );
                                     }
        );
    }
}
