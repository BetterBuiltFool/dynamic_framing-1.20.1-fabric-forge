package com.github.betterbuiltfool.validation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class EdgeValidator {
    
    public static boolean validate(
            Level level,
            long start,
            long end
    ) {
        return BlockPos.betweenClosedStream(BlockPos.of(start), BlockPos.of(end))
                       .filter(blockPos -> blockPos.asLong() != start && blockPos.asLong() != end)
                       .allMatch(blockPos -> BlockPosValidator.validate(level, blockPos));
    }
}
