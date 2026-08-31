package com.github.betterbuiltfool.blocks;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import com.github.betterbuiltfool.blocks.block_entities.StructureMemberBlockEntity;
import com.github.betterbuiltfool.geometry.BeamGeometryData;
import com.github.betterbuiltfool.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeamBlock extends FrameBlock {
    public static final String BLOCK_ID = "beam_block";
    public static EnumProperty<Size> SCALING =
            EnumProperty.create("scaling", Size.class, Size.FULL, Size.HALF, Size.QUARTER);
    
    public BeamBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition()
                                      .any()
                                      .setValue(SCALING, Size.FULL));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SCALING);
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos,
                                                BlockState state
    ) {
        return new StructureMemberBlockEntity(BlockEntityRegistry.MEMBER_ENTITY.get(), pos, state);
    }
    
    @Override
    @Deprecated
    public @NotNull VoxelShape getShape(BlockState state,
                                        BlockGetter level,
                                        BlockPos pos,
                                        CollisionContext context
    ) {
        return calcShape(state);
    }
    
    @Override
    @Deprecated
    public @NotNull VoxelShape getOcclusionShape(BlockState state,
                                                 BlockGetter level,
                                                 BlockPos pos
    ) {
        return calcShape(state);
    }
    
    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
    
    private static VoxelShape calcShape(BlockState state) {
        var geometry = calcGeometry(state);
        
        return Shapes.create(
                geometry.minX(),
                geometry.minY(),
                geometry.minZ(),
                geometry.maxX(),
                geometry.maxY(),
                geometry.maxZ()
        );
    }
    
    private static BeamGeometryData calcGeometry(BlockState state) {
        var axis = state.getValue(AXIS);
        var primary = state.getValue(ALIGNMENT_PRIMARY);
        var secondary = state.getValue(ALIGNMENT_SECONDARY);
        var scale = state.getValue(SCALING);
        
        return BeamGeometryData.from(
                primary,
                secondary,
                axis,
                scale
        );
    }
}
