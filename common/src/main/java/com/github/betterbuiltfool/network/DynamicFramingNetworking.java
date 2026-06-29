package com.github.betterbuiltfool.network;

import com.github.betterbuiltfool.DynamicFraming;
import dev.architectury.networking.NetworkChannel;
import net.minecraft.resources.ResourceLocation;

public class DynamicFramingNetworking {
    
    private static final ResourceLocation CHANNEL_ID = new ResourceLocation(DynamicFraming.MOD_ID, "main");
    private static final NetworkChannel CHANNEL = NetworkChannel.create(CHANNEL_ID);
    
    public static void init() {
        // TODO: Initialize the network sync classes
        // Format: CHANNEL.register(CLASSNAME.class, CLASSNAME::encode, CLASSNAME::new, CLASSNAME::handle);
    }
}
