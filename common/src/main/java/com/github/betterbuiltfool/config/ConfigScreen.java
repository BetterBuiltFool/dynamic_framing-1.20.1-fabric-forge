package com.github.betterbuiltfool.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;

public class ConfigScreen {
    
    public static ConfigBuilder createGui(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                                             .setParentScreen(parent)
                                             .setTitle(
                                                     Component.translatable("config.title")
                                             );
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        ConfigData defaults = new ConfigData();
        
        createNetworkViewCategory(builder, entryBuilder, defaults);
        
        createBlockValidationCategory(builder, entryBuilder, defaults);
        
        builder.setSavingRunnable(ConfigManager::save);
        
        return builder;
    }
    
    private static void createBlockValidationCategory(ConfigBuilder builder,
                                  ConfigEntryBuilder entryBuilder,
                                  ConfigData defaults
    ) {
        ConfigCategory blockValidation = builder.getOrCreateCategory(
                Component.translatable("config.category.block_validation")
        );
        
        ConfigHelper blockValidationHelper = new ConfigHelper(blockValidation, entryBuilder);
        blockValidationHelper.addStringList(
                "block_replacement_whitelist",
                CommonConfig.blockReplaceWhitelist.tagStrings(),
                defaults.blockReplaceWhiteList(),
                val -> CommonConfig.blockReplaceWhitelist = new CommonConfig.TagList<>(val, Registries.BLOCK)
        );
    }
    
    private static void createNetworkViewCategory(ConfigBuilder builder,
                                  ConfigEntryBuilder entryBuilder,
                                  ConfigData defaults
    ) {
        ConfigCategory networkView = builder.getOrCreateCategory(
                Component.translatable("config.category.network_view")
        );
        
        ConfigHelper networkViewHelper = new ConfigHelper(networkView, entryBuilder);
        
        networkViewHelper.addColor("standard_edge_color", CommonConfig.lineColor, defaults.lineColor(), color -> CommonConfig.lineColor = color);
        networkViewHelper.addColor("invalid_edge_color", CommonConfig.invalidEdgeColor, defaults.invalidEdgeColor(), color -> CommonConfig.invalidEdgeColor = color);
        networkViewHelper.addColor("valid_edge_color", CommonConfig.validEdgeColor, defaults.validEdgeColor(), color -> CommonConfig.validEdgeColor = color);
        networkViewHelper.addColor("selection_color", CommonConfig.selectionColor, defaults.selectionColor(), color -> CommonConfig.selectionColor = color);
        networkViewHelper.addColor("remove_selection_color", CommonConfig.removeSelectionColor, defaults.removeSelectionColor(),
                                   color -> CommonConfig.removeSelectionColor = color
        );
    }
}
