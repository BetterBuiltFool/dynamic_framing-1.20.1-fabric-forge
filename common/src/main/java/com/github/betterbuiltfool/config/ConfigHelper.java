package com.github.betterbuiltfool.config;

import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
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
    
    public void addColor(
            Component text,
            Color current,
            int defaultColor,
            Consumer<Color> saveConsumer
    ) {
        int cleanCurrentColor = current.getRGB() & 0xFFFFFF;
        int cleanDefaultColor = defaultColor & 0xFFFFFF;
        
        category.addEntry(builder.startColorField(text, cleanCurrentColor)
                                 .setDefaultValue(cleanDefaultColor)
                                 .setSaveConsumer(intColor -> saveConsumer.accept(new Color(intColor, false)))
                                 .build()
        );
    }
    
    public void addStringList(
            Component text,
            List<String> currentList,
            List<String> defaultList,
            Consumer<List<String>> saveConsumer
    ) {
        List<String> mutableCurrent = new ArrayList<>(currentList);
        category.addEntry(builder.startStrList(text, mutableCurrent)
                                  .setDefaultValue(defaultList)
                                  .setSaveConsumer(saveConsumer)
                                  .build()
        );
    }
}
