package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;

public class ProceduralFrameModel {
    
    private static final ResourceLocation GENERATED_QUADS =
            new ResourceLocation(DynamicFraming.MOD_ID, "generated_geometry");
    
    public static @NotNull ArrayList<BakedQuad> generateQuads(
            Direction side,
            RandomSource rand,
            Alignment primary,
            Alignment secondary,
            Direction.Axis axis,
            Size size,
            BlockState material
    ) {
        var dispatcher = Minecraft.getInstance()
                                  .getBlockRenderer();
        var materialModel = dispatcher.getBlockModel(material);
        var scale = size.getThickness();
        var bounds = calcAxisBounds(primary, secondary, axis, scale);
        var bakery = new FaceBakery();
        var faces = new ArrayList<BakedQuad>();
        
        for (var quad : materialModel.getQuads(material, side, rand)) {
            var sprite = quad.getSprite();
            var direction = quad.getDirection();
            var from = getQuadFrom(bounds, direction);
            var to = getQuadTo(bounds, direction);
            var face = getBlockElementFace(direction, bounds);
            
            faces.add(
                    bakery.bakeQuad(
                            from,
                            to,
                            face,
                            sprite,
                            direction,
                            BlockModelRotation.X0_Y0,
                            null,
                            true,
                            GENERATED_QUADS
                    )
            );
        }
        
        return faces;
    }
    
    private static @NotNull BlockElementFace getBlockElementFace(Direction direction,
                                                                 Bounds bounds
    ) {
        float[] uvBounds = switch (direction.getAxis()) {
            case X -> calcUV(bounds.y(), bounds.z(), false);
            case Y -> calcUV(bounds.x(), bounds.z(), false);
            default -> calcUV(bounds.x(), bounds.y(), true);
        };
        
        var uv = new BlockFaceUV(
                uvBounds,
                0
        );
        
        var face = new BlockElementFace(
                null,
                -1,
                "texture",
                uv
        );
        return face;
    }
    
    public static @NotNull ArrayList<BakedQuad> generateQuads(
            Direction side,
            RandomSource rand,
            Alignment alignX,
            Alignment alignY,
            Alignment alignZ,
            Size size,
            BlockState material
    ) {
        
        var dispatcher = Minecraft.getInstance()
                                  .getBlockRenderer();
        var materialModel = dispatcher.getBlockModel(material);
        var originalQuads = materialModel.getQuads(material, side, rand);
        var newQuads = new ArrayList<BakedQuad>();
        
        float scale = size.getThickness();
        var xBounds = calcAxisBounds(alignX, size);
        var yBounds = calcAxisBounds(alignY, size);
        var zBounds = calcAxisBounds(alignZ, size);
        
        float[][] bounds = {xBounds, yBounds, zBounds};
        
        for (var quad : originalQuads) {
            int[] vertices = quad.getVertices()
                                 .clone();
            var face = quad.getDirection();
            
            for (int i = 0; i < 4; i++) {
                int offset = i * 8;
                
                for (int j = 0; j < 3; j++) {
                    float original = Float.intBitsToFloat(vertices[offset + j]);
                    float modified = bounds[j][0] + (original * scale);
                    vertices[offset + j] = Float.floatToRawIntBits(modified);
                }
                
                if (face.getAxis() == Direction.Axis.Z) {
                    calcUV(vertices, quad, offset, scale, xBounds, yBounds, true);
                } else if (face.getAxis() == Direction.Axis.X) {
                    calcUV(vertices, quad, offset, scale, zBounds, yBounds, true);
                } else {
                    calcUV(vertices, quad, offset, scale, xBounds, yBounds, false);
                }
            }
        }
        return newQuads;
    }
    
