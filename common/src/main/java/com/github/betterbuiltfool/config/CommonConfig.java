package com.github.betterbuiltfool.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class CommonConfig {
    public static Color lineColor;
    public static Color invalidEdgeColor;
    public static Color validEdgeColor;
    public static Color selectionColor;
    public static Color removeSelectionColor;
    
    static {
        unpack(new ConfigData());
    }
    
    public static void unpack(ConfigData data) {
        lineColor = new Color(data.lineColor(), false);
        invalidEdgeColor = new Color(data.invalidEdgeColor(), false);
        validEdgeColor = new Color(data.validEdgeColor(), false);
        selectionColor = new Color(data.selectionColor(), false);
        removeSelectionColor = new Color(data.removeSelectionColor(), false);
    }
    
    public static ConfigData pack() {
        return new ConfigData(
                lineColor,
                invalidEdgeColor,
                validEdgeColor,
                selectionColor,
                removeSelectionColor
        );
    }
    
    public static ConfigBuilder createGui(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                                             .setParentScreen(parent)
                                             .setTitle(
                                                     Component.translatable("config.title")
                                             );
        ConfigCategory networkView = builder.getOrCreateCategory(
                Component.translatable("config.network_view_category")
        );
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        ConfigData defaults = new ConfigData();
        
        ConfigHelper helper = new ConfigHelper(networkView, entryBuilder);
        helper.addColor("standard_edge_color", lineColor, defaults.lineColor(), color -> lineColor = color);
        helper.addColor("invalid_edge_color", invalidEdgeColor, defaults.invalidEdgeColor(), color -> invalidEdgeColor = color);
        helper.addColor("valid_edge_color", validEdgeColor, defaults.validEdgeColor(), color -> validEdgeColor = color);
        helper.addColor("selection_color", selectionColor, defaults.selectionColor(), color -> selectionColor = color);
        helper.addColor("remove_selection_color", removeSelectionColor, defaults.removeSelectionColor(),
                        color -> removeSelectionColor = color
        );
        
        builder.setSavingRunnable(ConfigManager::save);
        
        return builder;
    }
}
