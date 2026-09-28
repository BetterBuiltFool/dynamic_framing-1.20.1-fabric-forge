package com.github.betterbuiltfool.forge.client;

import com.github.betterbuiltfool.blocks.JointBlock;
import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import com.github.betterbuiltfool.blocks.block_entities.StructureJointBlockEntity;
import com.github.betterbuiltfool.client.ProceduralJointModel;
import net.minecraft.client.Minecraft;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForgeProceduralJointBakedModel implements IForgeBakedModel, BakedModel {
    
    public ModelProperty<Map<Direction, Size>> SIZES_PROPERTY = new ModelProperty<>();
    public ModelProperty<Map<Direction, BlockState>> COPY_MATERIALS_PROPERTY = new ModelProperty<>();
    public ModelProperty<TextureAtlasSprite> PARTICLE_SPRITE = new ModelProperty<>();
    private static final ModelProperty<Alignment> ALIGN_X_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<Alignment> ALIGN_Y_PROPERTY = new ModelProperty<>();
    private static final ModelProperty<Alignment> ALIGN_Z_PROPERTY = new ModelProperty<>();
    
    public static final ForgeProceduralJointBakedModel INSTANCE = new ForgeProceduralJointBakedModel();
    
    private TextureAtlasSprite fallbackParticleSprite;
    
    private ForgeProceduralJointBakedModel() {
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
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state,
                                             @Nullable Direction side,
                                             @NotNull RandomSource rand,
                                             @NotNull ModelData data,
                                             @Nullable RenderType renderType
    ) {
        if (side != null) return List.of();
        var alignX = data.get(ALIGN_X_PROPERTY);
        var alignY = data.get(ALIGN_Y_PROPERTY);
        var alignZ = data.get(ALIGN_Z_PROPERTY);
        var sizes = data.get(SIZES_PROPERTY);
        var materials = data.get(COPY_MATERIALS_PROPERTY);
        
        return ProceduralJointModel.generateQuads(
                side,
                rand,
                alignX,
                alignY,
                alignZ,
                sizes,
                materials
        );
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level,
                                           @NotNull BlockPos pos,
                                           @NotNull BlockState state,
                                           @NotNull ModelData modelData
    ) {
        
        var alignX = state.getValue(JointBlock.ALIGNMENT_PRIMARY);
        var alignY = state.getValue(JointBlock.ALIGNMENT_SECONDARY);
        var alignZ = state.getValue(JointBlock.ALIGNMENT_TERTIARY);
        var sizes = JointBlock.getConnectionSizes(state);
        Map<Direction, BlockState> materials = new HashMap<>();
        TextureAtlasSprite particleSprite = null;
        
        if ((level.getBlockEntity(pos) instanceof StructureJointBlockEntity be)) {
            materials = be.getRenderData().renderMap();
            
            BlockState particleState = materials.values().stream().findAny().orElse(null);
            if (particleState != null) {
                particleSprite = Minecraft.getInstance()
                                         .getBlockRenderer()
                                         .getBlockModel(particleState)
                                         .getParticleIcon();
            }
        }
        
        return ModelData.builder()
                        .with(ALIGN_X_PROPERTY, alignX)
                        .with(ALIGN_Y_PROPERTY, alignY)
                        .with(ALIGN_Z_PROPERTY, alignZ)
                        .with(SIZES_PROPERTY, sizes)
                        .with(COPY_MATERIALS_PROPERTY, materials)
                        .with(PARTICLE_SPRITE, particleSprite)
                        .build();
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
        return getFallbackSprite();
    }
    
    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        var sprite = data.get(PARTICLE_SPRITE);
        if (sprite == null) {
            return getFallbackSprite();
        }
        
        return sprite;
    }
    
    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
    
    @SuppressWarnings("deprecation")
    private TextureAtlasSprite getFallbackSprite() {
        if (fallbackParticleSprite == null) {
            fallbackParticleSprite = Minecraft.getInstance()
                                              .getBlockRenderer()
                                              .getBlockModel(Blocks.STRIPPED_OAK_LOG.defaultBlockState())
                                              .getParticleIcon();
        }
        return fallbackParticleSprite;
    }
}
