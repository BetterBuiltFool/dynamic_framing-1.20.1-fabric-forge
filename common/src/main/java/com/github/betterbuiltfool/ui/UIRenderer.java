package com.github.betterbuiltfool.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.function.Supplier;

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
    
    abstract void startBatch();
    
    protected void startBatch(
            Supplier<ShaderInstance> shaderSupplier,
            VertexFormat.Mode mode,
            VertexFormat vertexFormat
    ) {
        Vec3 cameraPos = camera.getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        poseMatrix = poseStack.last()
                              .pose();
        
        RenderSystem.setShader(shaderSupplier);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        bufferBuilder.begin(mode, vertexFormat);
    }
    
    public void finishBatch() {
        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
    
    public int getPackedColor(Color color) {
        return FastColor.ABGR32.color(
                color.getAlpha(),
                color.getRed(),
                color.getGreen(),
                color.getBlue()
        );
    }
}
