package com.github.betterbuiltfool.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashSet;
import java.util.Set;

public class Node {
    public static final String NODE_POS_LABEL = "nodePos";
    public static final String CONNECTIONS_LABEL = "connections";
    private final long pos;
    private final LongSet connections = new LongOpenHashSet();
    
    //region Constructors
    public Node(long nodePos) {
        this.pos = nodePos;
    }
    
    public Node(BlockPos blockPos) {
        this.pos = blockPos.asLong();
    }
    
    public static Node deserialize(CompoundTag nbt) {
        Node node = new Node(nbt.getLong(NODE_POS_LABEL));
        long[] longConnections = nbt.getLongArray(CONNECTIONS_LABEL);
        for (long connection:longConnections) {
            node.connect(connection);
        }
        return node;
    }
    
    public static Node decode(FriendlyByteBuf buffer) {
        Node node = new Node(buffer.readLong());
        for (long connection: buffer.readLongArray()) {
            node.connect(connection);
        }
        return node;
    }
    //endregion
    
    //region Connection
    public void connect(long connectedPos) {
        this.connections.add(connectedPos);
    }
    
    public void connect(Node other) {
        if (other == null || other == this) return;
        this.connect(other.pos);
        other.connect(this.pos);
    }
    
    public LongSet getConnections() {
        return this.connections;
    }
    
    public Set<Edge> getEdges() {
        var edges = new HashSet<Edge>();
        for (var connection : connections) {
            edges.add(new Edge(this.pos, connection));
        }
        return edges;
    }
    //endregion
    
    
    public long getPos() {
        return pos;
    }
    
    public BlockPos getBlockPos() {
        return BlockPos.of(pos);
    }
    
    //region Serialization
    public CompoundTag serialize(CompoundTag nbt) {
        nbt.putLong(NODE_POS_LABEL, this.pos);
        nbt.putLongArray(CONNECTIONS_LABEL, connections.toLongArray());
        
        return nbt;
    }
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeLong(this.pos);
        buffer.writeLongArray(this.connections.toLongArray());
    }
    //endregion
}
