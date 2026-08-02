package com.github.betterbuiltfool.blocks;

import com.github.betterbuiltfool.blocks.block_entities.StructureJointBlockEntity;
import com.github.betterbuiltfool.helper.FrameEndpointHelper;
import com.github.betterbuiltfool.registry.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class FrameBlock extends Block {
    public FrameBlock(Properties properties) {
        super(properties);
    }
    
    public BlockState getComposedMaterial(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        
        boolean isVertical = state.is(BlockRegistry.POST_BLOCK.get());
        Direction negative = FrameEndpointHelper.getNegativeAxis(state, isVertical);
        
        BlockPos jointPos;
        Direction positive = negative.getOpposite();
        
        var negativePos = FrameEndpointHelper.findEndPoint((BlockAndTintGetter) level, pos, negative);
        var positivePos = FrameEndpointHelper.findEndPoint((BlockAndTintGetter) level, pos, positive);
        
        var negativeDist = pos.distToCenterSqr(negativePos.getCenter());
        var positiveDist = pos.distToCenterSqr(positivePos.getCenter());
        
        if (negativeDist < positiveDist) {
            jointPos = negativePos;
        } else {
            jointPos = positivePos;
        }
        
        if (!(level.getBlockEntity(jointPos) instanceof StructureJointBlockEntity jbEntity)) {
            return null;
        }
        var oppositeJointPos = jointPos == negativePos ? positivePos : negativePos;
        return jbEntity.getEdgeProfile(oppositeJointPos)
                       .material();
    }
    
    @Override
    @Deprecated
    public float getDestroyProgress(BlockState state,
                                    Player player,
                                    BlockGetter level,
                                    BlockPos pos
    ) {
        BlockState material = getComposedMaterial(state, level, pos);
        if (material == null) {
            return super.getDestroyProgress(state, player, level, pos);
        }
        
        return material.getDestroyProgress(player, level, pos);
    }
    
}
