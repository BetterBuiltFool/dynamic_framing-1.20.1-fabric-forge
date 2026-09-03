package com.github.betterbuiltfool.blocks;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.StructureJointBlockEntity;
import com.github.betterbuiltfool.blocks.block_entities.StructureMemberBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public abstract class FrameBlock extends Block implements EntityBlock {
    
    public static EnumProperty<Alignment> ALIGNMENT_PRIMARY =
            EnumProperty.create("alignment_primary", Alignment.class);
    public static EnumProperty<Alignment> ALIGNMENT_SECONDARY =
            EnumProperty.create("alignment_secondary", Alignment.class);
    
    public FrameBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition()
                                      .any()
                                      .setValue(ALIGNMENT_PRIMARY, Alignment.CENTER)
                                      .setValue(ALIGNMENT_SECONDARY, Alignment.CENTER));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ALIGNMENT_PRIMARY)
               .add(ALIGNMENT_SECONDARY);
    }
    
    public BlockState getComposedMaterial(
            BlockGetter level,
            BlockPos pos
    ) {
        var blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof StructureMemberBlockEntity structureBE)) return null;
        var jointPos = structureBE.getJointPos();
        
        if (!(level.getBlockEntity(jointPos) instanceof StructureJointBlockEntity jbEntity)) {
            return null;
        }
        return jbEntity.getEdgeProfile(structureBE.getDirection())
                       .material();
    }
    
    @Override
    @Deprecated
    public float getDestroyProgress(BlockState state,
                                    Player player,
                                    BlockGetter level,
                                    BlockPos pos
    ) {
        BlockState material = getComposedMaterial(level, pos);
        if (material == null) {
            return super.getDestroyProgress(state, player, level, pos);
        }
        
        return material.getDestroyProgress(player, level, pos);
    }
    
}
