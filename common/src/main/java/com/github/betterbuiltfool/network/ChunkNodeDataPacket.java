package com.github.betterbuiltfool.network;

import com.github.betterbuiltfool.data.NodeMap;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Supplier;

public class ChunkNodeDataPacket {
    private final NodeMap nodeMap;
    
    public ChunkNodeDataPacket(NodeMap nodeMap) {
        this.nodeMap = nodeMap;
    }
    
    public static ChunkNodeDataPacket decode(FriendlyByteBuf buffer) {
        var nodeMap = NodeMap.decode(buffer);
        return new ChunkNodeDataPacket(nodeMap);
    }
    
    public void encode(FriendlyByteBuf buffer) {
        this.nodeMap.encode(buffer);
    }
    
    public void handle(Supplier<NetworkManager.PacketContext> contextSupplier) {
        var context = contextSupplier.get();
        context.queue(() -> {
            ClientPacketReceiver.handleNodeDataSync(this.nodeMap);
        });
        
    }
}
