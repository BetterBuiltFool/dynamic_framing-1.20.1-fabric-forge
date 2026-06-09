package com.github.betterbuiltfool.ui;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.items.FroeTool;
import com.github.betterbuiltfool.items.RendersOverlay;
import com.github.betterbuiltfool.structure.JointNode;
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
        
        if (item.getItem() instanceof RendersOverlay toolItem) {
            
            Camera camera = client.getEntityRenderDispatcher().camera;
            
            for(JointNode node: toolItem.getNodes(item)) {
                Vec3 renderPos = node.getPosition().getCenter().subtract(camera.getPosition());
                renderNode(poseStack, renderPos, camera);
            }
        }
        
    }
    
    private static void renderNode(
            PoseStack poseStack,
            Vec3 renderPos,
            Camera camera
    ) {
        poseStack.pushPose();
        
        poseStack.translate((float) renderPos.x(), (float) renderPos.y(), (float) renderPos.z());
        poseStack.mulPose(camera.rotation());
        
        Matrix4f pose = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        
        float width = 0.25f;
        float height = 0.25f;
        
        buffer.vertex(pose, - width / 2, + height / 2, 0).uv(0.0f, 0.0f).endVertex();
        buffer.vertex(pose, + width / 2, + height / 2, 0).uv(1.0f, 0.0f).endVertex();
        buffer.vertex(pose, + width / 2, - height / 2, 0).uv(1.0f, 1.0f).endVertex();
        buffer.vertex(pose, - width / 2, - height / 2, 0).uv(0.0f, 1.0f).endVertex();
        
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, NODE_MARKER);
        RenderSystem.disableDepthTest();
        
        tesselator.end();
        
        RenderSystem.enableDepthTest();
        
        poseStack.popPose();
    }
}
