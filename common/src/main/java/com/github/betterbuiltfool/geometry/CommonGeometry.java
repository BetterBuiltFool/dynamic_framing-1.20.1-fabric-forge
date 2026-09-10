package com.github.betterbuiltfool.geometry;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;

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
    
    public record Bounds(float[] x, float[] y, float[] z) {}
}
