package com.github.betterbuiltfool.blocks.block_entities;

// Of note: the ordinals of these value are used for bitwise mapping and unpacking, so altering the order
// later may have consequences.
public enum Size {
    QUARTER(0.25f),
    HALF(0.5f),
    FULL(0.75f);
    
    final float thickness;
    
    private static final Size[] CACHE = Size.values();
    
    Size(float thickness) {
        this.thickness = thickness;
    }
    
    public float getThickness() {
        return thickness;
    }
    
    public static Size fromOrdinal(int ordinal) {
        return CACHE[ordinal];
    }
}
