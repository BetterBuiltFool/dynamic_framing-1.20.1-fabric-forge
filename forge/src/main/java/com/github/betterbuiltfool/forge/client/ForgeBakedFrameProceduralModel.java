package com.github.betterbuiltfool.forge.client;

import com.github.betterbuiltfool.blocks.BeamBlock;
import com.github.betterbuiltfool.blocks.FrameBlockStateData;
import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import com.github.betterbuiltfool.client.ProceduralFrameModel;
import com.github.betterbuiltfool.registry.BlockEntityRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.extensions.IForgeBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ForgeBakedFrameProceduralModel implements IForgeBakedModel, BakedModel {
    
    private static final ModelProperty<Alignment> ALIGN_X_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<Alignment> ALIGN_Y_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<Alignment> ALIGN_Z_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<Size> SIZE_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<Direction.Axis> AXIS_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<BlockState> COPY_MATERIAL_PROPERTY = new ModelProperty<>();
    
    public static final ForgeBakedFrameProceduralModel INSTANCE = new ForgeBakedFrameProceduralModel();
    
    private ForgeBakedFrameProceduralModel() {
    
    }
    
    @Override
    public @NotNull List<BakedQuad> getQuads(
            @Nullable BlockState state,
            @Nullable Direction side,
            @NotNull RandomSource rand,
            @NotNull ModelData data,
            @Nullable RenderType renderType
    ) {
        var alignX = data.get(ALIGN_X_PROPERTY);
        var alignY = data.get(ALIGN_Y_PROPERTY);
        var alignZ = data.get(ALIGN_Z_PROPERTY);
        var size = data.get(SIZE_PROPERTY);
        var copyMaterial = data.get(COPY_MATERIAL_PROPERTY);
        
        if (alignX == null || alignY == null || alignZ == null || size == null || copyMaterial == null) {
            DynamicFramingClientForge.LOGGER.info(
                    "Data failure; ModelData was not properly packed. Returning empty list.");
            return List.of();
        }
        
        DynamicFramingClientForge.LOGGER.info("Creating model quads");
        
        return ProceduralFrameModel.generateQuads(side, rand, alignX, alignY, alignZ, size, copyMaterial);
    }
    
    @Override
    public @NotNull ModelData getModelData(
            @NotNull BlockAndTintGetter level,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @NotNull ModelData modelData
    ) {
        var defaultData = FrameBlockStateData.DEFAULT;
        var alignX = defaultData.alignX();
        var alignY = defaultData.alignY();
        var alignZ = defaultData.alignZ();
        var size = defaultData.size();
        var axis = state.getValue(BeamBlock.AXIS);
        var copyMaterial = Blocks.OAK_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS, axis);
        
        DynamicFramingClientForge.LOGGER.info("Getting model data...");
        var blockEntityResult = level.getBlockEntity(pos, BlockEntityRegistry.MEMBER_ENTITY.get());
        if (blockEntityResult.isPresent()) {
            DynamicFramingClientForge.LOGGER.info("Found block entity for member.");
            var blockEntity = blockEntityResult.get();
            
            BlockPos jointPos = blockEntity.getJointPos();
            if (jointPos != null) {
                DynamicFramingClientForge.LOGGER.info("Joint pos: {}", jointPos);
                var jointEntityResult = level.getBlockEntity(jointPos, BlockEntityRegistry.JOINT_ENTITY.get());
                if (jointEntityResult.isPresent()) {
                    DynamicFramingClientForge.LOGGER.info("Found joint entity.");
                    var jointEntity = jointEntityResult.get();
                    
                    var edgeDirection = blockEntity.getDirection();
                    var edgeData = jointEntity.getEdgeData(edgeDirection);
                    copyMaterial = jointEntity.getEdgeMaterial(edgeDirection);
                    alignX = edgeData.alignX();
                    alignY = edgeData.alignY();
                    alignZ = edgeData.alignZ();
                    size = edgeData.size();
                    axis = edgeDirection.getAxis();
                }
            }
        }
        return ModelData.builder()
                        .with(ALIGN_X_PROPERTY, alignX)
                        .with(ALIGN_Y_PROPERTY, alignY)
                        .with(ALIGN_Z_PROPERTY, alignZ)
                        .with(SIZE_PROPERTY, size)
                        .with(COPY_MATERIAL_PROPERTY, copyMaterial)
                        .with(AXIS_PROPERTY, axis)
                        .build();
    }
    
    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state,
                                             @Nullable Direction direction,
                                             @NotNull RandomSource random
    ) {
        DynamicFramingClientForge.LOGGER.info("Fetching WRONG quads");
        return List.of();
    }
    
    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }
    
    @Override
    public boolean isGui3d() {
        return false;
    }
    
    @Override
    public boolean usesBlockLight() {
        return false;
    }
    
    @Override
    public boolean isCustomRenderer() {
        return false;
    }
    
    @Override
    public TextureAtlasSprite getParticleIcon() {
        return null;
    }
    
    @Override
    public ItemOverrides getOverrides() {
        return null;
    }
}
