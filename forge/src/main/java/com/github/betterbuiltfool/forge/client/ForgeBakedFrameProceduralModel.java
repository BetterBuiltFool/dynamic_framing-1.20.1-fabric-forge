package com.github.betterbuiltfool.forge.client;

import com.github.betterbuiltfool.blocks.BeamBlock;
import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import com.github.betterbuiltfool.blocks.block_entities.StructureMemberBlockEntity;
import com.github.betterbuiltfool.client.ProceduralFrameModel;
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
    
    private static final ModelProperty<Size> SIZE_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<Direction.Axis> AXIS_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<Alignment> ALIGN_PRIMARY_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<Alignment> ALIGN_SECONDARY_PROPERTY = new ModelProperty<>();
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
        var alignPrimary = data.get(ALIGN_PRIMARY_PROPERTY);
        var alignSecondary = data.get(ALIGN_SECONDARY_PROPERTY);
        var size = data.get(SIZE_PROPERTY);
        var axis = data.get(AXIS_PROPERTY);
        var copyMaterial = data.get(COPY_MATERIAL_PROPERTY);
        
        if (
                alignPrimary == null ||
                alignSecondary == null ||
                size == null ||
                axis == null ||
                copyMaterial == null
        ) {
            DynamicFramingClientForge.LOGGER.info(
                    "Data failure; ModelData was not properly packed. Returning empty list.");
            DynamicFramingClientForge.LOGGER.info(
                    "Bad info alignPrimary={}, alignSecondary={}, size={}, copyMaterial={}, axis={}", alignPrimary,
                    alignSecondary, size, copyMaterial, axis
            );
            return List.of();
        }
        return ProceduralFrameModel.generateQuads(side, rand, alignPrimary, alignSecondary, axis, size, copyMaterial);
    }
    
    @Override
    public @NotNull ModelData getModelData(
            @NotNull BlockAndTintGetter level,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @NotNull ModelData modelData
    ) {
        var primary = state.getValue(BeamBlock.ALIGNMENT_PRIMARY);
        var secondary = state.getValue(BeamBlock.ALIGNMENT_PRIMARY);
        var scaling = state.getValue(BeamBlock.SCALING);
        var axis = state.getValue(BeamBlock.AXIS);
        var copyMaterial = Blocks.OAK_LOG.defaultBlockState()
                                         .setValue(BlockStateProperties.AXIS, axis);
        
        if ((level.getBlockEntity(pos) instanceof StructureMemberBlockEntity be)) {
            var material = be.getMaterial();
            
            copyMaterial = material != null ? material : copyMaterial;
        }
        
        return ModelData.builder()
                       .with(ALIGN_PRIMARY_PROPERTY, primary)
                       .with(ALIGN_SECONDARY_PROPERTY, secondary)
                       .with(AXIS_PROPERTY, axis)
                       .with(SIZE_PROPERTY, scaling)
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
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return null;
    }
    
    @Override
    public @NotNull ItemOverrides getOverrides() {
        return null;
    }
}
