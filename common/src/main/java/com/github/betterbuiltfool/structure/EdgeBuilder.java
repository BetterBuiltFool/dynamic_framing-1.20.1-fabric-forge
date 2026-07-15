package com.github.betterbuiltfool.structure;

import com.github.betterbuiltfool.validation.BlockPosValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;


public class EdgeBuilder {
    
    public static void build(Level level,
                             long firstPos,
                             long secondPos,
                             Block edgeMaterial
    ) {
        var startPos = BlockPos.of(firstPos);
        var endPos = BlockPos.of(secondPos);
        
        var directionVector = startPos.subtract(endPos);
        var facing = Direction.getNearest(
                directionVector.getX(),
                directionVector.getY(),
                directionVector.getZ()
        );
        
        var blockState = edgeMaterial.defaultBlockState()
                                     .setValue(BlockStateProperties.AXIS, facing.getAxis());
        
        BlockPos.betweenClosedStream(BlockPos.of(firstPos), BlockPos.of(secondPos))
                .filter(blockPos -> BlockPosValidator.validate(level, blockPos))
                .forEach(pos -> level.setBlockAndUpdate(pos, blockState));
        
    }
}
