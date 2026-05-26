package com.github.betterbuiltfool.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

public class Node {
    private final HashSet<Edge> edges;
    private final BlockPos nodePosition;
    
    public Node(Vec3i position) {
        nodePosition = new BlockPos(position);
        edges = new HashSet<>();
    }
    
    public Node(Vec3i position, Edge edge) {
        this(position);
        edges.add(edge);
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
        return edgeToNode(new Node(position), material);
    }
    
    public Edge edgeToNode(Node node, Block material) {
        assert node != this;
        var edge = new StructureEdge(this, node, material);
        this.addEdge(edge);
        node.addEdge(edge);
        return edge;
    }
    
    public Iterable<Node> getStructure() {
        return new NodeIterable(this);
    }
    
    private static final class NodeIterable implements Iterable<Node> {
        private final Node start;
        
        private NodeIterable(Node start) {
            this.start = start;
        }
        
        public @NotNull Iterator<Node> iterator() {
            return new NodeIterator(this.start);
        }
        
        private static final class NodeIterator implements Iterator<Node> {
            private final HashSet<Node> traversed;
            private final Stack<Edge> toTraverse;
            
            private NodeIterator(Node start){
                this.traversed = new HashSet<>();
                this.traversed.add(start);
                
                this.toTraverse = new Stack<>();
            }
            
            @Override
            public boolean hasNext() {
                return !this.toTraverse.empty();
            }
            
            @Override
            public Node next() {
                var nextEdge = this.toTraverse.pop();
                var nextNode = nextEdge.getEndNode();
                for (Edge newEdge: nextNode.edges) {
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
