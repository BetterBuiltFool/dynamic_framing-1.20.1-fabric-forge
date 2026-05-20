package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.DynamicFraming;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FroeTool extends Item {
    
    public static final String ITEM_ID = "froe";
    
    private static final Set<TagKey<Item>> whitelist = new HashSet<>();
    public static final String FIRST_POS_DATA = "FirstPosData";
    
    static{
        // TODO: Read this in from config file
        whitelist.add(ItemTags.LOGS);
    }
    
    public FroeTool(Properties properties) {
        super(properties);
    }
    
    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltipComponents,
            TooltipFlag isAdvanced
    ) {
        var firstPos = getFirstPos(stack);
        
        if (firstPos != null) {
            tooltipComponents.add(
                    Component.translatable(
                            "tooltip.dynamic_framing.froe.firstpos").append(firstPos.toShortString()
                    )
            );
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
    
    public boolean validateOffhand(
            ItemStack offhandItem
    ) {
        
        for (TagKey<Item> tag : whitelist) {
            if (offhandItem.is(tag)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand usedHand
    ) {
        var froeTool = player.getMainHandItem();
        
        var firstPos = getFirstPos(froeTool);
        var ray = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        var lookPos = ray.getBlockPos();
        
        if (firstPos == null) {
            setFirstPos(froeTool, lookPos);
            return InteractionResultHolder.success(froeTool);
        }
        
        var secondPos = getSecondPos(firstPos, lookPos);
        
        var offhandItem = player.getOffhandItem();
        
        if (!validateOffhand(offhandItem)) {
            DynamicFraming.LOGGER.info("Invalid offhand item {}", offhandItem.getDisplayName());
            return InteractionResultHolder.fail(froeTool);
        }
        
        int materialCost = calcEdgeLength(firstPos, secondPos);
        
        if (inventoryCount(player, offhandItem) < materialCost) {
            DynamicFraming.LOGGER.info("Not enough {} for edge length of {}", offhandItem.getDisplayName(), materialCost);
            return InteractionResultHolder.fail(froeTool);
        }
        
        tryPlaceEdge(firstPos, secondPos);
        removeMaterialCost(player, offhandItem);
        
        return InteractionResultHolder.success(froeTool);
    }
    
    private void removeMaterialCost(
            Player player,
            ItemStack offhandItem
    ) {
    
    }
    
    private void tryPlaceEdge(
            BlockPos firstPos,
            BlockPos secondPos
    ) {
    
    }
    
    private int inventoryCount(
            Player player,
            ItemStack offhandItem
    ) {
        return 0;
    }
    
    private int calcEdgeLength(
            BlockPos firstPos,
            BlockPos secondPos
    ) {
        return 0;
    }
    
    private BlockPos getSecondPos(
            BlockPos firstPos,
            BlockPos lookPos
    ) {
        return null;
    }
    
    private static @Nullable BlockPos getFirstPos(ItemStack item) {
        if (!(item.hasTag() && item.getTag().contains(FIRST_POS_DATA))) {
            return null;
        }
        CompoundTag firstPosTag = item.getTag().getCompound(FIRST_POS_DATA);
        
        return new BlockPos(
                firstPosTag.getInt("X"),
                firstPosTag.getInt("Y"),
                firstPosTag.getInt("Z")
        );
        
    }
    
    private void setFirstPos(
            ItemStack froeTool,
            BlockPos lookPos
    ) {
        CompoundTag firstPosTag = froeTool.getOrCreateTag();
        
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("X", lookPos.getX());
        posTag.putInt("Y", lookPos.getY());
        posTag.putInt("Z", lookPos.getZ());
        
        firstPosTag.put(FIRST_POS_DATA, posTag);
    }
}