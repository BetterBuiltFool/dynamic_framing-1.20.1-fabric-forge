package com.github.betterbuiltfool.forge.client;

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
                    
                    var direction = blockEntity.getDirection();
                    var edgeData = jointEntity.getEdgeData(direction);
                    var copyMaterial = jointEntity.getEdgeMaterial(direction);
                    
                    return ModelData.builder()
                                    .with(ALIGN_X_PROPERTY, edgeData.alignX())
                                    .with(ALIGN_Y_PROPERTY, edgeData.alignY())
                                    .with(ALIGN_Z_PROPERTY, edgeData.alignZ())
                                    .with(SIZE_PROPERTY, edgeData.size())
                                    .with(COPY_MATERIAL_PROPERTY, copyMaterial)
                                    .build();
                }
            }
        }
        DynamicFramingClientForge.LOGGER.info("Failed model data packing, going default...");
        var defaultData = FrameBlockStateData.DEFAULT;
        var copyMaterial = Blocks.OAK_LOG.defaultBlockState();
        return ModelData.builder()
                        .with(ALIGN_X_PROPERTY, defaultData.alignX())
                        .with(ALIGN_Y_PROPERTY, defaultData.alignY())
                        .with(ALIGN_Z_PROPERTY, defaultData.alignZ())
                        .with(SIZE_PROPERTY, defaultData.size())
                        .with(COPY_MATERIAL_PROPERTY, copyMaterial)
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
