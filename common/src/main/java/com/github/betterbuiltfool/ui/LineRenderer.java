package com.github.betterbuiltfool.ui;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class LineRenderer extends UIRenderer<LineRenderer> {
    
    public LineRenderer(Minecraft client,
                        PoseStack poseStack
    ) {
        super(client, poseStack);
    }
    
    @Override
    protected void startBatch() {
        super.startBatch(
                GameRenderer::getPositionColorShader,
                VertexFormat.Mode.DEBUG_LINES,
                DefaultVertexFormat.POSITION_COLOR
        );
    }
    
    public void renderLine(long startPos,
                           long endPos,
                           Color lineColor
    ) {
        int packedColor = getPackedColor(lineColor);
        
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
    }
}
