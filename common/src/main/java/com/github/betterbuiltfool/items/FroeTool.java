package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.blocks.FrameBlock;
import com.github.betterbuiltfool.blocks.block_entities.BeamBlockEntity;
import com.github.betterbuiltfool.blocks.block_entities.JointBlockEntity;
import com.github.betterbuiltfool.client.ClientLocalNodes;
import com.github.betterbuiltfool.config.CommonConfig;
import com.github.betterbuiltfool.data.RaycastService;
import com.github.betterbuiltfool.items.nbtHelper.FroeData;
import com.github.betterbuiltfool.structure.EdgeBuilder;
import com.github.betterbuiltfool.ui.overlays.FramingHammerOverlay;
import com.github.betterbuiltfool.ui.overlays.NodeOverlayContextBuilder;
import com.github.betterbuiltfool.validation.EdgeValidator;
import com.github.betterbuiltfool.validation.ItemValidator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FroeTool extends Item implements RendersOverlay, SuppressesEquipAnimation, HasLeftClickUse {
    public static final String ITEM_ID = "froe";
    
    public FroeTool(Properties properties) {
        super(properties);
    }
    
    @Override
    public void inventoryTick(
            ItemStack stack,
            Level level,
            Entity entity,
            int slotId,
            boolean isSelected
    ) {
        if (!isSelected || !(entity instanceof Player player) || level.isClientSide()) {
            return;
        }
        
        var froeTool = new FroeData(stack);
        
        var selection = RaycastService.getClosestEdge(player);
        froeTool.setSelection(selection);
    }
    
    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        var hand = context.getHand();
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var blockState = level.getBlockState(pos);
        
        if (!(blockState.getBlock() instanceof FrameBlock)) return InteractionResult.PASS;
        
        
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            tryPushEdgeAlignment(level, pos, context.getClickedFace()
                                                    .getAxis()
            );
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand usedHand
    ) {
        var stack = player.getMainHandItem();
        if (usedHand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        var froeTool = new FroeData(stack);
        
        if (!froeTool.hasSelection()) {
            return InteractionResultHolder.pass(stack);
        }
        
        var selection = froeTool.getSelection();
        if (!EdgeValidator.validate(level, selection.posA(), selection.posB())) return InteractionResultHolder.pass(stack);
        
        ItemStack offhandItem = player.getOffhandItem();
        var offhandBlockItem = ItemValidator.validatedStructureItem(offhandItem);
        if (offhandBlockItem == null) {
            return InteractionResultHolder.pass(stack);
        }
        // TODO: Check inventory amount of offhandItem;
        if (!level.isClientSide()) {
            EdgeBuilder.build(level, selection.posA(), selection.posB(), offhandBlockItem.getBlock());
            // TODO: Handle inventory adjustment
        }
        return InteractionResultHolder.success(stack);
    }
    
    @Override
    public InteractionResult useLeftClick(Level level,
                                          Player player,
                                          InteractionHand interactionHand
    ) {
        var pos = getLookPos(player, level);
        
        if (!level.isClientSide()) {
            tryScaleEdge(level, pos);
        }
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public boolean shouldBlockMining(Player player,
                                     ItemStack itemStack
    ) {
        return true;
    }
    
    private static @NotNull BlockPos getLookPos(Player player,
                                                Level level
    ) {
        
        Vec3 start = getEyePosition(player);
        var blockHitResult = level.clip(new ClipContext(
                start,
                start.add(player.getViewVector(1.0f)
                                .scale(5.0f)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));
        return blockHitResult.getBlockPos();
    }
    
    private static @NotNull Vec3 getEyePosition(Player player) {
        return player.getEyePosition();
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
    
    @Override
    public void renderOverlay(Minecraft client,
                              PoseStack poseStack,
                              @NotNull ItemStack itemStack
    ) {
        ClientLocalNodes.requestRefresh(client);
        var froeTool = new FroeData(itemStack);
        
        var contextBuilder = new NodeOverlayContextBuilder(client, poseStack).addNodeMap(ClientLocalNodes.getLocalNodes());
        
        if (froeTool.hasSelection()) {
            var selection = froeTool.getSelection();
            if (EdgeValidator.validate(client.level, selection.posA(), selection.posB())) {
                contextBuilder.setHighlightColor(CommonConfig.validEdgeColor);
            } else {
                contextBuilder.setHighlightColor(CommonConfig.invalidEdgeColor);
            }
            contextBuilder.addFirstPos(selection.posA()).addSecondPos(selection.posB());
        }
        FramingHammerOverlay.renderOverlay(contextBuilder.build());
    }
    
    private void tryScaleEdge(
            Level level,
            BlockPos pos
    ) {
        var jointEntity = getJointEntity(level, pos);
        if (jointEntity == null) {
            return;
        }
        if (jointEntity.getBlockPos() == pos) {
            // Must have clicked on the joint itself, so cycle all directions
            for (var direction : Direction.values()) {
                jointEntity.cycleEdgeScale(direction);
            }
            return;
        }
        var vector = pos.subtract(jointEntity.getBlockPos());
        var direction = Direction.getNearest(vector.getX(), vector.getY(), vector.getZ());
        jointEntity.cycleEdgeScale(direction);
    }
    
    private void tryPushEdgeAlignment(
            Level level,
            BlockPos pos,
            Direction.Axis faceAxis
    ) {
        // Find the edge from the selection
        // determine if we're pushing or scaling
        // Go to each joint, and alter the scale/offset as appropriate
        var jointEntity = getJointEntity(level, pos);
        if (jointEntity == null) {
            return;
        }
        jointEntity.cycleAxisAlignment(faceAxis);
    }
    
    private @Nullable JointBlockEntity getJointEntity(Level level,
                                                      BlockPos pos
    ) {
        var entity = level.getBlockEntity(pos);
        if (entity instanceof BeamBlockEntity be) {
            var jointPosEntity = level.getBlockEntity(be.getJointPos());
            if (jointPosEntity instanceof JointBlockEntity jbe) {
                return jbe;
            }
        }
        if (entity instanceof JointBlockEntity jbe) {
            return jbe;
        }
        return null;
        
    }
}
