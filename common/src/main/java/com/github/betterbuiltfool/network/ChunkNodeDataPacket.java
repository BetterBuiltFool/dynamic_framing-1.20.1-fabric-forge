package com.github.betterbuiltfool.network;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.network.FriendlyByteBuf;

public class ChunkNodeDataPacket {
    private final LongSet nodePositions = new LongOpenHashSet();
    
    public ChunkNodeDataPacket(long... nodePos) {
        nodePositions.addAll(LongArrayList.wrap(nodePos));
    }
    
    public ChunkNodeDataPacket(LongSet nodePos) {
        this(nodePos.toLongArray());
    }
    
    public static ChunkNodeDataPacket decode(FriendlyByteBuf buffer) {
        return new ChunkNodeDataPacket(new LongOpenHashSet(buffer.readLongArray()));
    }
    
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeLongArray(nodePositions.toLongArray());
    }
}
