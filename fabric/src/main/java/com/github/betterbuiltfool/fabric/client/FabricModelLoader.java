package com.github.betterbuiltfool.fabric.client;

import com.github.betterbuiltfool.DynamicFraming;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;

public class FabricModelLoader {
    public static ResourceLocation JOINT_ID = new ResourceLocation(DynamicFraming.MOD_ID, "block/joint_block");
    public static ResourceLocation BEAM_ID = new ResourceLocation(DynamicFraming.MOD_ID, "block/beam_block");
    
    public static void register() {
        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.resolveModel()
                         .register(resolverContext -> {
                             if (resolverContext.id()
                                                .equals(JOINT_ID)) {
                                 return new FabricProceduralJointModel();
                             } else if (resolverContext.id()
                                                       .equals(BEAM_ID)) {
                                 return new FabricProceduralBeamModel();
                             }
                             return null;
                         });
        });
    }
}
