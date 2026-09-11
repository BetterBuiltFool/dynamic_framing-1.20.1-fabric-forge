package com.github.betterbuiltfool.geometry;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class CommonGeometry {
    
    public static float[] calcAxis(Alignment alignment,
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
    
    
    public static void adjustBounds(
            int[] vertices,
            CommonGeometry.Bounds bounds,
            int vertexOffset
    ) {
        for (var axis : Direction.Axis.values()) {
            adjustBound(vertices, bounds, vertexOffset, axis);
        }
    }
    
    private static void adjustBound(
            int[] vertices,
            CommonGeometry.Bounds bounds,
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
    
    public static void adjustUV(
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
    
    public record Bounds(float[] x, float[] y, float[] z) {
        
        public Bounds(Bounds original) {
            this(original.x, original.y, original.z);
        }
        
        public float[] get(Direction.Axis axis) {
            return switch (axis) {
                case X -> x;
                case Y -> y;
                default -> z;
            };
        }
    }
}
