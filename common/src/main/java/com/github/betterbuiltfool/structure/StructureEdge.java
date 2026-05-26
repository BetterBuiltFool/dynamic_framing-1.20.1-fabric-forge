package com.github.betterbuiltfool.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

public class StructureEdge extends Edge{
    private final Block edgeMaterial;
    
    public StructureEdge(Node start, Node end, Block material) {
        super(start, end);
        edgeMaterial = material;
    }
    
    @Override
    public int getLength() {
        return (int) getStartNode().getPosition().distToCenterSqr(getEndNode().getPosition().getCenter());
    }
    
    @Override
    public int getMaterialCost() {
        // TODO: Add a filter to remove the irreplaceable blocks
        return Math.toIntExact(
                BlockPos.betweenClosedStream(
                        getStartNode().getPosition(),
                        getEndNode().getPosition())
                .count());
    }
    
    @Override
    public void generateFill(@NotNull Level level) {
        var firstPos = getStartNode().getPosition();
        var secondPos = getEndNode().getPosition();
        BlockPos.betweenClosedStream(firstPos, secondPos).forEach(pos -> {
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
