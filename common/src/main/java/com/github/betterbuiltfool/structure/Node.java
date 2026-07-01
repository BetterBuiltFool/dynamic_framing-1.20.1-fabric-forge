package com.github.betterbuiltfool.structure;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class Node {
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
        Node node = new Node(nbt.getLong("pos"));
        long[] longConnections = nbt.getLongArray("connections");
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
    //endregion
    
    
    public long getPos() {
        return pos;
    }
    
    public BlockPos getBlockPos() {
        return BlockPos.of(pos);
    }
    
    //region Serialization
    public CompoundTag serialize(CompoundTag nbt) {
        nbt.putLong("nodePos", this.pos);
        nbt.putLongArray("connections", connections.toLongArray());
        
        return nbt;
    }
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeLong(this.pos);
        buffer.writeLongArray(this.connections.toLongArray());
    }
    //endregion
}
