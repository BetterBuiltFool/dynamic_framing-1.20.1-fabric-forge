package com.github.betterbuiltfool.client;


import com.github.betterbuiltfool.registry.BlockRegistry;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BaseModelCache {
    private static final Map<BakedModel, ProceduralFrameModel> WRAPPERS = new HashMap<>();
    
    public static final Set<ResourceLocation> CHECK_BLOCKS = Set.of(
            BlockRegistry.JOINT_BLOCK.getId(),
            BlockRegistry.BEAM_BLOCK.getId(),
            BlockRegistry.POST_BLOCK.getId()
    );
    
    public static ProceduralFrameModel getOrCreate(BakedModel original) {
        return WRAPPERS.computeIfAbsent(original, ProceduralFrameModel::new);
    }
}
