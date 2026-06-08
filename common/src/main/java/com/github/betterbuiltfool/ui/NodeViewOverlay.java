package com.github.betterbuiltfool.ui;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.items.FroeTool;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.*;

public class NodeViewOverlay {
    
    private static final ResourceLocation NODE_MARKER = new ResourceLocation(
            DynamicFraming.MOD_ID, "textures/ui/node_marker.png"
    );
    
    public static void renderOverlay(PoseStack poseStack, LevelRenderer levelRenderer) {
        Minecraft client = Minecraft.getInstance();
        
        if (client.player == null || client.level == null) {
            return;
        }
        var item = client.player.getMainHandItem();
        
        if (item.getItem() instanceof FroeTool) {
            // TODO: add a base class or interface for approving tools
            DynamicFraming.LOGGER.info("Tool Equipped");
            
            
            var currentNodePos = FroeTool.getFirstPos(item);
            if (currentNodePos != null) {
                
                Camera camera = client.getEntityRenderDispatcher().camera;
                
                Vec3 renderPos = currentNodePos.getCenter().subtract(camera.getPosition());
                poseStack.pushPose();
                Matrix4f pose = poseStack.last().pose();
                Tesselator tesselator = Tesselator.getInstance();
                BufferBuilder buffer = tesselator.getBuilder();
                
                poseStack.translate((float) renderPos.x(), (float) renderPos.y(), (float) renderPos.z());
                poseStack.mulPose(camera.rotation());
                
                buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
                
                int width = 8;
                int height = 8;
                
                buffer.vertex(pose, - (float) width / 2, + (float) height / 2, 0).endVertex();
                buffer.vertex(pose, + (float) width / 2, + (float) height / 2, 0).endVertex();
                buffer.vertex(pose, + (float) width / 2, - (float) height / 2, 0).endVertex();
                buffer.vertex(pose, - (float) width / 2, - (float) height / 2, 0).endVertex();
                
                RenderSystem.setShader(GameRenderer::getPositionShader);
                
                tesselator.end();
                
                poseStack.popPose();
            }
        }
        
    }
}
