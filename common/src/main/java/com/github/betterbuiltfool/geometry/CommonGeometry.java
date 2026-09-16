package com.github.betterbuiltfool.geometry;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
    
    
    public static void adjustToBounds(
            int[] vertices,
            Bounds bounds,
            int vertexOffset
    ) {
        for (var axis : Direction.Axis.values()) {
            adjustBoundAxis(vertices, bounds, vertexOffset, axis);
        }
    }
    
    private static void adjustBoundAxis(
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
    
    public static float[][] getUVBounds(
            Direction.Axis axis,
            Direction.Axis facingAxis,
            Bounds bounds
    ) {
        float[] uBounds;
        float[] vBounds;
        
        switch (facingAxis) {
            case X -> {
                uBounds = bounds.z();
                vBounds = bounds.y();
            }
            case Y -> {
                uBounds = bounds.x();
                vBounds = bounds.z();
            }
            default -> {
                uBounds = bounds.x();
                vBounds = bounds.y();
            }
        }
        if (CommonGeometry.shouldFlip(axis, facingAxis)) {
            var temp = uBounds;
            uBounds = vBounds;
            vBounds = temp;
        }
        return new float[][]{uBounds, vBounds};
    }
    
    private static boolean shouldFlip(Direction.Axis axis,
                                      Direction.Axis facingAxis
    ) {
        return ((axis == Direction.Axis.X && facingAxis != Direction.Axis.X) ||
                (axis == Direction.Axis.Z && facingAxis == Direction.Axis.X));
    }
    
    public static void adjustUV(
            int[] vertices,
            BakedQuad quad,
            int offset,
            float[] uBounds,
            float[] vBounds
    ) {
        
        float u = Float.intBitsToFloat(vertices[offset + 4]);
        float v = Float.intBitsToFloat(vertices[offset + 5]);
        
        TextureAtlasSprite sprite = quad.getSprite();
        float uMin = sprite.getU0();
        float uMax = sprite.getU1();
        float vMin = sprite.getV0();
        float vMax = sprite.getV1();
        
        float localU = (u - uMin) / (uMax - uMin);
        float localV = (v - vMin) / (vMax - vMin);
        
        float mappedU = uBounds[0] + (localU * (uBounds[1] - uBounds[0]));
        float mappedV = vBounds[0] + (localV * (vBounds[1] - vBounds[0]));
        
        vertices[offset + 4] = Float.floatToRawIntBits(uMin + mappedU * (uMax - uMin));
        vertices[offset + 5] = Float.floatToRawIntBits(vMin + mappedV * (vMax - vMin));
    }
}