    private static void calcUV(
            int[] vertices,
            BakedQuad quad,
            int offset,
            float scale,
            float[] uBounds,
            float[] vBounds,
            boolean invertV
    ) {
        
        float u = Float.intBitsToFloat(vertices[offset + 4]);
        float v = Float.intBitsToFloat(vertices[offset + 5]);
        
        float uMin = quad.getSprite()
                         .getU0();
        float uMax = quad.getSprite()
                         .getU1();
        float vMin = quad.getSprite()
                         .getV0();
        float vMax = quad.getSprite()
                         .getV1();
        
        float localU = (u - uMin) / (uMax - uMin);
        float localV = (v - vMin) / (vMax - vMin);
        
        localU = uBounds[0] + (localU * scale);
        if (invertV) {
            localV = 1.0f - (vBounds[0] + (localV * scale));
        } else {
            localV = vBounds[0] + (localV * scale);
        }
        
        vertices[offset + 4] = Float.floatToRawIntBits(uMin + localU * (uMax - uMin));
        vertices[offset + 5] = Float.floatToRawIntBits(vMin + localV * (vMax - vMin));
    }
    
    private static float[] calcUV(
            float[] uBounds,
            float[] vBounds,
            boolean invertV
    ) {
        
        float[] uv = new float[4];
        for (int i = 0; i < 2; i++) {
            uv[(2 * i)] = uBounds[i];
        }
        for (int j = 0; j < 2; j++) {
            var v = vBounds[j];
            if (invertV) {
                v = 16 - v;
            }
            uv[1 + (2 * j)] = v;
        }
        
        return uv;
    }
    
    private static float[] calcAxisBounds(Alignment alignment,
                                          Size size
    ) {
        float scale = size.getThickness();
        float start;
        
        switch (alignment) {
            case NEGATIVE -> start = 0.0f;
            case POSITIVE -> start = 1.0f - scale;
            case CENTER -> start = 0.5f - (scale / 2.0f);
            default -> throw new IllegalArgumentException("What kind of alignment enum is this?");
        }
        return new float[]{start, start + scale};
    }
    
    private static Bounds calcAxisBounds(
            Alignment primary,
            Alignment secondary,
            Direction.Axis axis,
            float scale
    ) {
        final float[] FULL = {0.0f, 1.0f};
        
        switch (axis) {
            case X -> {
                return new Bounds(FULL, calcAxis(primary, scale), calcAxis(secondary, scale));
            }
            case Y -> {
                return new Bounds(calcAxis(primary, scale), FULL, calcAxis(secondary, scale));
            }
            default -> {
                return new Bounds(calcAxis(primary, scale), calcAxis(secondary, scale), FULL);
            }
        }
    }
    
    private static float[] calcAxis(Alignment alignment,
                                    float scale
    ) {
        float start;
        
        switch (alignment) {
            case NEGATIVE -> start = 0.0f;
            case POSITIVE -> start = 1.0f - scale;
            default -> start = 0.5f - (scale / 2.0f);
        }
        return new float[]{start, start + scale};
    }
    
    private static Vector3f getQuadFrom(Bounds bounds,
                                        Direction direction
    ) {
        int xIndex = 0;
        int yIndex = 0;
        int zIndex = 0;
        
        switch (direction) {
            case UP -> yIndex = 1;
            case NORTH -> zIndex = 1;
            case EAST -> xIndex = 1;
        }
        
        return new Vector3f(bounds.x()[xIndex] * 16, bounds.y()[yIndex] * 16, bounds.z()[zIndex] * 16);
        
    }
    
    private static Vector3f getQuadTo(Bounds bounds,
                                      Direction direction
    ) {
        int xIndex = 1;
        int yIndex = 1;
        int zIndex = 1;
        
        switch (direction) {
            case DOWN -> yIndex = 0;
            case SOUTH -> zIndex = 0;
            case WEST -> xIndex = 0;
        }
        
        return new Vector3f(bounds.x()[xIndex] * 16, bounds.y()[yIndex] * 16, bounds.z()[zIndex] * 16);
        
    }
    
    private record Bounds(float[] x, float[] y, float[] z) {}
}
