package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.client.ClientLocalNodes;
import com.github.betterbuiltfool.data.FramedStructureStorage;
import com.github.betterbuiltfool.items.nbtHelper.FramingHammerData;
import com.github.betterbuiltfool.structure.GraphHit;
import com.github.betterbuiltfool.structure.RaycastService;
import com.github.betterbuiltfool.ui.overlays.FramingHammerOverlay;
import com.github.betterbuiltfool.ui.overlays.NodeOverlayContextBuilder;
import com.github.betterbuiltfool.validation.EdgeValidator;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

/**
 * A Framing tool for establishing the shape of a structure by placing nodes.
 */
public class FramingHammer extends Item implements RendersOverlay, SuppressesEquipAnimation, HasLeftClickUse {
    
    public static final String ITEM_ID = "framing_hammer";
    public static final Color INVALID_EDGE_COLOR = new Color(255, 0, 0);
    
    public FramingHammer(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useLeftClick(Level level,
                                          Player player,
                                          InteractionHand interactionHand
    ) {
        var itemStack = player.getItemInHand(interactionHand);
        
        if (!shouldBlockMining(player, itemStack)) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        var hammerTool = new FramingHammerData(itemStack);
        
        if (!level.isClientSide() && !hammerTool.hasSecondPos()) {
            var selection = hammerTool.getSelection();
            
            var storage = FramedStructureStorage.get(level);
            var graph = storage.getDimensionGraph(level.dimension());
            
            if (selection instanceof GraphHit.NodeHit nodeHit) {
                graph.remove(nodeHit.packedPos());
            } else if (selection instanceof GraphHit.EdgeHit edgeHit) {
                graph.remove(edgeHit.posA(), edgeHit.posB());
            }
            hammerTool.clearSelection();
        }
        
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public boolean shouldBlockMining(Player player,
                                     ItemStack itemStack
    ) {
        return new FramingHammerData(itemStack).hasSelection();
    }
    
    @Override
    public void inventoryTick(ItemStack stack,
                              Level level,
                              Entity entity,
                              int slotId,
                              boolean isSelected
    ) {
        if (!isSelected || !(entity instanceof Player player) || level.isClientSide()) {
            return;
        }
        
        var hammerTool = new FramingHammerData(stack);
        
        var selection = RaycastService.getClosest(player);
        hammerTool.setSelection(selection);
        
        if (!hammerTool.hasFirstPos()) {
            return;
        }
        
        long firstPos = hammerTool.getFirstPos();
        
        if (selection != null) {
            long secondPos = getSelectedPos(hammerTool);
            if (isCoaxial(firstPos, secondPos)) {
                hammerTool.setSecondPos(secondPos);
                return;
            }
        }
        var ray = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        var lookPos = ray.getBlockPos();
        long secondPos = calcSecondPos(BlockPos.of(firstPos), lookPos).asLong();
        
        hammerTool.setSecondPos(secondPos);
    }
    
    private boolean isCoaxial(long firstPos,
                              long secondPos
    ) {
        boolean xMatch = (BlockPos.getX(firstPos) == BlockPos.getX(secondPos));
        boolean yMatch = (BlockPos.getY(firstPos) == BlockPos.getY(secondPos));
        boolean zMatch = (BlockPos.getZ(firstPos) == BlockPos.getZ(secondPos));
        
        return ((xMatch && yMatch) || (xMatch && zMatch) || (yMatch && zMatch));
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand usedHand
    ) {
        var hammerTool = new FramingHammerData(player.getMainHandItem());
        
        if (player.isShiftKeyDown()) {
            hammerTool.clear();
            return InteractionResultHolder.consume(hammerTool.wrapped);
        }
        
        var ray = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        var hitPos = ray.getBlockPos();
        var lookPos = hitPos.relative(ray.getDirection());
        
        if (!hammerTool.hasFirstPos()) {
            return firstUse(hammerTool, lookPos);
        }
        
        return secondUse(level, hammerTool);
    }
    
    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltipComponents,
            TooltipFlag isAdvanced
    ) {
        var tool = new FramingHammerData(stack);
        
        addSelectionText(tooltipComponents, tool);
        addFirstPosText(tooltipComponents, tool);
        
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
    
    public boolean shouldCauseReequipAnimation(
            ItemStack oldStack,
            ItemStack newStack,
            boolean slotChanged
    ) {
        if (slotChanged) {
            return true;
        }
        
        if (this.shouldSuppressReequip(oldStack, newStack)) {
            return false;
        }
        
        return oldStack.getItem() != newStack.getItem();
    }
    
    private static void addSelectionText(List<Component> tooltipComponents,
                                         FramingHammerData tool
    ) {
        if (tool.hasSelection()) {
            tooltipComponents.add(
                    Component.translatable(
                                     "tooltip.dynamic_framing.framing_hammer.selection"
                             )
                             .append(tool.getSelection()
                                         .toString())
            );
        }
    }
    
    private static void addFirstPosText(List<Component> tooltipComponents,
                                        FramingHammerData tool
    ) {
        if (tool.hasFirstPos()) {
            tooltipComponents.add(
                    Component.translatable(
                            "tooltip.dynamic_framing.framing_hammer.firstpos"
                             )
                             .append(BlockPos.of(tool.getFirstPos())
                                             .toShortString())
            );
        }
    }
    
    private @NotNull InteractionResultHolder<ItemStack> firstUse(
            FramingHammerData hammerTool,
            BlockPos lookPos
    ) {
        long firstPos;
        if (!hammerTool.hasSelection()) {
            firstPos = lookPos.asLong();
        } else {
            firstPos = getSelectedPos(hammerTool);
        }
        hammerTool.setFirstPos(firstPos);
        return InteractionResultHolder.success(hammerTool.wrapped);
    }
    
    private static long getSelectedPos(FramingHammerData hammerTool) {
        var selection = hammerTool.getSelection();
        if (selection instanceof GraphHit.NodeHit nodeHit) {
            return nodeHit.packedPos();
        } else if (selection instanceof GraphHit.EdgeHit edgeHit) {
            // TODO: temp, make EdgeHit contain collision position
            return edgeHit.hitPos();
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    private @NotNull InteractionResultHolder<ItemStack> secondUse(
            Level level,
            FramingHammerData hammerTool
    ) {
        if (!hammerTool.hasSecondPos()) {
            DynamicFraming.LOGGER.info("No second node detected. This shouldn't be happening.");
            hammerTool.clear();
            return InteractionResultHolder.consume(hammerTool.wrapped);
        }
        var firstPos = hammerTool.getFirstPos();
        var secondPos = hammerTool.getSecondPos();
        
        if (!level.isClientSide) {
            var storage = FramedStructureStorage.get(level);
            var structureGraph = FramedStructureStorage.getOrCreateDimensionGraph(level);
            
            structureGraph.connect(firstPos, secondPos);
            storage.setDirty();
        }
        hammerTool.clear();
        return InteractionResultHolder.success(hammerTool.wrapped);
    }
    
    /**
     * Calculates the second position of an edge based on where the player is looking.
     *
     * @param firstPos The starting position of the edge
     * @param lookPos The position at which the player is looking
     * @return A BlockPos that is coaxial to the firstPos
     */
    private @NotNull BlockPos calcSecondPos(
            @NotNull BlockPos firstPos,
            @NotNull BlockPos lookPos
    ) {
        
        var delta = lookPos.subtract(firstPos);
        var direction = Direction.getNearest(delta.getX(), delta.getY(), delta.getZ());
        var axis = direction.getAxis();
        
        return firstPos.relative(axis, delta.get(axis));
    }
    
    @Override
    public void renderOverlay(Minecraft client,
                              PoseStack poseStack,
                              @NotNull ItemStack itemStack
    ) {
        ClientLocalNodes.requestRefresh(client);
        var hammerTool = new FramingHammerData(itemStack);
        
        var highlightNodes = new LongOpenHashSet();
        var contextBuilder =
                new NodeOverlayContextBuilder(client, poseStack).addNodeMap(ClientLocalNodes.getLocalNodes())
                                                                .addHighlightPos(highlightNodes);
        // TODO: This is a mess, clean it up.
        if (hammerTool.hasFirstPos() && hammerTool.hasSecondPos()) {
            var firstPos = hammerTool.getFirstPos();
            var secondPos = hammerTool.getSecondPos();
            if (!EdgeValidator.validate(client.level, firstPos, secondPos)) {
                contextBuilder = contextBuilder.setHighlightColor(INVALID_EDGE_COLOR);
            }
            contextBuilder = contextBuilder.addHighlightPos(firstPos)
                                           .addFirstPos(firstPos)
                                           .addHighlightPos(secondPos)
                                           .addSecondPos(secondPos);
        } else if (hammerTool.hasSelection()) {
            var player = client.player;
            assert player != null;
            if (!player.isShiftKeyDown()) {
                contextBuilder = contextBuilder.setHighlightColor(new Color(0, 127, 255));
            } else {
                contextBuilder = contextBuilder.setHighlightColor(new Color(255, 127, 0));
            }
            var selection = hammerTool.getSelection();
            if (selection instanceof GraphHit.NodeHit nodeHit) {
                contextBuilder = contextBuilder.addHighlightPos(nodeHit.packedPos());
            } else if (selection instanceof GraphHit.EdgeHit edgeHit) {
                contextBuilder = contextBuilder.addFirstPos(edgeHit.posA())
                                               .addSecondPos(edgeHit.posB())
                                               .addHighlightPos(edgeHit.hitPos());
            }
        }
        FramingHammerOverlay.renderOverlay(contextBuilder.build());
    }
}