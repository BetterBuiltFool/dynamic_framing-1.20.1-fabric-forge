package com.github.betterbuiltfool.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record Edge(long firstPos, long secondPos, Direction.Axis axis) {
    public Edge(long firstPos,
                long secondPos
    ) {
        this(
                Math.min(firstPos, secondPos),
                Math.max(firstPos, secondPos),
                calculateAxis(firstPos, secondPos)
        );
    }
    
    private static Direction.Axis calculateAxis(long start,
                                                long end
    ) {
        if (BlockPos.getX(start) != BlockPos.getX(end)) {
            return Direction.Axis.X;
        }
        if (BlockPos.getY(start) != BlockPos.getY(end)) {
            return Direction.Axis.Y;
        }
        return Direction.Axis.Z;
    }
    
    private int getCoordinate(long position) {
        return switch (this.axis) {
            case X -> BlockPos.getX(position);
            case Y -> BlockPos.getY(position);
            case Z -> BlockPos.getZ(position);
        };
    }
    
    public boolean intersectedBy(long targetPos) {
        int targetCoord = getCoordinate(targetPos);
        int firstCoord = getCoordinate(this.firstPos);
        int secondCoord = getCoordinate(this.secondPos);
        
        return targetCoord >= firstCoord && targetCoord <= secondCoord;
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
        return switch (this.axis) {
            case X -> BlockPos.getY(position) == BlockPos.getY(this.firstPos) &&
                      BlockPos.getZ(position) == BlockPos.getZ(this.firstPos);
            case Y -> BlockPos.getX(position) == BlockPos.getX(this.firstPos) &&
                      BlockPos.getZ(position) == BlockPos.getZ(this.firstPos);
            case Z -> BlockPos.getY(position) == BlockPos.getY(this.firstPos) &&
                      BlockPos.getX(position) == BlockPos.getX(this.firstPos);
        };
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
        long x1 = BlockPos.getX(firstPos);
        long y1 = BlockPos.getY(firstPos);
        long z1 = BlockPos.getZ(firstPos);
        long y2 = BlockPos.getY(secondPos);
        long z2 = BlockPos.getZ(secondPos);
        long x2 = BlockPos.getX(secondPos);
        
        return distanceSqr(x1, y1, z1, x2, y2, z2);
    }
    
    public static long distanceSqr(
            long x1,
            long y1,
            long z1,
            long x2,
            long y2,
            long z2
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
