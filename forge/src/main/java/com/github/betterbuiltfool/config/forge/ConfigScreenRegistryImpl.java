package com.github.betterbuiltfool.config.forge;

import com.github.betterbuiltfool.config.ConfigScreen;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.function.BiFunction;

public class ConfigScreenRegistryImpl {
    
    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(new BiFunction<Minecraft, Screen, Screen>() {
                    @Override
                    public Screen apply(Minecraft minecraft,
                                        Screen screen
                    ) {
                        ConfigBuilder builder = ConfigScreen.createGui(screen);
                        
                        return (Screen) builder.build();
                    }
                }
                )
        );
    }
}
