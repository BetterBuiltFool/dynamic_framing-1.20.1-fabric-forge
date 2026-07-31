package com.github.betterbuiltfool.blocks.block_entities;

public enum Size {
    QUARTER(0.25f),
    HALF(0.5f),
    FULL(0.75f);
    
    final float thickness;
    
    Size(float thickness) {
        this.thickness = thickness;
    }
    
    public float getThickness() {
        return thickness;
    }
}
