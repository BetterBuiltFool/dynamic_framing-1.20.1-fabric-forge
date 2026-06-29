package com.github.betterbuiltfool.ui;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.items.RendersOverlay;
import com.github.betterbuiltfool.structure.JointNode;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

public class NodeViewOverlay {
    
    public static void renderOverlay(PoseStack poseStack) {
        Minecraft client = Minecraft.getInstance();
        
        if (client.player == null || client.level == null) {
            return;
        }
        var item = client.player.getMainHandItem();
        
        if (item.getItem() instanceof RendersOverlay toolItem) {
            
            Camera camera = client.getEntityRenderDispatcher().camera;
            
            for(JointNode node: toolItem.getNodes(item)) {
                Vec3 renderPos = node.getPosition().getCenter().subtract(camera.getPosition());
                NodeMarkerRenderer.renderNode(poseStack, renderPos, camera);
            }
        }
        
    }
}
