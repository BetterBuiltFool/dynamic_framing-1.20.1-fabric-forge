package com.github.betterbuiltfool.structure;

import com.github.betterbuiltfool.data.FramedStructureStorage;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RaycastService {
    private static final double NODE_THICKNESS = 0.3;
    private static final double EDGE_THICKNESS = 0.25;
    
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
    
    @Nullable
    public static GraphHit.NodeHit getClosestNode(
            Vec3 origin,
            Vec3 direction,
            double reach,
            NodeMap nodeMap
    ) {
        long closestPos = -1;
        double closestDist = Double.MAX_VALUE;
        
        Vec3 rayEnd = origin.add(direction.scale(reach));
        
        for (var nodePos : nodeMap.nodes()) {
            Vec3 center = Vec3.atCenterOf(BlockPos.of(nodePos));
            var hitResult = getBoundingBoxHitPoint(origin, rayEnd, center, center, NODE_THICKNESS);
            
            if (hitResult.isEmpty()) {
                continue;
            }
            
            var dist = origin.distanceToSqr(hitResult.get());
            
            if (dist >= closestDist) {
                continue;
            }
            
            closestPos = nodePos;
            closestDist = dist;
        }
        
        if (closestPos == -1) {
            return null;
        } else {
            return new GraphHit.NodeHit(closestPos, closestDist);
        }
    }
    
    @Nullable
    public static GraphHit.EdgeHit getClosestEdge(
            Vec3 origin,
            Vec3 direction,
            double reach,
            NodeMap nodeMap
    ) {
        long closestStart = -1;
        long closestEnd = -1;
        double closestDist = Double.MAX_VALUE;
        
        Vec3 rayEnd = origin.add(direction.scale(reach));
        
        for (var entry : nodeMap.entrySet()) {
            long startPos = entry.getLongKey();
            Vec3 startCenter = Vec3.atCenterOf(BlockPos.of(startPos));
            
            for (long endPos : entry.getValue()) {
                Vec3 endCenter = Vec3.atCenterOf(BlockPos.of(endPos));
                
                var hitResult = getBoundingBoxHitPoint(origin, rayEnd, startCenter, endCenter, EDGE_THICKNESS);
                
                if (hitResult.isEmpty()) {
                    continue;
                }
                
                var dist = origin.distanceToSqr(hitResult.get());
                
                if (dist >= closestDist) {
                    continue;
                }
                
                closestStart = startPos;
                closestEnd = endPos;
                closestDist = dist;
            }
        }
        
        if (closestStart == -1) {
            return null;
        } else {
            return new GraphHit.EdgeHit(closestStart, closestEnd, closestDist);
        }
    }
    
    private static double calcReach(Player player) {
        return player.isCreative() ? 5 : 4.5;
    }
    
    @Nullable
    public static GraphHit getClosest(
            Player player
    ) {
        Vec3 origin = player.getEyePosition(1.0f);
        Vec3 direction = player.getViewVector(1.0f)
                               .normalize();
        
        Level level = player.level();
        
        var graph = FramedStructureStorage.get(level)
                                          .getOrCreateDimensionGraph(level.dimension());
        
        ChunkPos[] chunks =
                FramedStructureStorage.getSurroundingChunks(new ChunkPos(BlockPos.containing(player.position())));
        
        LongSet packedPos = graph.getPackedNodesForChunk(chunks);
        
        NodeMap nodeMap = graph.getNodeMap(packedPos);
        
        GraphHit.NodeHit nodeTarget = getClosestNode(origin, direction, calcReach(player), nodeMap);
        GraphHit.EdgeHit edgeTarget = getClosestEdge(origin, direction, calcReach(player), nodeMap);
        
        if (nodeTarget == null && edgeTarget == null) {
            return null;
        }
        if (nodeTarget == null) {
            return edgeTarget;
        }
        if (edgeTarget == null) {
            return nodeTarget;
        }
        
        return nodeTarget.distance() <= edgeTarget.distance() ? nodeTarget : edgeTarget;
    }
    
    
}
