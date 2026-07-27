package com.github.betterbuiltfool.config;

import com.github.betterbuiltfool.DynamicFraming;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.function.Consumer;

public class ConfigHelper {
    
    private final ConfigCategory category;
    private final ConfigEntryBuilder builder;
    
    public ConfigHelper(ConfigCategory category,
                        ConfigEntryBuilder builder
    ) {
        this.category = category;
        this.builder = builder;
    }
    
    private Component getText(String key) {
        return Component.translatable("option." + DynamicFraming.MOD_ID + "." + key);
    }
    
    public void addColor(
            String key,
            Color current,
            Color defaultColor,
            Consumer<Color> saveConsumer
    ) {
        category.addEntry(builder.startColorField(getText(key), current.getRGB())
                                 .setDefaultValue(defaultColor.getRGB())
                                 .setSaveConsumer(intColor -> saveConsumer.accept(new Color(intColor, true)))
                                 .build()
        );
    }
}
