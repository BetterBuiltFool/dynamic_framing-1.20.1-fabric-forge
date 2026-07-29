package com.github.betterbuiltfool.validation;

import com.github.betterbuiltfool.config.CommonConfig;
import com.github.betterbuiltfool.config.ConfigData;
import com.github.betterbuiltfool.config.ConfigHelper;
import com.github.betterbuiltfool.config.ConfigManager;
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
        ConfigCategory networkView = builder.getOrCreateCategory(
                Component.translatable("config.category.network_view")
        );
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        ConfigData defaults = new ConfigData();
        
        ConfigHelper networkViewHelper = new ConfigHelper(networkView, entryBuilder);
        networkViewHelper.addColor("standard_edge_color", CommonConfig.lineColor, defaults.lineColor(), color -> CommonConfig.lineColor = color);
        networkViewHelper.addColor("invalid_edge_color", CommonConfig.invalidEdgeColor, defaults.invalidEdgeColor(), color -> CommonConfig.invalidEdgeColor = color);
        networkViewHelper.addColor("valid_edge_color", CommonConfig.validEdgeColor, defaults.validEdgeColor(), color -> CommonConfig.validEdgeColor = color);
        networkViewHelper.addColor("selection_color", CommonConfig.selectionColor, defaults.selectionColor(), color -> CommonConfig.selectionColor = color);
        networkViewHelper.addColor("remove_selection_color", CommonConfig.removeSelectionColor, defaults.removeSelectionColor(),
                                   color -> CommonConfig.removeSelectionColor = color
        );
        
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
        
        builder.setSavingRunnable(ConfigManager::save);
        
        return builder;
    }
}
