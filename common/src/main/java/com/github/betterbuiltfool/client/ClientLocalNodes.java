package com.github.betterbuiltfool.client;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;

public class ClientLocalNodes {
    private static final Long2ObjectMap<LongSet> LOCAL_NODES = new Long2ObjectOpenHashMap<>();
    
    public static void clear() {
        LOCAL_NODES.clear();
    }
    
    public static void updateNode(long position, LongSet connections) {
        LOCAL_NODES.put(position, connections);
    }
    
    public static Long2ObjectMap<LongSet> getLocalNodes() {
        return LOCAL_NODES;
    }
}
