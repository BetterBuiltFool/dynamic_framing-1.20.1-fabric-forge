package com.github.betterbuiltfool.ui.overlays;

import com.github.betterbuiltfool.ui.NodeMarkerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class FramingHammerOverlay {
    public static void renderOverlay(Minecraft client, PoseStack poseStack, LongSet positions) {
        
        Camera camera = client.getEntityRenderDispatcher().camera;
        
        for(long node: positions) {
            Vec3 renderPos = BlockPos.of(node).getCenter().subtract(camera.getPosition());
            NodeMarkerRenderer.renderNode(poseStack, renderPos, camera);
        }
        
    }
}
