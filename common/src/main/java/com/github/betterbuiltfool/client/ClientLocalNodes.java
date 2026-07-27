package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.data.FramedStructureStorage;
import com.github.betterbuiltfool.data.NodeMap;
import com.github.betterbuiltfool.network.ChunkNodeRequestPacket;
import com.github.betterbuiltfool.network.DynamicFramingNetworking;
import net.minecraft.client.Minecraft;

public class ClientLocalNodes {
    private static NodeMap LOCAL_MAP;
    
    public static void setNodeMap(NodeMap nodeMap) {
        LOCAL_MAP = nodeMap;
    }
    
    public static NodeMap getLocalNodes() {
        return LOCAL_MAP;
    }
    
    public static void requestRefresh(Minecraft client) {
        var level = client.level;
        assert level != null;
        var dimension = client.level.dimension();
        var player = client.player;
        assert player != null;
        var playerChunk = player.chunkPosition();
        
        DynamicFramingNetworking.CHANNEL.sendToServer(
                new ChunkNodeRequestPacket(
                        dimension,
                        FramedStructureStorage.getSurroundingChunks(playerChunk)
                )
        );
    }
}
