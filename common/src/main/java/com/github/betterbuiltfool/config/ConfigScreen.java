package com.github.betterbuiltfool.config;

import com.github.betterbuiltfool.init.ModTexts;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;

public class ConfigScreen {
    
    public static ConfigBuilder createGui(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                                             .setParentScreen(parent)
                                             .setTitle(
                                                     ModTexts.CONFIG_TITLE
                                             );
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        ConfigData defaults = new ConfigData();
        
        createNetworkViewCategory(builder, entryBuilder, defaults);
        
        createBlockValidationCategory(builder, entryBuilder, defaults);
        
        createStructureCategory(builder, entryBuilder, defaults);
        
        builder.setSavingRunnable(ConfigManager::save);
        
        return builder;
    }
    
    private static void createStructureCategory(
            ConfigBuilder builder,
            ConfigEntryBuilder entryBuilder,
            ConfigData defaults
    ) {
        ConfigCategory structure = builder.getOrCreateCategory(
                ModTexts.CONFIG_CATEGORY_STRUCTURE
        );
        
        ConfigHelper structureHelper = new ConfigHelper(structure, entryBuilder);
        structureHelper.addStringList(ModTexts.CONFIG_OPTION_STRUCTURE_MATERIAL_WHITELIST,
                                      CommonConfig.structureMaterialWhitelist.tagStrings(),
                                      defaults.structureMaterialWhitelist(),
                                      val ->
                                              CommonConfig.structureMaterialWhitelist = new CommonConfig.TagList<>(
                                                      val,
                                                      Registries.ITEM
                                              )
        );
    }
    
    private static void createBlockValidationCategory(ConfigBuilder builder,
                                  ConfigEntryBuilder entryBuilder,
                                  ConfigData defaults
    ) {
        ConfigCategory blockValidation = builder.getOrCreateCategory(
                ModTexts.CONFIG_CATEGORY_BLOCK_VALIDATION
        );
        
        ConfigHelper blockValidationHelper = new ConfigHelper(blockValidation, entryBuilder);
        blockValidationHelper.addStringList(
                ModTexts.CONFIG_OPTION_BLOCK_REPLACEMENT_WHITELIST,
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
                ModTexts.CONFIG_CATEGORY_NETWORK_VIEW
        );
        
        ConfigHelper networkViewHelper = new ConfigHelper(networkView, entryBuilder);
        
        networkViewHelper.addColor(ModTexts.CONFIG_OPTION_STANDARD_EDGE_COLOR, CommonConfig.lineColor, defaults.lineColor(), color -> CommonConfig.lineColor = color);
        networkViewHelper.addColor(ModTexts.CONFIG_OPTION_INVALID_EDGE_COLOR, CommonConfig.invalidEdgeColor, defaults.invalidEdgeColor(), color -> CommonConfig.invalidEdgeColor = color);
        networkViewHelper.addColor(ModTexts.CONFIG_OPTION_VALID_EDGE_COLOR, CommonConfig.validEdgeColor, defaults.validEdgeColor(), color -> CommonConfig.validEdgeColor = color);
        networkViewHelper.addColor(ModTexts.CONFIG_OPTION_SELECTION_COLOR, CommonConfig.selectionColor, defaults.selectionColor(), color -> CommonConfig.selectionColor = color);
        networkViewHelper.addColor(ModTexts.CONFIG_OPTION_REMOVE_SELECTION_COLOR, CommonConfig.removeSelectionColor, defaults.removeSelectionColor(),
                                   color -> CommonConfig.removeSelectionColor = color
        );
    }
}
