package com.github.betterbuiltfool;

import com.github.betterbuiltfool.config.ConfigManager;
import com.github.betterbuiltfool.config.ConfigScreenRegistry;
import com.github.betterbuiltfool.network.DynamicFramingNetworking;
import com.github.betterbuiltfool.registry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DynamicFraming {
    public static final String MOD_ID = "dynamic_framing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public static void init() {
        // Write common init code here.
        LOGGER.info("Initializing Dynamic Framing");
        
        TabRegistry.register();
        BlockRegistry.register();
        ItemRegistry.register();
        CommandRegistry.register();
        BlockEntityRegistry.register();
        
        DynamicFramingNetworking.init();
        
        ConfigManager.load();
        ConfigScreenRegistry.register();
    }
}
