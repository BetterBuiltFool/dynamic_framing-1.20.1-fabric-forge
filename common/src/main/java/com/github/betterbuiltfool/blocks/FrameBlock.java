package com.github.betterbuiltfool.blocks;

import com.github.betterbuiltfool.blocks.block_entities.StructureJointBlockEntity;
import com.github.betterbuiltfool.blocks.block_entities.StructureMemberBlockEntity;
import com.github.betterbuiltfool.helper.FrameEndpointHelper;
import com.github.betterbuiltfool.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public abstract class FrameBlock extends Block implements EntityBlock {
    public FrameBlock(Properties properties) {
        super(properties);
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
