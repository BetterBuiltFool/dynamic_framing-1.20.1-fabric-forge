package com.github.betterbuiltfool.ui;

import com.github.betterbuiltfool.DynamicFraming;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class NodeMarkerRenderer extends UIRenderer<NodeMarkerRenderer> {
    
    private static final ResourceLocation NODE_MARKER = new ResourceLocation(
            DynamicFraming.MOD_ID, "textures/ui/node_marker.png"
    );
    
    private static final float[][] uvSequence = {
            {0.0f, 1.0f},
            {0.0f, 0.0f},
            {1.0f, 0.0f},
            {1.0f, 1.0f}
    };
    
    private Vec3 cameraUp;
    private Vec3 cameraRight;
    
    public NodeMarkerRenderer(Minecraft client,
                              PoseStack poseStack
    ) {
        super(client, poseStack);
    }
    
    @Override
    protected void startBatch() {
        super.startBatch(
                GameRenderer::getPositionTexColorShader,
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX_COLOR
        );
        RenderSystem.setShaderTexture(0, NODE_MARKER);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        cameraRight = new Vec3(this.camera.getLeftVector()).scale(-1.0f);
        cameraUp = new Vec3(this.camera.getUpVector());
    }
    
    public void renderNode(
            long nodePos,
            Color nodeColor
    ) {
        int packedColor = getPackedColor(nodeColor);
        
        Vec3 worldPos = BlockPos.of(nodePos)
                                .getCenter();
        
        float width = 0.25f;
        float height = 0.25f;
        
        for (float[] uv : uvSequence) {
            float u = uv[0];
            float v = uv[1];
            
            float rightScale = (u * 2.0f) - 1.0f;
            float upScale = ((1.0f - v) * 2.0f) - 1.0f;
            
            Vec3 renderPos = getBillboardCorner(worldPos, upScale, rightScale, width / 2, height / 2);
            
            drawVertex(renderPos, u, v, packedColor);
        }
    }
    
    public void renderNode(long nodePos) {
        renderNode(nodePos, new Color(255, 255, 255));
    }
    
    private void drawVertex(Vec3 renderPos,
                            float u,
                            float v,
                            int packedColor
    ) {
        bufferBuilder.vertex(poseMatrix, (float) renderPos.x(), (float) renderPos.y(), (float) renderPos.z())
                     .uv(u, v)
                     .color(packedColor)
                     .endVertex();
    }
    
    private Vec3 getBillboardCorner(
            Vec3 centerPos,
            float upScale,
            float rightScale,
            float halfWidth,
            float halfHeight
    ) {
        Vec3 offset = cameraRight.scale(rightScale * halfWidth)
                                 .add(cameraUp.scale(upScale * halfHeight));
        
        return centerPos.add(offset);
    }
}
