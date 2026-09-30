package com.github.betterbuiltfool.config;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CommonConfig {
    public static Color lineColor;
    public static Color invalidEdgeColor;
    public static Color validEdgeColor;
    public static Color selectionColor;
    public static Color removeSelectionColor;
    
    public static TagList<Block> blockReplaceWhitelist;
    
    public static List<String> structureMaterialBlacklistStrings;
    public final static List<Predicate<ItemStack>> structureMaterialBlacklist = new ArrayList<>();
    
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
        structureMaterialBlacklistStrings = data.structureMaterialBlacklist();
        parseStructureMaterialBlacklist(structureMaterialBlacklist, structureMaterialBlacklistStrings);
    }
    
    public static ConfigData pack() {
        return new ConfigData(
                lineColor.getRGB(),
                invalidEdgeColor.getRGB(),
                validEdgeColor.getRGB(),
                selectionColor.getRGB(),
                removeSelectionColor.getRGB(),
                blockReplaceWhitelist.tagStrings(),
                structureMaterialBlacklistStrings
        );
    }
    
    public static void parseStructureMaterialBlacklist(List<Predicate<ItemStack>> blacklist,
                                                       List<String> entries
    ) {
        blacklist.clear();
        
        for (var entry : entries) {
            String value = entry.trim();
            
            if (value.isEmpty()) {
                continue;
            }
            
            boolean isExplicitTag = entry.startsWith("#");
            
            if (isExplicitTag) {
                entry = entry.substring(1);
            }
            
            ResourceLocation id = ResourceLocation.tryParse(entry);
            
            if (id == null) {
                continue;
            }
            
            if (isExplicitTag) {
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, id);
                blacklist.add(stack -> stack.is(tagKey));
            } else if (BuiltInRegistries.ITEM.containsKey(id)) {
                Item item = BuiltInRegistries.ITEM.get(id);
                blacklist.add(stack -> stack.is(item));
            } else {
                // Implicit tag
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, id);
                blacklist.add(stack -> stack.is(tagKey));
            }
        }
        
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
