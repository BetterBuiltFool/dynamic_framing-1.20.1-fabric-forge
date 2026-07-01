package com.github.betterbuiltfool.network;

import com.github.betterbuiltfool.client.ClientLocalNodes;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;

public class ClientPacketReceiver {
    
    public static void handleNodeDataSync(Long2ObjectMap<LongSet> nodeData) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> (Runnable) () -> {
            ClientLocalNodes.clear();
            ClientLocalNodes.addNodes(nodeData);
        });
    }
}
