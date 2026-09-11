package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import com.github.betterbuiltfool.geometry.CommonGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ProceduralFrameModel {
    
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
        var faces = new ArrayList<BakedQuad>();
        
        for (var quad : materialModel.getQuads(material, side, rand)) {
            int[] vertices = quad.getVertices()
                                 .clone();
            var facing = quad.getDirection();
            
            for (int i = 0; i < 4; i++) {
                int offset = i * 8;
                
                CommonGeometry.adjustBounds(vertices, bounds, offset);
                
                switch (facing.getAxis()) {
                    case X -> CommonGeometry.adjustUV(vertices, quad, offset, scale, bounds.z(), bounds.y(), true);
                    case Y -> CommonGeometry.adjustUV(vertices, quad, offset, scale, bounds.x(), bounds.z(), false);
                    default -> CommonGeometry.adjustUV(vertices, quad, offset, scale, bounds.x(), bounds.y(), true);
                }
            }
            faces.add(new BakedQuad(vertices, quad.getTintIndex(), facing, quad.getSprite(), quad.isShade()));
        }
        return faces;
    }
    
    private static CommonGeometry.Bounds calcAxisBounds(
            Alignment primary,
            Alignment secondary,
            Direction.Axis axis,
            float scale
    ) {
        final float[] FULL = {0.0f, 1.0f};
        
        switch (axis) {
            case X -> {
                return new CommonGeometry.Bounds(FULL, CommonGeometry.calcAxis(primary, scale),
                                  CommonGeometry.calcAxis(secondary, scale)
                );
            }
            case Y -> {
                return new CommonGeometry.Bounds(CommonGeometry.calcAxis(primary, scale), FULL,
                                  CommonGeometry.calcAxis(secondary, scale)
                );
            }
            default -> {
                return new CommonGeometry.Bounds(CommonGeometry.calcAxis(primary, scale), CommonGeometry.calcAxis(secondary, scale),
                                                 FULL
                );
            }
        }
    }
}
