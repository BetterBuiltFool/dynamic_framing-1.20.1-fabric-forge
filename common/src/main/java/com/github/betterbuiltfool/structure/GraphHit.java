package com.github.betterbuiltfool.structure;

public sealed interface GraphHit {
    
    double distance();
    
    record NodeHit(long packedPos, double distance) implements GraphHit {}
    
    record EdgeHit(long posA, long posB, long hitPos, double distance) implements GraphHit {}
}
