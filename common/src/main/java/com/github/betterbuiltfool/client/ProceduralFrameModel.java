package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ProceduralFrameModel {
    
    public static @NotNull ArrayList<BakedQuad> generateQuads(
            Direction side,
            RandomSource rand,
            Alignment primary,
            Alignment secondary,
            Direction.Axis axis,
            Size size,
            BlockState material
    ) {
        var dispatcher = Minecraft.getInstance()
                                  .getBlockRenderer();
        var materialModel = dispatcher.getBlockModel(material);
        var scale = size.getThickness();
        var bounds = calcAxisBounds(primary, secondary, axis, scale);
        var faces = new ArrayList<BakedQuad>();
        
        for (var quad : materialModel.getQuads(material, side, rand)) {
            int[] vertices = quad.getVertices()
                                 .clone();
            var facing = quad.getDirection();
            
            for (int i = 0; i < 4; i++) {
                int offset = i * 8;
                
                adjustBounds(vertices, bounds, offset);
                
                switch (facing.getAxis()) {
                    case X -> adjustUV(vertices, quad, offset, scale, bounds.z(), bounds.y(), true);
                    case Y -> adjustUV(vertices, quad, offset, scale, bounds.x(), bounds.z(), false);
                    default -> adjustUV(vertices, quad, offset, scale, bounds.x(), bounds.y(), true);
                }
            }
            faces.add(new BakedQuad(vertices, quad.getTintIndex(), facing, quad.getSprite(), quad.isShade()));
        }
        return faces;
    }
    
    private static void adjustBounds(
            int[] vertices,
            Bounds bounds,
            int vertexOffset
    ) {
        for (var axis : Direction.Axis.values()) {
            adjustBound(vertices, bounds, vertexOffset, axis);
        }
    }
    
    private static void adjustBound(
            int[] vertices,
            Bounds bounds,
            int vertexOffset,
            Direction.Axis axis
    ) {
        int positionOffset;
        float[] axisBounds;
        switch (axis) {
            case X -> {
                positionOffset = 0;
                axisBounds = bounds.x();
            }
            case Y -> {
                positionOffset = 1;
                axisBounds = bounds.y();
            }
            default -> {
                positionOffset = 2;
                axisBounds = bounds.z();
            }
        }
        float original = Float.intBitsToFloat(vertices[vertexOffset + positionOffset]);
        float modified = Mth.clamp(original, axisBounds[0], axisBounds[1]);
        vertices[vertexOffset + positionOffset] = Float.floatToRawIntBits(modified);
    }
    
    private static void adjustUV(
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
    
    private static Bounds calcAxisBounds(
            Alignment primary,
            Alignment secondary,
            Direction.Axis axis,
            float scale
    ) {
        final float[] FULL = {0.0f, 1.0f};
        
        switch (axis) {
            case X -> {
                return new Bounds(FULL, calcAxis(primary, scale), calcAxis(secondary, scale));
            }
            case Y -> {
                return new Bounds(calcAxis(primary, scale), FULL, calcAxis(secondary, scale));
            }
            default -> {
                return new Bounds(calcAxis(primary, scale), calcAxis(secondary, scale), FULL);
            }
        }
    }
    
    private static float[] calcAxis(Alignment alignment,
                                    float scale
    ) {
        float start;
        
        switch (alignment) {
            case NEGATIVE -> start = 0.0f;
            case POSITIVE -> start = 1.0f - scale;
            default -> start = 0.5f - (scale / 2.0f);
        }
        return new float[]{start, start + scale};
    }
    
    private record Bounds(float[] x, float[] y, float[] z) {}
}
