package com.github.betterbuiltfool.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CoaxSelection {
    
    public static long getCoaxialPoint(
            Vec3 rayOrigin,
            Vec3 rayDirection,
            BlockPos blockPos
    ) {
        Vec3 planeOrigin = blockPos.getCenter();
        
        Vec3 planeNormal = getPlaneNormal(rayDirection);
        
        double projection = rayDirection.dot(planeNormal);
        
        if (Math.abs(projection) < 1e-6) {
            return blockPos.asLong();
        }
        
        Vec3 intersection = getIntersection(planeOrigin, planeNormal, rayOrigin, rayDirection, projection);
        
        Vec3 relativeVector = intersection.subtract(planeOrigin);
        
        Vec3 filteredVec = relativeVector.multiply(new Vec3(1, 1, 1).subtract(planeNormal));
        
        Direction targetDirection = Direction.getNearest(filteredVec.x, filteredVec.y, filteredVec.z);
        Direction.Axis targetAxis = targetDirection.getAxis();
        
        return BlockPos.asLong(
                (targetAxis == Direction.Axis.X) ? Mth.floor(intersection.x) : blockPos.getX(),
                (targetAxis == Direction.Axis.Y) ? Mth.floor(intersection.y) : blockPos.getY(),
                (targetAxis == Direction.Axis.Z) ? Mth.floor(intersection.z) : blockPos.getZ()
        );
    }
    
    public static boolean isCoaxial(
            long firstPos,
            long secondPos
    ) {
        int x = BlockPos.getX(secondPos);
        int y = BlockPos.getY(secondPos);
        int z = BlockPos.getZ(secondPos);
        return isCoaxial(firstPos, x, y, z);
    }
    
    public static boolean isCoaxial(
            long position,
            int x,
            int y,
            int z
    ) {
        boolean xMatch = BlockPos.getX(position) == x;
        boolean yMatch = BlockPos.getY(position) == y;
        boolean zMatch = BlockPos.getZ(position) == z;
        
        return ((xMatch && yMatch) || (xMatch && zMatch) || (yMatch && zMatch));
    }
    
    public static boolean isCoplanar(
            long firstPos,
            long secondPos,
            Direction.Axis normal
    ) {
        return getCoordinate(firstPos, normal) == getCoordinate(secondPos, normal);
    }
    
    /**
     * Gets the BlockPos coordinate of the packed values along the specified axis.
     *
     * @param position A packed long position.
     * @param axis     The axis we want the coordinate along.
     *
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
    
    private static Vec3 getIntersection(
            Vec3 planeOrigin,
            Vec3 planeNormal,
            Vec3 rayOrigin,
            Vec3 rayDirection,
            double projection
    ) {
        var scalingFactor = planeOrigin.subtract(rayOrigin)
                                       .dot(planeNormal) / projection;
        return rayOrigin.add(rayDirection.scale(scalingFactor));
    }
    
    private static Vec3 getPlaneNormal(Vec3 rayDirection) {
        Direction dominant = Direction.getNearest(rayDirection.x, rayDirection.y, rayDirection.z);
        
        Vec3 normal = Vec3.atLowerCornerOf(dominant.getNormal());
        
        return normal.multiply(normal);
    }
    
    
}
