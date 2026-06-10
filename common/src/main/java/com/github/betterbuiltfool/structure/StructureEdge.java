package com.github.betterbuiltfool.structure;

import com.github.betterbuiltfool.validation.BlockPosValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class StructureEdge extends Edge{
    
    public StructureEdge(JointNode start, JointNode end) {
        super(start, end);
    }
    
    @Override
    public int getLength() {
        return (int) getStartNode().getPosition().distToCenterSqr(getEndNode().getPosition().getCenter());
    }
    
    @Override
    public int getMaterialCost(@NotNull Level level) {
        // TODO: Add a filter to remove the irreplaceable blocks
        return Math.toIntExact(
                BlockPos.betweenClosedStream(
                        getStartNode().getPosition(),
                        getEndNode().getPosition())
                .filter(blockPos -> BlockPosValidator.validateEdgePlacement(level, blockPos))
                .count());
    }
    
    @Override
    public void generateFill(@NotNull Level level, Block edgeMaterial) {
        var firstPos = getStartNode().getPosition();
        var secondPos = getEndNode().getPosition();
        BlockPos.betweenClosedStream(firstPos, secondPos)
            .filter(blockPos -> BlockPosValidator.validateEdgePlacement(level, blockPos))
            .forEach(pos -> {
            var currentBlockState = level.getBlockState(pos);
            if (!currentBlockState.isAir()){
                return;
            }
            
            var directionVector = firstPos.subtract(secondPos);
            
            var facing = Direction.getNearest(
                    directionVector.getX(),
                    directionVector.getY(),
                    directionVector.getZ()
            );
            
            var newBlockState = edgeMaterial.defaultBlockState().setValue(BlockStateProperties.AXIS, facing.getAxis());
            
            level.setBlockAndUpdate(pos, newBlockState);
            
        });
    }
}
