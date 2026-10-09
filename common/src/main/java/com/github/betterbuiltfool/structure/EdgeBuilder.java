package com.github.betterbuiltfool.structure;

import com.github.betterbuiltfool.blocks.BeamBlock;
import com.github.betterbuiltfool.blocks.JointBlock;
import com.github.betterbuiltfool.blocks.block_entities.BeamBlockEntity;
import com.github.betterbuiltfool.blocks.block_entities.EdgeProfile;
import com.github.betterbuiltfool.blocks.block_entities.JointBlockEntity;
import com.github.betterbuiltfool.blocks.block_entities.Size;
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


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
        startPos = getEndpoint(level, startPos, facing.getOpposite(), edgeMaterialBlockState);
        endPos = getEndpoint(level, endPos, facing, edgeMaterialBlockState);
        setEndJoint(level, startPos, endPos, edgeMaterialBlockState);
        setEndJoint(level, endPos, startPos, edgeMaterialBlockState);
        
        BlockPos finalStartPos = startPos;
        BlockPos finalEndPos = endPos;
        edgeWalk(
                startPos,
                endPos,
                facing,
                mutPos -> setFrameBlock(level, mutPos, finalStartPos, facing, edgeMaterialBlockState),
                mutPos -> setFrameBlock(level, mutPos, finalEndPos, facing, edgeMaterialBlockState)
        );
    }
    
    private static BlockPos getEndpoint(
            Level level,
            BlockPos pos,
            Direction direction,
            BlockState edgeMaterialBlockState
    ) {
        
        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockRegistry.JOINT_BLOCK.get())) {
            return pos;
        }
        if (!(level.getBlockEntity(pos) instanceof JointBlockEntity be)) {
            return pos;
        }
        if (be.getConnectionCount() != 1) {
            return pos;
        }
        if (!be.hasConnection(direction)) {
            return pos;
        }
        if (be.getEdgeMaterial(direction) != edgeMaterialBlockState) {
            return pos;
        }
        return BlockPos.of(be.getConnection(direction));
    }
    
    public static void edgeWalk(
            BlockPos start,
            BlockPos end,
            Direction towardsEnd,
            Consumer<BlockPos.MutableBlockPos> upperConsumer,
            Consumer<BlockPos.MutableBlockPos> lowerConsumer
    ) {
        var current = start.mutable();
        var dist = start.distManhattan(end);
        var halfway = dist / 2;
        for (int step = 0; step < halfway; step++) {
            current.move(towardsEnd);
            upperConsumer.accept(current);
        }
        for (int step = halfway; step < dist - 1; step++) {
            current.move(towardsEnd);
            lowerConsumer.accept(current);
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
        
        if (level.getBlockEntity(pos) instanceof BeamBlockEntity be) {
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
        
        Map<Direction, BlockPos> connections = new HashMap<>();
        Map<Direction, EdgeProfile> edgeData = new HashMap<>();
        
        
        Block jointBlock = BlockRegistry.JOINT_BLOCK.get();
        Block beamBlock = BlockRegistry.BEAM_BLOCK.get();
        if (!state.is(jointBlock)) {
            if (state.is(beamBlock)) {
                splitEdge(level, pos, connections, edgeData);
            }
            state = jointBlock.defaultBlockState();
        }
        var direction = getDirection(pos, connectedPos);
        EdgeProfile edgeProfile = new EdgeProfile(material, Size.FULL, direction);
        putConnectionData(connections, edgeData, connectedPos, edgeProfile);
        
        for (var entrySet : edgeData.entrySet()) {
            var dir = entrySet.getKey();
            var data = entrySet.getValue();
            
            var connectionProperty = JointBlock.connectionProperties.get(dir);
            state = state.setValue(connectionProperty, data.size());
        }
        
        level.setBlockAndUpdate(pos, state);
        
        if (!(level.getBlockEntity(pos) instanceof JointBlockEntity be)) {
            return;
        }
        
        for (var dir : connections.keySet()) {
            var connection = connections.get(dir);
            var data = edgeData.get(dir);
            
            be.registerConnection(connection, data.material(), data.size());
        }
        
    }
    
    private static void splitEdge(Level level,
                                  BlockPos pos,
                                  Map<Direction, BlockPos> connections,
                                  Map<Direction, EdgeProfile> edgeData
    ) {
        Edge edge = BeamBlock.getEdge(level, pos);
        BlockPos posA = BlockPos.of(edge.firstPos());
        BlockPos posB = BlockPos.of(edge.secondPos());
        var direction = getDirection(pos, posA);
        
        correctJointLinks(level, pos, edge, direction);
        
        EdgeProfile profileA = JointBlock.getEdgeProfile(level, posA, direction);
        EdgeProfile profileB = JointBlock.getEdgeProfile(level, posA, direction.getOpposite());
        JointBlock.removeConnection(level, posB, posA);
        JointBlock.removeConnection(level, posA, posB);
        putConnectionData(connections, edgeData, posA, profileA);
        putConnectionData(connections, edgeData, posB, profileB);
    }
    
    private static void correctJointLinks(Level level,
                                          BlockPos pos,
                                          Edge edge,
                                          Direction direction
    ) {
        var split = edge.splitAt(pos.asLong());
        var edgeLower = split.lower();
        var edgeUpper = split.upper();
        BlockPos lowerStart = BlockPos.of(edgeLower.firstPos());
        BlockPos lowerEnd = BlockPos.of(edgeLower.secondPos());
        edgeWalk(
                lowerStart,
                lowerEnd,
                direction,
                walkPos -> BeamBlock.setJoint(level, walkPos, lowerStart),
                walkPos -> BeamBlock.setJoint(level, walkPos, lowerEnd)
        );
        BlockPos upperStart = BlockPos.of(edgeUpper.firstPos());
        BlockPos upperEnd = BlockPos.of(edgeUpper.secondPos());
        edgeWalk(
                upperStart,
                upperEnd,
                direction,
                walkPos -> BeamBlock.setJoint(level, walkPos, upperStart),
                walkPos -> BeamBlock.setJoint(level, walkPos, upperEnd)
        );
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
    public static void removeMaterialCost(
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
    
    private static void putConnectionData(
            Map<Direction, BlockPos> edges,
            Map<Direction, EdgeProfile> connections,
            BlockPos pos,
            EdgeProfile profile
    ) {
        edges.put(profile.direction(), pos);
        connections.put(profile.direction(), profile);
    }
    
    private static Direction getDirection(BlockPos posA,
                                          BlockPos posB
    ) {
        var directionVector = posB.subtract(posA);
        return Direction.getNearest(
                directionVector.getX(),
                directionVector.getY(),
                directionVector.getZ()
        );
    }
}
