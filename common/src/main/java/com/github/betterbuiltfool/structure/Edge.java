package com.github.betterbuiltfool.structure;

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
    
    public Edge.Split splitAt(long splitPos) {
        long firstPos = this.firstPos;
        long secondPos = this.secondPos;
        
        return new Edge.Split(
                new Edge(firstPos, splitPos),
                new Edge(splitPos, secondPos)
        );
    }
    
    public record Split(Edge first, Edge second) {}
}
