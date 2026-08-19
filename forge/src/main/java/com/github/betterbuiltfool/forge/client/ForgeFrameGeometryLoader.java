package com.github.betterbuiltfool.forge.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class ForgeFrameGeometryLoader implements IGeometryLoader<ForgeFrameGeometryLoader.Unbaked> {
    public static final ForgeFrameGeometryLoader INSTANCE = new ForgeFrameGeometryLoader();
    
    private ForgeFrameGeometryLoader() {
    }
    
    @Override
    public Unbaked read(JsonObject jsonObject,
                        JsonDeserializationContext jsonDeserializationContext
    ) throws JsonParseException {
        DynamicFramingClientForge.LOGGER.info("Loading model json");
        DynamicFramingClientForge.LOGGER.info("Nothing to load");
        return new Unbaked();
    }
    
    public record Unbaked() implements IUnbakedGeometry<Unbaked> {
        
        @Override
        public BakedModel bake(IGeometryBakingContext iGeometryBakingContext,
                               ModelBaker baker,
                               Function<Material, TextureAtlasSprite> spriteGetter,
                               ModelState modelState,
                               ItemOverrides overrides,
                               ResourceLocation resourceLocation
        ) {
            DynamicFramingClientForge.LOGGER.info("Getting baked model instance");
            return ForgeBakedFrameProceduralModel.INSTANCE;
        }
    }
}
