package com.github.betterbuiltfool.network;

import com.github.betterbuiltfool.client.ClientLocalNodes;
import com.github.betterbuiltfool.data.FramedStructureStorage;
import dev.architectury.networking.NetworkManager;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

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
    
    public void handle(Supplier<NetworkManager.PacketContext> contextSupplier) {
        var context = contextSupplier.get();
        context.queue(() -> {
            // Do nothing yet
//            ClientLocalNodes.clear();
//            ClientLocalNodes.addNodes(this.nodePositions);
        });
    }
}
