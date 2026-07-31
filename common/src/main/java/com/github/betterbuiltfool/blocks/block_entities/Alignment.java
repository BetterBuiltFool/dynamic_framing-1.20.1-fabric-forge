package com.github.betterbuiltfool.blocks.block_entities;

public enum Alignment {
    POSITIVE(1),
    CENTER(0),
    NEGATIVE(-1);
    
    final int alignment;
    
    Alignment(int alignment) {
        this.alignment = alignment;
    }
    
    public int getAlignment() {
        return alignment;
    }
}
