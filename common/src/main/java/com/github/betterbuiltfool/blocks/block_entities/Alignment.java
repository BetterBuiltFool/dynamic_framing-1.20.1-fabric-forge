package com.github.betterbuiltfool.blocks.block_entities;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

// Of note: the ordinals of these value are used for bitwise mapping and unpacking, so altering the order
// later may have consequences.
public enum Alignment implements StringRepresentable {
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
    
    public Alignment push() {
        return CACHE[(this.ordinal() - 1) % CACHE.length];
    }
    
    public static Alignment fromValue(int value) {
        if (value < 0) return NEGATIVE;
        if (value > 0) return POSITIVE;
        return CENTER;
    }
    
    public static Alignment fromOrdinal(int ordinal) {
        return CACHE[ordinal];
    }
    
    @Override
    public @NotNull String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
