package com.github.betterbuiltfool.ui.overlays;

import com.github.betterbuiltfool.ui.NodeMarkerRenderer;
import com.github.betterbuiltfool.ui.NodeOverlayContext;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class FramingHammerOverlay {
    public static void renderOverlay(NodeOverlayContext context) {
        var client = context.client;
        var poseStack = context.poseStack;
        var positions = context.highlightNodes;
        
        Camera camera = client.getEntityRenderDispatcher().camera;
        
        for(long node: positions) {
            Vec3 renderPos = BlockPos.of(node).getCenter().subtract(camera.getPosition());
            NodeMarkerRenderer.renderNode(poseStack, renderPos, camera);
        }
        
    }
}
