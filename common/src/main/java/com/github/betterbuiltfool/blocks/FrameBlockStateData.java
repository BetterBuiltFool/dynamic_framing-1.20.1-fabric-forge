package com.github.betterbuiltfool.blocks;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;

public class FrameBlockStateData {
    private Alignment alignX;
    private Alignment alignY;
    private Alignment alignZ;
    private Size size;
    
    public FrameBlockStateData(
            Alignment alignX,
            Alignment alignY,
            Alignment alignZ,
            Size size
    ) {
        this.alignX = alignX;
        this.alignY = alignY;
        this.alignZ = alignZ;
        this.size = size;
    }
    
    public static FrameBlockStateData of(int packed) {
        return new FrameBlockStateData(
                FrameBlockStateData.getX(packed),
                FrameBlockStateData.getY(packed),
                FrameBlockStateData.getZ(packed),
                FrameBlockStateData.getSize(packed)
        );
    }
    
    public int asInt() {
        return alignX.ordinal() << 9 |
               alignY.ordinal() << 6 |
               alignZ.ordinal() << 3 |
               size.ordinal();
    }
    
    public Alignment getX() {
        return alignX;
    }
    
    public Alignment getY() {
        return alignY;
    }
    
    public Alignment getZ() {
        return alignZ;
    }
    
    public Size getSize() {
        return size;
    }
    
    public static Alignment getX(int packed) {
        var ordinal = (packed >> 9) & 7;
        return Alignment.fromOrdinal(ordinal);
    }
    public static Alignment getY(int packed) {
        var ordinal = (packed >> 6) & 7;
        return Alignment.fromOrdinal(ordinal);
    }
    public static Alignment getZ(int packed) {
        var ordinal = (packed >> 3) & 7;
        return Alignment.fromOrdinal(ordinal);
    }
    public static Size getSize(int packed) {
        var ordinal = packed & 7;
        return Size.fromOrdinal(ordinal);
    }
}
