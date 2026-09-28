package com.github.betterbuiltfool.fabric.client;

import com.github.betterbuiltfool.blocks.BeamBlock;
import com.github.betterbuiltfool.blocks.block_entities.BeamBlockEntity;
import com.github.betterbuiltfool.client.ProceduralFrameModel;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class FabricProceduralBeamModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private TextureAtlasSprite fallbackParticleSprite;
    
    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state,
                                             @Nullable Direction direction,
                                             RandomSource random
    ) {
        return List.of();
    }
    
    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView,
                               BlockState state,
                               BlockPos pos,
                               Supplier<RandomSource> randomSupplier,
                               RenderContext context
    ) {
        var primary = state.getValue(BeamBlock.ALIGNMENT_PRIMARY);
        var secondary = state.getValue(BeamBlock.ALIGNMENT_SECONDARY);
        var scaling = state.getValue(BeamBlock.SCALING);
        var axis = state.getValue(BeamBlock.AXIS);
        var copyMaterial = Blocks.OAK_LOG.defaultBlockState()
                                         .setValue(BlockStateProperties.AXIS, axis);
        
        if (blockView.getBlockEntityRenderData(pos) instanceof BeamBlockEntity.RenderData renderData) {
            var material = renderData.material();
            copyMaterial = material != null ? material : copyMaterial;
        }
        
        var bakedQuads = ProceduralFrameModel.generateQuads(
                null,
                randomSupplier.get(),
                primary,
                secondary,
                axis,
                scaling,
                copyMaterial
        );
        
        var emitter = context.getEmitter();
        var material = RendererAccess.INSTANCE
                               .getRenderer()
                               .materialFinder()
                               .find();
        
        for (var quad : bakedQuads) {
            emitter.fromVanilla(
                    quad,
                    material,
                    null
            );
            emitter.emit();
        }
    }
    
    @Override
    public boolean useAmbientOcclusion() {
        return true;
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
    public @NotNull ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }
    
    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
    
    @Override
    public @NotNull Collection<ResourceLocation> getDependencies() {
        return List.of();
    }
    
    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
    
    }
    
    @Override
    public @Nullable BakedModel bake(ModelBaker baker,
                                     Function<Material, TextureAtlasSprite> spriteGetter,
                                     ModelState state,
                                     ResourceLocation location
    ) {
        return this;
    }
    
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
