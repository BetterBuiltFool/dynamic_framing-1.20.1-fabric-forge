package com.github.betterbuiltfool.network;

import com.github.betterbuiltfool.client.ClientLocalNodes;
import com.github.betterbuiltfool.data.NodeMap;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import org.jetbrains.annotations.NotNull;

public class ClientPacketReceiver {
    
    public static void handleNodeDataSync(@NotNull NodeMap nodeMap) {
        EnvExecutor.runInEnv(Env.CLIENT, () -> (Runnable) () -> {
            ClientLocalNodes.setNodeMap(nodeMap);
        });
    }
}
