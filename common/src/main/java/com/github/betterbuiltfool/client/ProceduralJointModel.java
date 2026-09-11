package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
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
            
            BiFunction<CommonGeometry.Bounds, Direction, CommonGeometry.Bounds> subpartTransformer =
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
                    size,
                    material
            ));
        }
        return faces;
    }
    
    private static ArrayList<BakedQuad> generateSubpart(
            Direction side,
            RandomSource rand,
            BlockRenderDispatcher dispatcher,
            BiFunction<CommonGeometry.Bounds, Direction, CommonGeometry.Bounds> subpartTransformer,
            CommonGeometry.Bounds bounds,
            Direction direction,
            Size size,
            BlockState material
    ) {
        var materialModel = dispatcher.getBlockModel(material);
        var scale = size.getThickness();
        var faces = new ArrayList<BakedQuad>();
        
        bounds = subpartTransformer.apply(bounds, direction);
        
        for (var quad : materialModel.getQuads(material, side, rand)) {
            int[] vertices = quad.getVertices()
                                 .clone();
            var facing = quad.getDirection();
            
            if (facing.getOpposite() == direction) {
                // Opposite of direction will always be facing the center.
                // The center will always be at least as large as the subpart, so it will never show.
                continue;
            }
            for (int i = 0; i < 4; i++) {
                int offset = i * 8;
                
                CommonGeometry.adjustBounds(vertices, bounds, offset);
                
                // Move adjustUV to CommonGeometry from ProceduralFrameModel
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
            Alignment x,
            Alignment y,
            Alignment z,
            Size size
    ) {
        float scale = size.getThickness();
        return new CommonGeometry.Bounds(
                CommonGeometry.calcAxis(x, scale),
                CommonGeometry.calcAxis(y, scale),
                CommonGeometry.calcAxis(z, scale)
        );
    }
    
    private static CommonGeometry.Bounds calcSubpartBounds(
            CommonGeometry.Bounds bounds,
            Direction direction
    ) {
        bounds = new CommonGeometry.Bounds(bounds);
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
    
    private static CommonGeometry.Bounds calcSubpartBoundsWithCenter(
            CommonGeometry.Bounds bounds,
            Direction direction
    ) {
        bounds = new CommonGeometry.Bounds(bounds);
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
