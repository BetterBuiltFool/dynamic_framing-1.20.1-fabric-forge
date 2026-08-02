package com.github.betterbuiltfool.helper;

import com.github.betterbuiltfool.blocks.BeamBlock;
import com.github.betterbuiltfool.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

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
    
    public static Direction getNegativeAxis(BlockState state,
                                            boolean isVertical
    ) {
        if (isVertical) {
            return Direction.DOWN;
        }
        if (state.getValue(BeamBlock.AXIS) == Direction.Axis.X) {
            return Direction.WEST;
        } else {
            return Direction.NORTH;
        }
    }
}
