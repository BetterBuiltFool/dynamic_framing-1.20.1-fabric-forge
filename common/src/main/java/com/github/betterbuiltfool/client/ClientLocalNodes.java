package com.github.betterbuiltfool.client;

import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;

public class ClientLocalNodes {
    private static final Long2ObjectMap<LongSet> LOCAL_NODES = new Long2ObjectOpenHashMap<>();
    
    public static void clear() {
        LOCAL_NODES.clear();
    }
    
    public static void addNodes(Long2ObjectMap<LongSet> nodeData) {
        LOCAL_NODES.putAll(nodeData);
    }
    
    public static void handleNodeDataSync(Long2ObjectMap<LongSet> nodeData) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> new Runnable() {
            @Override
            public void run() {
                clear();
                addNodes(nodeData);
            }
        });
    }
    
    public static Long2ObjectMap<LongSet> getLocalNodes() {
        return LOCAL_NODES;
    }
}
