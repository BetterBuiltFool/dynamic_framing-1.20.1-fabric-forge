package com.github.betterbuiltfool.structure;

import com.github.betterbuiltfool.blocks.BeamBlock;
import com.github.betterbuiltfool.blocks.JointBlock;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import com.github.betterbuiltfool.blocks.block_entities.StructureJointBlockEntity;
import com.github.betterbuiltfool.blocks.block_entities.StructureMemberBlockEntity;
import com.github.betterbuiltfool.registry.BlockRegistry;
import com.github.betterbuiltfool.validation.BlockPosValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;


public class EdgeBuilder {
    
    public static void build(
            Level level,
            long firstPos,
            long secondPos,
            Block edgeMaterial
    ) {
        var startPos = BlockPos.of(firstPos);
        var endPos = BlockPos.of(secondPos);
        
        var directionVector = endPos.subtract(startPos);
        var facing = Direction.getNearest(
                directionVector.getX(),
                directionVector.getY(),
                directionVector.getZ()
        );
        
        var edgeMaterialBlockState = edgeMaterial.defaultBlockState()
                                     .setValue(BlockStateProperties.AXIS, facing.getAxis());
        
        setEndJoint(level, startPos, endPos, edgeMaterialBlockState);
        setEndJoint(level, endPos, startPos, edgeMaterialBlockState);
        
        var current = startPos.mutable();
        var dist = startPos.distManhattan(endPos);
        var halfway = dist / 2;
        var opposite = facing.getOpposite();
        for (int step = 0; step < halfway; step++) {
            current = current.move(facing);
            setFrameBlock(level, current, startPos, facing, edgeMaterialBlockState);
        }
        for (int step = halfway; step < dist - 1; step++) {
            current = current.move(facing);
            setFrameBlock(level, current, endPos, opposite, edgeMaterialBlockState);
        }
        
    }
    
    private static void setFrameBlock(
            Level level,
            BlockPos.MutableBlockPos pos,
            BlockPos jointPos,
            Direction facing,
            BlockState material
    ) {
        var axis = facing.getAxis();
        BlockState state = BlockRegistry.BEAM_BLOCK.get()
                                                   .defaultBlockState()
                                                   .setValue(BeamBlock.AXIS, axis);
        
        level.setBlock(pos, state, Block.UPDATE_ALL);
        
        if (level.getBlockEntity(pos) instanceof StructureMemberBlockEntity be) {
            be.setJointPos(jointPos);
            be.setDirection(facing);
            be.setMaterial(material);
            be.setChanged();
        }
        
    }
    
    private static void setEndJoint(
            Level level,
            BlockPos pos,
            BlockPos connectedPos,
            BlockState material
    ) {
        var state = level.getBlockState(pos);
        
        Block jointBlock = BlockRegistry.JOINT_BLOCK.get();
        if (!state.is(jointBlock)) {
            state = jointBlock.defaultBlockState();
        }
        var directionVector = connectedPos.subtract(pos);
        var direction = Direction.getNearest(directionVector.getX(), directionVector.getY(), directionVector.getZ());
        
        var connectionProperty = JointBlock.connectionProperties.get(direction);
        state = state.setValue(connectionProperty, Size.FULL);
        
        level.setBlockAndUpdate(pos, state);
        
        if (!(level.getBlockEntity(pos) instanceof StructureJointBlockEntity be)) {
            return;
        }
        
        be.registerConnection(connectedPos, material, Size.FULL);
        
    }
    
    public static int getMaterialCost(
            Level level,
            long firstPos,
            long secondPos
    ) {
        return Math.toIntExact(
                BlockPos.betweenClosedStream(BlockPos.of(firstPos), BlockPos.of(secondPos))
                        .filter(blockPos -> BlockPosValidator.validate(level, blockPos))
                        .count()
        );
    }
    
    /**
     * Extracts the material cost from the given inventory, preferentially removing first from the inventory, and
     * removing the remainder from the offhand stack.
     * <p>
     * Note this will not fail if the inventory does not have enough items.
     *
     * @param inventory    The source inventory that supplies raw materials.
     * @param offhandItem  The item type to be removed, and secondary source of raw materials
     * @param materialCost The total amount of materials to be extracted.
     */
    private void removeMaterialCost(
            @NotNull Inventory inventory,
            @NotNull ItemStack offhandItem,
            int materialCost
    ) {
        int amountRemoved = 0;
        for (ItemStack slotItem : inventory.items) {
            if (slotItem.getItem() != offhandItem.getItem()) {
                continue;
            }
            int slotCount = slotItem.getCount();
            amountRemoved += slotCount;
            
            if (amountRemoved >= materialCost) {
                int amountUsed = amountRemoved - materialCost;
                slotItem.setCount(amountUsed);
                break;
            } else {
                slotItem.setCount(0);
            }
        }
        if (amountRemoved < materialCost) {
            int amountUsed = materialCost - amountRemoved;
            offhandItem.setCount(offhandItem.getCount() - amountUsed);
        }
        
    }
}
