package com.github.betterbuiltfool.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CommonConfig {
    public static Color lineColor;
    public static Color invalidEdgeColor;
    public static Color validEdgeColor;
    public static Color selectionColor;
    public static Color removeSelectionColor;
    
    public static TagList<Block> blockReplaceWhitelist;
    
    static {
        unpack(new ConfigData());
    }
    
    public static void unpack(ConfigData data) {
        lineColor = new Color(data.lineColor(), false);
        invalidEdgeColor = new Color(data.invalidEdgeColor(), false);
        validEdgeColor = new Color(data.validEdgeColor(), false);
        selectionColor = new Color(data.selectionColor(), false);
        removeSelectionColor = new Color(data.removeSelectionColor(), false);
        
        blockReplaceWhitelist = new TagList<>(data.blockReplaceWhiteList(), Registries.BLOCK);
    }
    
    public static ConfigData pack() {
        return new ConfigData(
                lineColor.getRGB(),
                invalidEdgeColor.getRGB(),
                validEdgeColor.getRGB(),
                selectionColor.getRGB(),
                removeSelectionColor.getRGB(),
                blockReplaceWhitelist.tagStrings()
        );
    }
    
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
        networkViewHelper.addColor("standard_edge_color", lineColor, defaults.lineColor(), color -> lineColor = color);
        networkViewHelper.addColor("invalid_edge_color", invalidEdgeColor, defaults.invalidEdgeColor(), color -> invalidEdgeColor = color);
        networkViewHelper.addColor("valid_edge_color", validEdgeColor, defaults.validEdgeColor(), color -> validEdgeColor = color);
        networkViewHelper.addColor("selection_color", selectionColor, defaults.selectionColor(), color -> selectionColor = color);
        networkViewHelper.addColor("remove_selection_color", removeSelectionColor, defaults.removeSelectionColor(),
                        color -> removeSelectionColor = color
        );
        
        ConfigCategory blockValidation = builder.getOrCreateCategory(
                Component.translatable("config.category.block_validation")
        );
        
        ConfigHelper blockValidationHelper = new ConfigHelper(blockValidation, entryBuilder);
        blockValidationHelper.addStringList(
                "block_replacement_whitelist",
                blockReplaceWhitelist.tagStrings(),
                defaults.blockReplaceWhiteList(),
                val -> blockReplaceWhitelist = new TagList<>(val, Registries.BLOCK)
        );
        
        builder.setSavingRunnable(ConfigManager::save);
        
        return builder;
    }
    
    public record TagList<T> (List<String> tagStrings, List<TagKey<T>> tags) {
        
        public TagList (List<String> strings, ResourceKey<? extends Registry<T>> registryKey) {
            this(strings, generateTags(strings, registryKey));
        }
        
        private static <T> List<TagKey<T>> generateTags(
                List<String> strings,
                ResourceKey<? extends Registry<T>> registryKey
        ) {
            List<TagKey<T>> compiledTags = new ArrayList<>();
            for (var string:strings) {
                if (!ResourceLocation.isValidResourceLocation(string)) continue;
                compiledTags.add(TagKey.create(registryKey, new ResourceLocation(string)));
            }
            return compiledTags;
        }
    }
}
