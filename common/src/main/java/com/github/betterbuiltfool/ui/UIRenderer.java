package com.github.betterbuiltfool.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

abstract class UIRenderer {
    
    private final PoseStack poseStack;
    private final Camera camera;
    protected final Tesselator tesselator;
    protected final BufferBuilder bufferBuilder;
    protected Matrix4f poseMatrix;
    
    public UIRenderer(
            Minecraft client,
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
        
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}
