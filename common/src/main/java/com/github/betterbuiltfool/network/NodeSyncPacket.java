package com.github.betterbuiltfool.network;

import com.github.betterbuiltfool.structure.Node;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

public class NodeSyncPacket {
    private final Node networkNode;
    
    public NodeSyncPacket(Node node) {
        networkNode = node;
    }
    
    public NodeSyncPacket(FriendlyByteBuf buffer) {
        networkNode = Node.decode(buffer);
    }
    
    public void encode(FriendlyByteBuf buffer) {
        this.networkNode.encode(buffer);
    }
    
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            ClientPacketReceiver.handleNodeSync(this);
        });
    }
}
