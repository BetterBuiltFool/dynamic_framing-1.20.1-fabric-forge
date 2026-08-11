package com.github.betterbuiltfool.blocks;

import com.github.betterbuiltfool.blocks.block_entities.StructureMemberBlockEntity;
import com.github.betterbuiltfool.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public class BeamBlock extends FrameBlock {
    public static final String BLOCK_ID = "beam_block";
    
    public static EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    
    public BeamBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition()
                                      .any()
                                      .setValue(AXIS, Direction.Axis.X));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos,
                                                BlockState state
    ) {
        return new StructureMemberBlockEntity(BlockEntityRegistry.MEMBER_ENTITY.get(), pos, state);
    }
}
