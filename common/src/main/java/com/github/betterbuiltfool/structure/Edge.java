package com.github.betterbuiltfool.structure;

import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract object representing a connection between two nodes.
 */
abstract public class Edge {
    private final Node startNode;
    private final Node endNode;
    
    public Edge(Node start, Node end) {
        startNode = start;
        endNode = end;
    }
    
    public Node getStartNode() {
        return startNode;
    }
    
    public Node getEndNode() {
        return endNode;
    }
    
    abstract public int getLength();
    abstract public int getMaterialCost();
    abstract public void generateFill(@NotNull Level level);
}
