package com.github.betterbuiltfool.network;

import com.github.betterbuiltfool.client.ClientLocalNodes;
import com.github.betterbuiltfool.data.FramedStructureStorage;
import dev.architectury.networking.NetworkManager;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ChunkNodeDataPacket {
    private final Long2ObjectMap<LongSet> nodePositions = new Long2ObjectOpenHashMap<>();
    
//    public ChunkNodeDataPacket(long... nodePos) {
//        nodePositions.addAll(LongArrayList.wrap(nodePos));
//    }
    public ChunkNodeDataPacket(Long2ObjectMap<LongSet> nodePos) {
        this.nodePositions.putAll(nodePos);
    }
    
    public static ChunkNodeDataPacket decode(FriendlyByteBuf buffer) {
        Long2ObjectMap<LongSet> nodePos = new Long2ObjectOpenHashMap<>();
        var size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            var key = buffer.readLong();
            var values = buffer.readLongArray();
            nodePos.put(key, LongSet.of(values));
        }
        return new ChunkNodeDataPacket(nodePos);
    }
    
    public void encode(FriendlyByteBuf buffer) {
        int size = nodePositions.size();
        buffer.writeInt(size);
        for (var entry:nodePositions.long2ObjectEntrySet()) {
            var key = entry.getLongKey();
            var value = entry.getValue();
            buffer.writeLong(key);
            buffer.writeLongArray(value.toLongArray());
        }
//        buffer.writeLongArray(nodePositions.toLongArray());
    }
    
    public void handle(Supplier<NetworkManager.PacketContext> contextSupplier) {
        var context = contextSupplier.get();
        context.queue(() -> {
            ClientLocalNodes.handleNodeDataSync(this.nodePositions);
        });
    }
}
