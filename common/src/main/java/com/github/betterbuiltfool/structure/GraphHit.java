package com.github.betterbuiltfool.structure;

public sealed interface GraphHit {
    record NodeHit(long packedPos, double distance) implements GraphHit {}
    
    record EdgeHit(long posA, long posB, double distance) implements GraphHit {}
}
