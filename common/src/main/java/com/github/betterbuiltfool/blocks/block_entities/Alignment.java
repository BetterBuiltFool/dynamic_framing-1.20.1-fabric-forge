package com.github.betterbuiltfool.blocks.block_entities;

// Of note: the ordinals of these value are used for bitwise mapping and unpacking, so altering the order
// later may have consequences.
public enum Alignment {
    POSITIVE(1),
    CENTER(0),
    NEGATIVE(-1);
    
    final int alignment;
    private final static Alignment[] CACHE = Alignment.values();
    
    Alignment(int alignment) {
        this.alignment = alignment;
    }
    
    public int getAlignment() {
        return alignment;
    }
    
    public static Alignment fromValue(int value) {
        if (value < 0) return NEGATIVE;
        if (value > 0) return POSITIVE;
        return CENTER;
    }
    
    public static Alignment fromOrdinal(int ordinal) {
        return CACHE[ordinal];
    }
}
