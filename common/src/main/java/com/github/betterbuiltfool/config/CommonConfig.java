package com.github.betterbuiltfool.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.*;

public class CommonConfig {
    public static final Color SKY_BLUE = new Color(0, 192, 255);
    public static final Color DARK_ORANGE = new Color(255, 127, 0);
    public static Color lineColor = new Color(0, 0, 255);
    public static Color invalidEdgeColor = new Color(255, 0, 0);
    public static Color validEdgeColor = new Color(0, 255, 0);
    public static Color selectionColor = SKY_BLUE;
    public static Color removeSelectionColor = DARK_ORANGE;
    
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
        
        ConfigHelper helper = new ConfigHelper(networkView, entryBuilder);
        helper.addColor("standard_edge_color", lineColor, Color.BLUE, color -> lineColor = color);
        helper.addColor("invalid_edge_color", invalidEdgeColor, Color.RED, color -> invalidEdgeColor = color);
        helper.addColor("valid_edge_color", validEdgeColor, Color.GREEN, color -> validEdgeColor = color);
        helper.addColor("selection_color", selectionColor, SKY_BLUE, color -> selectionColor = color);
        helper.addColor("remove_selection_color", removeSelectionColor, DARK_ORANGE,
                        color -> removeSelectionColor = color
        );
        
        return builder;
    }
}
