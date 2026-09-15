package com.github.betterbuiltfool.geometry;

import net.minecraft.core.Direction;

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
