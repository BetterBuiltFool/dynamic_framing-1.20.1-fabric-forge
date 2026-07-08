package com.github.betterbuiltfool.network;

import com.github.betterbuiltfool.DynamicFraming;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.resources.ResourceLocation;

public class DynamicFramingNetworking {
    
    private static final ResourceLocation CHANNEL_ID = new ResourceLocation(DynamicFraming.MOD_ID, "main");
    public static final NetworkChannel CHANNEL = NetworkChannel.create(CHANNEL_ID);
    
    public static void init() {
        // Format: CHANNEL.register(CLASSNAME.class, CLASSNAME::encode, CLASSNAME::decode, CLASSNAME::handle);
        CHANNEL.register(ChunkNodeRequestPacket.class, ChunkNodeRequestPacket::encode, ChunkNodeRequestPacket::decode, ChunkNodeRequestPacket::handle);
        CHANNEL.register(ChunkNodeDataPacket.class, ChunkNodeDataPacket::encode, ChunkNodeDataPacket::decode, ChunkNodeDataPacket::handle);
    }
}
