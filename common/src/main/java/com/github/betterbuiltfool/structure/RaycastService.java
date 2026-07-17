package com.github.betterbuiltfool.structure;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class RaycastService {
    
    private static Optional<Vec3> getBoundingBoxHitPoint(
            Vec3 rayOrigin,
            Vec3 rayEnd,
            Vec3 nodeA,
            Vec3 nodeB,
            double thickness
    ) {
        AABB boundingBox = new AABB(
                Math.min(nodeA.x, nodeB.x),
                Math.min(nodeA.y, nodeB.y),
                Math.min(nodeA.z, nodeB.z),
                Math.max(nodeA.x, nodeB.x),
                Math.max(nodeA.y, nodeB.y),
                Math.max(nodeA.z, nodeB.z)
        ).inflate(thickness);
        
        return boundingBox.clip(rayOrigin, rayEnd);
    }
    
    
}
