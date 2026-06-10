package com.github.betterbuiltfool.structure;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract object representing a connection between two nodes.
 */
abstract public class Edge {
    private final JointNode startNode;
    private final JointNode endNode;
    
    public Edge(JointNode start, JointNode end) {
        startNode = start;
        endNode = end;
    }
    
    public JointNode getStartNode() {
        return startNode;
    }
    
    public JointNode getEndNode() {
        return endNode;
    }
    
    abstract public int getLength();
    abstract public int getMaterialCost(@NotNull Level level);
    abstract public void generateFill(@NotNull Level level, Block edgeMaterial);
}
