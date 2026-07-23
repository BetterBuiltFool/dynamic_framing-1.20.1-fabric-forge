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
