package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.client.ClientLocalNodes;
import com.github.betterbuiltfool.config.CommonConfig;
import com.github.betterbuiltfool.data.CoaxSelection;
import com.github.betterbuiltfool.data.FramedStructureStorage;
import com.github.betterbuiltfool.data.RaycastService;
import com.github.betterbuiltfool.init.ModTexts;
import com.github.betterbuiltfool.items.nbtHelper.FramingHammerData;
import com.github.betterbuiltfool.structure.GraphHit;
import com.github.betterbuiltfool.ui.overlays.FramingHammerOverlay;
import com.github.betterbuiltfool.ui.overlays.NodeOverlayContextBuilder;
import com.github.betterbuiltfool.validation.EdgeValidator;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
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

import java.util.List;

/**
 * A Framing tool for establishing the shape of a structure by placing nodes.
 */
public class FramingHammer extends Item implements RendersOverlay, SuppressesEquipAnimation, HasLeftClickUse {
    
    public static final String ITEM_ID = "framing_hammer";
    
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
            storage.setDirty();
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
            if (CoaxSelection.isCoaxial(firstPos, secondPos)) {
                hammerTool.setSecondPos(secondPos);
                return;
            }
        }
        long secondPos = CoaxSelection.getCoaxialPoint(
                player.getEyePosition(1.0f),
                player.getViewVector(1.0f),
                BlockPos.of(firstPos)
        );
        
        hammerTool.setSecondPos(secondPos);
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand usedHand
    ) {
        if (usedHand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(player.getMainHandItem());
        }
        var hammerTool = new FramingHammerData(player.getMainHandItem());
        
        if (player.isShiftKeyDown()) {
            hammerTool.clear();
            return InteractionResultHolder.consume(hammerTool.wrapped);
        }
        
        var ray = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        BlockPos lookPos;
        var hitPos = ray.getBlockPos();
        if (level.getBlockState(hitPos)
                 .isAir()) {
            lookPos = hitPos;
        } else {
            lookPos = hitPos.relative(ray.getDirection());
        }
        
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
                            ModTexts.TOOLTIP_FRAMING_HAMMER_SELECTION.getString(),
                            tool.getSelection()
                    )
            );
        }
    }
    
    private static void addFirstPosText(List<Component> tooltipComponents,
                                        FramingHammerData tool
    ) {
        if (tool.hasFirstPos()) {
            tooltipComponents.add(
                    Component.translatable(
                            ModTexts.TOOLTIP_FRAMING_HAMMER_FIRST_POS.getString(),
                            BlockPos.of(tool.getFirstPos())
                                    .toShortString()
                    )
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
                contextBuilder = contextBuilder.setHighlightColor(CommonConfig.invalidEdgeColor);
            }
            contextBuilder = contextBuilder.addHighlightPos(firstPos)
                                           .addFirstPos(firstPos)
                                           .addHighlightPos(secondPos)
                                           .addSecondPos(secondPos);
        } else if (hammerTool.hasSelection()) {
            var player = client.player;
            assert player != null;
            if (!player.isShiftKeyDown()) {
                contextBuilder = contextBuilder.setHighlightColor(CommonConfig.selectionColor);
            } else {
                contextBuilder = contextBuilder.setHighlightColor(CommonConfig.removeSelectionColor);
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