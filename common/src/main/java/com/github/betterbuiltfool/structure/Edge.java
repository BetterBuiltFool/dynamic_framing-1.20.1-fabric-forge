package com.github.betterbuiltfool.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

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
    
    /**
     * Gets the BlockPos coordinate of the packed value along the edge's axis.
     *
     * @param position A packed long position.
     *
     * @return The appropriate coordinate along the edge's internal axis.
     */
    public int getCoordinate(long position) {
        return getCoordinate(position, this.axis);
    }
    
    /**
     * Gets the BlockPos coordinate of the packed values along the specified axis.
     * @param position A packed long position.
     * @param axis The axis we want the coordinate along.
     * @return The appropriate coordinate along the specified axis.
     */
    public static int getCoordinate(long position,
                                     Direction.Axis axis
    ) {
        return switch (axis) {
            case X -> BlockPos.getX(position);
            case Y -> BlockPos.getY(position);
            case Z -> BlockPos.getZ(position);
        };
    }
    
    /**
     * Finds the shared end point of this edge with the other. Throws if the edges do not share an end.
     * @param edge The other edge to compare with.
     * @return The packed position of the endpoint common to both edges.
     */
    public long getSharedEnd(Edge edge) {
        if (this.firstPos == edge.firstPos || this.firstPos == edge.secondPos) {
            return firstPos;
        }
        if (this.secondPos == edge.firstPos || this.secondPos == edge.secondPos) {
            return secondPos;
        }
        throw new IllegalArgumentException(this + " does not share an end with " + edge);
    }
    
    /**
     * Determines if the target position exists within the bounds of this edge.
     * @param targetPos A packed position in space.
     * @return True if the position is coaxial to the edge, else false.
     */
    public boolean containsOnAxis(long targetPos) {
        int targetCoord = getCoordinate(targetPos);
        int firstCoord = getCoordinate(this.firstPos);
        int secondCoord = getCoordinate(this.secondPos);
        
        return targetCoord >= Math.min(firstCoord, secondCoord) && targetCoord <= Math.max(firstCoord, secondCoord);
    }
    
    /**
     * Determines if the two edges intersect in a noncoaxial way.
     * @param other The other edge that may be intersecting.
     * @return True if the edges cross, otherwise false.
     */
    public boolean intersectedBy(Edge other) {
        if (this.axis == other.axis) {
            return false;
        }
        if (!isCoplanarTo(other)) {
            return false;
        }
        
        return this.containsOnAxis(other.firstPos()) && other.containsOnAxis(this.firstPos);
        
    }
    
    /**
     * Determines if the other edge exists on the same plane as this one.
     * @param other The other edge that may be coplanar.
     * @return True if the edges are on the same plane, otherwise false.
     */
    public boolean isCoplanarTo(Edge other) {
        Direction.Axis normalAxis = getNormalAxis(other);
        return getCoordinate(firstPos, normalAxis) == getCoordinate(other.firstPos(), normalAxis);
    }
    
    /**
     * Determines if this edge fully contains the other edge within its bounds
     * @param other The other edge that may be subsumed by this one.
     * @return True if the other edge is contained, otherwise false.
     */
    public boolean coaxiallyContains(Edge other) {
        
        if (this.axis != other.axis()) {
            return false;
        }
        
        Direction.Axis firstPlanarAxis, secondPlanarAxis;
        switch (this.axis) {
            case X -> {
                firstPlanarAxis = Direction.Axis.Y;
                secondPlanarAxis = Direction.Axis.Z;
            }
            case Y -> {
                firstPlanarAxis = Direction.Axis.X;
                secondPlanarAxis = Direction.Axis.Z;
            }
            case Z -> {
                firstPlanarAxis = Direction.Axis.Y;
                secondPlanarAxis = Direction.Axis.X;
            }
            default -> throw new IllegalArgumentException("Invalid axis");
        }
        if (getCoordinate(this.firstPos(), firstPlanarAxis) != getCoordinate(other.firstPos, firstPlanarAxis) ||
            getCoordinate(this.firstPos(), secondPlanarAxis) != getCoordinate(other.firstPos, secondPlanarAxis)) {
            return false;
        }
        int firstThis = getCoordinate(this.firstPos);
        int secondThis = getCoordinate(this.secondPos);
        int firstOther = other.getCoordinate(other.firstPos());
        int secondOther = other.getCoordinate(other.secondPos());
        
        return Math.min(firstThis, secondThis) < Math.min(firstOther, secondOther) &&
               Math.max(firstThis, secondThis) > Math.max(firstOther, secondOther);
    }
    
    /**
     * Calculates the intersection point between this edge and another.
     * The edges *must* intersect, or else the resulting value will not be correct.
     *
     * @param other Another, intersecting edge.
     *
     * @return A packed long of the intersection point.
     */
    public long getIntersectionPos(Edge other) {
        Direction.Axis normalAxis = getNormalAxis(other);
        
        int[] result = new int[3];
        
        result[this.axis.ordinal()] = this.getCoordinate(other.firstPos());
        result[other.axis()
                    .ordinal()] = other.getCoordinate(this.firstPos);
        result[normalAxis.ordinal()] = Edge.getCoordinate(this.firstPos, normalAxis);
        
        return BlockPos.asLong(result[0], result[1], result[2]);
    }
    
    private Direction.Axis getNormalAxis(Edge other) {
        return Direction.Axis.values()[3 - this.axis.ordinal() - other.axis.ordinal()];
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
    
    /**
     * Determines if the position is coaxial to the edge.
     * @param position A packed position which may or may not be coaxial.
     * @return True if it is. otherwise false.
     */
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
    
    /**
     * Determines the end of the edge that is closest to the supplied position.
     * @param target Packed position we are looking for.
     * @return The packed endpoint closest to the target.
     */
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
    
    /**
     * Calculates the square distance between two packed positions
     * @param firstPos The first packed position.
     * @param secondPos The second packed position.
     * @return The square distance between both positions.
     */
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
    
    /**
     * Calculates the distance between two coordinate-wise positions.
     * @param x1 First x coordinate.
     * @param y1 First y coordinate.
     * @param z1 First z coordinate.
     * @param x2 Second x coordinate.
     * @param y2 Second y coordinate.
     * @param z2 Second z coordinate.
     * @return The square distance between the two positions.
     */
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
    
    /**
     * Provides a pair of edges with the same outer endpoints as this edge, and a shared endpoint at the split pos.
     * @param splitPos The packed position where the edge should be split.
     * @return The split pair.
     */
    public Edge.Split splitAt(long splitPos) {
        long firstPos = this.firstPos;
        long secondPos = this.secondPos;
        
        return new Edge.Split(
                new Edge(firstPos, splitPos),
                new Edge(splitPos, secondPos)
        );
    }
    
    @Override
    public @NotNull String toString() {
        return String.format(
                "Edge[%s](%s <-> %s)",
                this.axis.name(),
                BlockPos.of(this.firstPos),
                BlockPos.of(this.secondPos)
        );
    }
    
    /**
     * Container for split pairs.
     * @param upper One of the new edges.
     * @param lower The other new edge.
     */
    public record Split(Edge upper, Edge lower) {}
    
}
