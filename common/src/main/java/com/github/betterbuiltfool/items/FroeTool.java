package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.DynamicFraming;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FroeTool extends Item {
    
    public static final String ITEM_ID = "froe";
    
    public FroeTool(Properties properties) {
        super(properties);
    }
    
}