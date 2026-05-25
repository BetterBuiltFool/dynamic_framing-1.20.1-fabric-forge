package com.github.betterbuiltfool.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

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
}
