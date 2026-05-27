package com.github.betterbuiltfool.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class JointNode {
    private final HashSet<Edge> edges;
    private final BlockPos nodePosition;
    
    public JointNode(Vec3i position) {
        nodePosition = new BlockPos(position);
        edges = new HashSet<>();
    }
    
    public void addEdge(Edge edge) {
        edges.add(edge);
    }
    
    public Iterator<Edge> getEdges() {
        return this.edges.iterator();
    }
    
    public BlockPos getPosition() {
        return nodePosition;
    }
    
    public Edge edgeToPosition(Vec3i position, Block material) {
        assert position != nodePosition;
        return edgeToNode(new JointNode(position), material);
    }
    
    public Edge edgeToNode(JointNode node, Block material) {
        assert node != this;
        var edge = new StructureEdge(this, node, material);
        this.addEdge(edge);
        node.addEdge(edge);
        return edge;
    }
    
    /**
     * Allows traversal of the entire structure to which the node is attached.
     *
     * @return An iterable of all Nodes connected to the starting Node.
     */
    public Iterable<JointNode> getStructure() {
        return new NodeIterable(this);
    }
    
    private record NodeIterable(JointNode start) implements Iterable<JointNode> {
        
        public @NotNull Iterator<JointNode> iterator() {
                return new NodeIterator(this.start);
            }
            
            private static final class NodeIterator implements Iterator<JointNode> {
                private final HashSet<JointNode> traversed;
                private final ArrayDeque<Edge> toTraverse;
                
                private NodeIterator(JointNode start) {
                    this.traversed = new HashSet<>();
                    this.traversed.add(start);
                    
                    this.toTraverse = new ArrayDeque<>();
                }
                
                @Override
                public boolean hasNext() {
                    return !this.toTraverse.isEmpty();
                }
                
                @Override
                public JointNode next() {
                    var nextEdge = this.toTraverse.removeFirst();
                    var nextNode = nextEdge.getEndNode();
                    for (Edge newEdge : nextNode.edges) {
                        if (this.traversed.contains(newEdge.getEndNode())) {
                            continue;
                        }
                        this.toTraverse.add(newEdge);
                    }
                    this.traversed.add(nextNode);
                    return nextNode;
                }
            }
        }
}
