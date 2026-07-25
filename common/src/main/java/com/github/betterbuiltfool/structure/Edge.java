package com.github.betterbuiltfool.structure;

import com.github.betterbuiltfool.data.CoaxSelection;
import net.minecraft.core.BlockPos;

public record Edge(long firstPos, long secondPos) {
    public Edge {
        if (firstPos > secondPos) {
            var temp = firstPos;
            firstPos = secondPos;
            secondPos = temp;
        }
    }
    
    public boolean intersectedBy(long targetPos) {
        int tx = BlockPos.getX(targetPos);
        int ty = BlockPos.getY(targetPos);
        int tz = BlockPos.getZ(targetPos);
        
        int fx = BlockPos.getX(firstPos);
        int fy = BlockPos.getY(firstPos);
        int fz = BlockPos.getZ(firstPos);
        
        int sx = BlockPos.getX(secondPos);
        int sy = BlockPos.getY(secondPos);
        int sz = BlockPos.getZ(secondPos);
        
        return tx >= Math.min(fx, sx) && tx <= Math.max(fx, sx) &&
               ty >= Math.min(fy, sy) && tx <= Math.max(fy, sy) &&
               tz >= Math.min(fz, sz) && tx <= Math.max(fz, sz);
    }
    
    /**
     * Returns the opposing end of the edge. Throws an exception if the end given is not actually an endpoint for the
     * edge.
     *
     * @param endPos A position, as a packed long, known to be one end or another of the edge.
     *
     * @return A long representing the opposing end from endPos.
     */
    public long getOpposingEnd(long endPos) {
        if (endPos == firstPos) {
            return secondPos;
        }
        if (endPos == secondPos) {
            return firstPos;
        }
        
        throw new IllegalArgumentException(String.format(
                "Invalid position for edge, %s. Valid positions are %s and %s",
                BlockPos.of(endPos),
                BlockPos.of(firstPos),
                BlockPos.of(secondPos)
        ));
    }
    
    public boolean isCoaxialTo(long position) {
        int px = BlockPos.getX(position);
        int py = BlockPos.getY(position);
        int pz = BlockPos.getZ(position);
        
        return (
                CoaxSelection.isCoaxial(this.firstPos, px, py, pz) &&
                CoaxSelection.isCoaxial(this.secondPos, px, py, pz)
        );
    }
    
    public long getClosestEnd(long target) {
        int tx = BlockPos.getX(target);
        int ty = BlockPos.getY(target);
        int tz = BlockPos.getZ(target);
        
        int x1 = BlockPos.getX(firstPos);
        int y1 = BlockPos.getY(firstPos);
        int z1 = BlockPos.getZ(firstPos);
        
        int y2 = BlockPos.getY(secondPos);
        int z2 = BlockPos.getZ(secondPos);
        int x2 = BlockPos.getX(secondPos);
        
        long distFirstSqr = distanceSqr(x1, y1, z1, tx, ty, tz);
        long distSecondSqr = distanceSqr(x2, y2, z2, tx, ty, tz);
        
        if (distFirstSqr <= distSecondSqr) {
            return firstPos;
        }
        return secondPos;
    }
    
    public static long distanceSqr(long firstPos,
                                   long secondPos
    ) {
        int x1 = BlockPos.getX(firstPos);
        int y1 = BlockPos.getY(firstPos);
        int z1 = BlockPos.getZ(firstPos);
        int y2 = BlockPos.getY(secondPos);
        int z2 = BlockPos.getZ(secondPos);
        int x2 = BlockPos.getX(secondPos);
        
        return distanceSqr(x1, y1, z1, x2, y2, z2);
    }
    
    public static long distanceSqr(int x1,
                                   int y1,
                                   int z1,
                                   int x2,
                                   int y2,
                                   int z2
    ) {
        long dx = x1 - x2;
        long dy = y1 - y2;
        long dz = z1 - z2;
        
        return dx * dx + dy * dy + dz * dz;
    }
    
    public Edge.Split splitAt(long splitPos) {
        long firstPos = this.firstPos;
        long secondPos = this.secondPos;
        
        return new Edge.Split(
                new Edge(firstPos, splitPos),
                new Edge(splitPos, secondPos)
        );
    }
    
    public record Split(Edge upper, Edge lower) {}
    
}
