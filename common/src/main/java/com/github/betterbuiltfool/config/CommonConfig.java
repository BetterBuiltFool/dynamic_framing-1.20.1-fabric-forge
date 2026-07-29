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
import net.minecraft.world.item.Item;
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
    public static TagList<Item> structureMaterialWhitelist;
    
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
        structureMaterialWhitelist = new TagList<>(data.structureMaterialWhitelist(), Registries.ITEM);
    }
    
    public static ConfigData pack() {
        return new ConfigData(
                lineColor.getRGB(),
                invalidEdgeColor.getRGB(),
                validEdgeColor.getRGB(),
                selectionColor.getRGB(),
                removeSelectionColor.getRGB(),
                blockReplaceWhitelist.tagStrings(),
                structureMaterialWhitelist.tagStrings()
        );
    }
    
    public record TagList<T> (List<String> tagStrings, List<TagKey<T>> tags) {
        
        public TagList (List<String> strings, ResourceKey<? extends Registry<T>> registryKey) {
            this(
                List.copyOf(strings),
                List.copyOf(generateTags(strings, registryKey))
            );
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
