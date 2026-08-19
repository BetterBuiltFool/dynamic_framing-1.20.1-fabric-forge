package com.github.betterbuiltfool.forge.client;

import com.github.betterbuiltfool.DynamicFraming;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = DynamicFraming.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DynamicFramingClientForge {
    public static final Logger LOGGER = LoggerFactory.getLogger(DynamicFraming.MOD_ID + "_forge_client");
    
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void registerGeometryLoader(ModelEvent.RegisterGeometryLoaders event) {
        LOGGER.info("Registering Framing member loader");
        event.register("framing_member_loader", ForgeFrameGeometryLoader.INSTANCE);
    }
}
