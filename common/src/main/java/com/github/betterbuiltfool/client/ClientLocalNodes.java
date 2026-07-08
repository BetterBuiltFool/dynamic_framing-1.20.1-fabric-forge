package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.network.ChunkNodeRequestPacket;
import com.github.betterbuiltfool.network.DynamicFramingNetworking;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;

public class ClientLocalNodes {
    private static final Long2ObjectMap<LongSet> LOCAL_NODES = new Long2ObjectOpenHashMap<>();
    
    public static void clear() {
        LOCAL_NODES.clear();
    }
    
    public static void addNodes(Long2ObjectMap<LongSet> nodeData) {
        LOCAL_NODES.putAll(nodeData);
    }
    
    public static Long2ObjectMap<LongSet> getLocalNodes() {
        return LOCAL_NODES;
    }
    
    private static ChunkPos[] getSurroundingChunks(ChunkPos center) {
        return ChunkPos.rangeClosed(
                center,
                1
        ).toArray(ChunkPos[]::new);
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
                        getSurroundingChunks(playerChunk)
                )
        );
    }
}
