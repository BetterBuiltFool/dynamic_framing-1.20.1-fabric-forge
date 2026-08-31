package com.github.betterbuiltfool.blocks;

import com.github.betterbuiltfool.blocks.block_entities.Size;
import com.github.betterbuiltfool.blocks.block_entities.StructureJointBlockEntity;
import com.github.betterbuiltfool.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class JointBlock extends FrameBlock {
    public static final String BLOCK_ID = "joint_block";
    
    // Represents connections. Size.NONE means unconnected.
    public static final EnumProperty<Size> NORTH = EnumProperty.create("north", Size.class);
    public static final EnumProperty<Size> SOUTH = EnumProperty.create("south", Size.class);
    public static final EnumProperty<Size> EAST = EnumProperty.create("east", Size.class);
    public static final EnumProperty<Size> WEST = EnumProperty.create("west", Size.class);
    public static final EnumProperty<Size> UP = EnumProperty.create("up", Size.class);
    public static final EnumProperty<Size> DOWN = EnumProperty.create("down", Size.class);
    
    public JointBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition()
                                      .any()
                                      .setValue(NORTH, Size.NONE)
                                      .setValue(SOUTH, Size.NONE)
                                      .setValue(EAST, Size.NONE)
                                      .setValue(WEST, Size.NONE)
                                      .setValue(UP, Size.NONE)
                                      .setValue(DOWN, Size.NONE)
        );
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos,
                                                BlockState state
    ) {
        return new StructureJointBlockEntity(BlockEntityRegistry.JOINT_ENTITY.get(), pos, state);
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN);
    }
}
