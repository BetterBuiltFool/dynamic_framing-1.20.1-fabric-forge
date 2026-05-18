package com.github.betterbuiltfool.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import com.github.betterbuiltfool.DynamicFraming;

@Mod(DynamicFraming.MOD_ID)
public final class DynamicFramingForge {
    public DynamicFramingForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(DynamicFraming.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        DynamicFraming.init();
    }
}
