package com.github.betterbuiltfool.geometry;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import net.minecraft.core.Direction;

public record BeamGeometryData(
        float minX,
        float minY,
        float minZ,
        float maxX,
        float maxY,
        float maxZ
) {
    
    private static BeamGeometryData from(float[] primaryBounds,
                                         Direction.Axis primaryAxis,
                                         float[] secondaryBounds,
                                         Direction.Axis secondaryAxis
    ) {
        float minX = 0.0f, minY = 0.0f, minZ = 0.0f;
        float maxX = 1.0f, maxY = 1.0f, maxZ = 1.0f;
        
        switch (primaryAxis) {
            case X -> {
                minX = primaryBounds[0];
                maxX = primaryBounds[1];
            }
            case Y -> {
                minY = primaryBounds[0];
                maxY = primaryBounds[1];
            }
            default -> {
                minZ = primaryBounds[0];
                maxZ = primaryBounds[1];
            }
        }
        
        switch (secondaryAxis) {
            case X -> {
                minX = secondaryBounds[0];
                maxX = secondaryBounds[1];
            }
            case Y -> {
                minY = secondaryBounds[0];
                maxY = secondaryBounds[1];
            }
            default -> {
                minZ = secondaryBounds[0];
                maxZ = secondaryBounds[1];
            }
        }
        
        return new BeamGeometryData(minX, minY, minZ, maxX, maxY, maxZ);
    }
    
    public static BeamGeometryData from(
            Alignment primary,
            Alignment secondary,
            Direction.Axis axis,
            Size size
    ) {
        var scale = size.getThickness();
        var primaryBounds = calcAxis(primary, scale);
        var secondaryBounds = calcAxis(secondary, scale);
        Direction.Axis primaryAxis, secondaryAxis;
        
        switch (axis) {
            case X -> {
                primaryAxis = Direction.Axis.Y;
                secondaryAxis = Direction.Axis.Z;
            }
            case Y -> {
                primaryAxis = Direction.Axis.X;
                secondaryAxis = Direction.Axis.Z;
            }
            default -> {
                primaryAxis = Direction.Axis.X;
                secondaryAxis = Direction.Axis.Y;
            }
        }
        
        return from(primaryBounds, primaryAxis, secondaryBounds, secondaryAxis);
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
}
