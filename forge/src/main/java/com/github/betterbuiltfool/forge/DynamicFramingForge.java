package com.github.betterbuiltfool.forge;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.client.DynamicFramingClient;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DynamicFraming.MOD_ID)
public final class DynamicFramingForge {
    public DynamicFramingForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        IEventBus modEventBus = FMLJavaModLoadingContext.get()
                                                        .getModEventBus();
        EventBuses.registerModEventBus(DynamicFraming.MOD_ID, modEventBus);

        // Run our common setup.
        DynamicFraming.init();
        
        modEventBus.addListener(this::onClientSetup);
    }
    
    private void onClientSetup(final FMLClientSetupEvent event) {
        DynamicFramingClient.init();
    }
}
