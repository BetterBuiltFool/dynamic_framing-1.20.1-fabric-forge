package com.github.betterbuiltfool.structure;

import java.util.HashSet;

public class Node {
    private final HashSet<Edge> edges;
    
    public Node() {
        edges = new HashSet<>();
    }
    
    public Node(Edge edge) {
        edges = new HashSet<>();
        edges.add(edge);
    }
    
    public Node(HashSet<Edge> inherited) {
        edges = inherited;
    }
    
    public void addEdge(Edge edge) {
        edges.add(edge);
    }
}
