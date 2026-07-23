package com.github.betterbuiltfool.structure;

public record Edge(long firstPos, long secondPos) {
    public Edge {
        if (firstPos > secondPos) {
            var temp = firstPos;
            firstPos = secondPos;
            secondPos = temp;
        }
    }
}
