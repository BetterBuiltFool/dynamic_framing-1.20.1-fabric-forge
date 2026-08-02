package com.github.betterbuiltfool.helper;

import com.github.betterbuiltfool.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;

public class FrameEndpointHelper {
    
    public static BlockPos findEndPoint(BlockAndTintGetter level,
                                  BlockPos start,
                                  Direction direction
    ) {
        var current = start.mutable();
        while (true) {
            current.move(direction);
            var state = level.getBlockState(current);
            if (state.is(BlockRegistry.JOINT_BLOCK.get())) {
                return current.immutable();
            }
            
            if (!state.is(BlockRegistry.POST_BLOCK.get()) || !state.is(BlockRegistry.BEAM_BLOCK.get())) {
                return start;
            }
        }
    }
}
