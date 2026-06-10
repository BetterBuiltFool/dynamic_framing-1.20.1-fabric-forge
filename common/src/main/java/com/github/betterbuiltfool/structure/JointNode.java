package com.github.betterbuiltfool.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class JointNode {
    private final Set<JointNode> connections;
    private final BlockPos nodePosition;
    
    public JointNode(Vec3i position) {
        nodePosition = new BlockPos(position);
        connections = new HashSet<>();
    }
    
    public Iterator<JointNode> getConnections() {
        return this.connections.iterator();
    }
    
    public BlockPos getPosition() {
        return nodePosition;
    }
}
