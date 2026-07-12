package com.github.betterbuiltfool.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.*;

public class LineRenderer {
    private final PoseStack poseStack;
    private final Camera camera;
    private final Tesselator tesselator;
    private final BufferBuilder bufferBuilder;
    private Matrix4f poseMatrix;
    
    public LineRenderer(Minecraft client,
                        PoseStack poseStack
    ) {
        this.poseStack = poseStack;
        this.camera = client.getEntityRenderDispatcher().camera;
        this.tesselator = Tesselator.getInstance();
        this.bufferBuilder = tesselator.getBuilder();
    }
    
    public void startBatch() {
        Vec3 cameraPos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        poseMatrix = poseStack.last()
                              .pose();
        
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
    }
    
    public void finishBatch() {
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        ;
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
    
    public void renderLine(long startPos,
                           long endPos,
                           Color lineColor
    ) {
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        int packedColor = FastColor.ABGR32.color(
                lineColor.getAlpha(),
                lineColor.getRed(),
                lineColor.getGreen(),
                lineColor.getBlue()
        );
        
        Vec3 start = BlockPos.of(startPos)
                             .getCenter();
        Vec3 end = BlockPos.of(endPos)
                           .getCenter();
        bufferBuilder.vertex(poseMatrix, (float) start.x, (float) start.y, (float) start.z)
                     .color(packedColor)
                     .endVertex();
        bufferBuilder.vertex(poseMatrix, (float) end.x, (float) end.y, (float) end.z)
                     .color(packedColor)
                     .endVertex();
        tesselator.end();
    }
}
