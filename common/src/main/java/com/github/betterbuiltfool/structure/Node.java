package com.github.betterbuiltfool.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;

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
    
    private final class NodeIterator implements Iterator<Node> {
        private final HashSet<Node> traversed;
        private final Stack<Edge> toTraverse;
        
        public NodeIterator(Node start){
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
            var nextEdge = toTraverse.pop();
            var nextNode = nextEdge.getEndNode();
            for (Edge newEdge: nextNode.edges) {
                if (traversed.contains(newEdge.getEndNode())) {
                    continue;
                }
                toTraverse.add(newEdge);
            }
            traversed.add(nextNode);
            return nextNode;
        }
    }
}
