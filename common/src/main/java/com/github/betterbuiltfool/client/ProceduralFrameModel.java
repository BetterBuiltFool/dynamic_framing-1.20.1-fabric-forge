package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ProceduralFrameModel {
    
    public static @NotNull ArrayList<BakedQuad> generateQuads(
            Direction side,
            RandomSource rand,
            Alignment alignX,
            Alignment alignY,
            Alignment alignZ,
            Size size,
            BlockState material
    ) {
        
        var dispatcher = Minecraft.getInstance()
                                  .getBlockRenderer();
        var materialModel = dispatcher.getBlockModel(material);
        var originalQuads = materialModel.getQuads(material, side, rand);
        var newQuads = new ArrayList<BakedQuad>();
        
        float scale = size.getThickness();
        var xBounds = calcAxisBounds(alignX, size);
        var yBounds = calcAxisBounds(alignY, size);
        var zBounds = calcAxisBounds(alignZ, size);
        
        float[][] bounds = {xBounds, yBounds, zBounds};
        
        for (var quad : originalQuads) {
            int[] vertices = quad.getVertices()
                                 .clone();
            var face = quad.getDirection();
            
            for (int i = 0; i < 4; i++) {
                int offset = i * 8;
                
                for (int j = 0; j < 3; j++) {
                    float original = Float.intBitsToFloat(vertices[offset + j]);
                    float modified = bounds[j][0] + (original * scale);
                    vertices[offset + j] = Float.floatToRawIntBits(modified);
                }
                
                if (face.getAxis() == Direction.Axis.Z) {
                    calcUV(vertices, quad, offset, scale, xBounds, yBounds, true);
                } else if (face.getAxis() == Direction.Axis.X) {
                    calcUV(vertices, quad, offset, scale, zBounds, yBounds, true);
                } else {
                    calcUV(vertices, quad, offset, scale, xBounds, yBounds, false);
                }
            }
        }
        return newQuads;
    }
    
    private static void calcUV(
            int[] vertices,
            BakedQuad quad,
            int offset,
            float scale,
            float[] uBounds,
            float[] vBounds,
            boolean invertV
    ) {
        
        float u = Float.intBitsToFloat(vertices[offset + 4]);
        float v = Float.intBitsToFloat(vertices[offset + 5]);
        
        float uMin = quad.getSprite()
                         .getU0();
        float uMax = quad.getSprite()
                         .getU1();
        float vMin = quad.getSprite()
                         .getV0();
        float vMax = quad.getSprite()
                         .getV1();
        
        float localU = (u - uMin) / (uMax - uMin);
        float localV = (v - vMin) / (vMax - vMin);
        
        localU = uBounds[0] + (localU * scale);
        if (invertV) {
            localV = 1.0f - (vBounds[0] + (localV * scale));
        } else {
            localV = vBounds[0] + (localV * scale);
        }
        
        vertices[offset + 4] = Float.floatToRawIntBits(uMin + localU * (uMax - uMin));
        vertices[offset + 5] = Float.floatToRawIntBits(vMin + localV * (vMax - vMin));
    }
    
    private static float[] calcAxisBounds(Alignment alignment,
                                          Size size
    ) {
        float scale = size.getThickness();
        float start;
        
        switch (alignment) {
            case NEGATIVE -> start = 0.0f;
            case POSITIVE -> start = 1.0f - scale;
            case CENTER -> start = 0.5f - (scale / 2.0f);
            default -> throw new IllegalArgumentException("What kind of alignment enum is this?");
        }
        return new float[]{start, start + scale};
    }
}
