package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import com.github.betterbuiltfool.geometry.Bounds;
import com.github.betterbuiltfool.geometry.CommonGeometry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.function.BiFunction;

public class ProceduralJointModel {
    
    public static @NotNull ArrayList<BakedQuad> generateQuads(
            Direction side,
            RandomSource rand,
            Alignment x,
            Alignment y,
            Alignment z,
            Map<Direction, Size> sizes,
            Map<Direction, BlockState> materials
    ) {
        var dispatcher = Minecraft.getInstance()
                                  .getBlockRenderer();
        var faces = new ArrayList<BakedQuad>();
        
        var centerDirection = getCenterDirection(sizes);
        
        for (var entrySet : sizes.entrySet()) {
            var direction = entrySet.getKey();
            var size = entrySet.getValue();
            var material = materials.get(direction);
            var bounds = calcAxisBounds(x, y, z, size);
            
            BiFunction<Bounds, Direction, Bounds> subpartTransformer =
                    (direction == centerDirection) ?
                    ProceduralJointModel::calcSubpartBoundsWithCenter
                                                   :
                    ProceduralJointModel::calcSubpartBounds;
            
            faces.addAll(generateSubpart(
                    side,
                    rand,
                    dispatcher,
                    subpartTransformer,
                    bounds,
                    direction,
                    material
            ));
        }
        return faces;
    }
    
    private static ArrayList<BakedQuad> generateSubpart(
            Direction side,
            RandomSource rand,
            BlockRenderDispatcher dispatcher,
            BiFunction<Bounds, Direction, Bounds> subpartTransformer,
            Bounds bounds,
            Direction direction,
            BlockState material
    ) {
        var materialModel = dispatcher.getBlockModel(material);
        var faces = new ArrayList<BakedQuad>();
        
        var modifedBounds = subpartTransformer.apply(bounds, direction);
        
        for (var quad : materialModel.getQuads(material, side, rand)) {
            int[] vertices = quad.getVertices()
                                 .clone();
            var facing = quad.getDirection();
            
            // TODO: This doesn't seem to be quite working out as expected.
//            if (facing == direction) {
//                // Opposite of direction will always be facing the center.
//                // The center will always be at least as large as the subpart, so it will never show.
//                continue;
//            }
            for (int i = 0; i < 4; i++) {
                int offset = i * 8;
                
                CommonGeometry.adjustToBounds(vertices, modifedBounds, offset);
                
                // Move adjustUV to CommonGeometry
                adjustUV(quad, facing, vertices, offset, modifedBounds);
            }
            faces.add(new BakedQuad(vertices, quad.getTintIndex(), facing, quad.getSprite(), quad.isShade()));
        }
        return faces;
    }
    
    private static void adjustUV(BakedQuad quad,
                                 Direction facing,
                                 int[] vertices,
                                 int offset,
                                 Bounds bounds
    ) {
        switch (facing.getAxis()) {
            case X -> CommonGeometry.adjustUV(vertices, quad, offset, bounds.z(), bounds.y());
            case Y -> CommonGeometry.adjustUV(vertices, quad, offset, bounds.x(), bounds.z());
            default -> CommonGeometry.adjustUV(vertices, quad, offset, bounds.x(), bounds.y());
        }
    }
    
    private static Bounds calcAxisBounds(
            Alignment x,
            Alignment y,
            Alignment z,
            Size size
    ) {
        float scale = size.getThickness();
        return new Bounds(
                CommonGeometry.calcAxis(x, scale),
                CommonGeometry.calcAxis(y, scale),
                CommonGeometry.calcAxis(z, scale)
        );
    }
    
    private static Bounds calcSubpartBounds(
            Bounds bounds,
            Direction direction
    ) {
        bounds = new Bounds(bounds);
        float[] axisBounds = bounds.get(direction.getAxis());
        
        float lower = axisBounds[0];
        float upper = axisBounds[1];
        
        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            axisBounds[0] = upper;
            axisBounds[1] = 1.0f;
        } else {
            axisBounds[0] = 0;
            axisBounds[1] = lower;
        }
        return bounds;
    }
    
    private static Bounds calcSubpartBoundsWithCenter(
            Bounds bounds,
            Direction direction
    ) {
        bounds = new Bounds(bounds);
        float[] axisBounds = bounds.get(direction.getAxis());
        
        if (direction.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            axisBounds[1] = 1.0f;
        } else {
            axisBounds[0] = 0;
        }
        return bounds;
    }
    
    private static Direction getCenterDirection(Map<Direction, Size> sizes) {
        return sizes
                       .entrySet()
                       .stream()
                       .max(
                               Comparator.<Map.Entry<Direction, Size>> comparingDouble(
                                                 entry -> entry.getValue()
                                                               .getThickness()
                                         )
                                         .thenComparingInt(
                                                 entry -> tiePriority(entry.getKey())
                                         )
                       )
                       .map(Map.Entry::getKey)
                       .orElseThrow(() -> new IllegalArgumentException("Size map must have at least one direction"));
    }
    
    private static int tiePriority(Direction direction) {
        return switch (direction) {
            case DOWN -> 2;
            case UP -> 0;
            default -> 1;
        };
    }
}
