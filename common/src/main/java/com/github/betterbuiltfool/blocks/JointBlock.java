package com.github.betterbuiltfool.blocks;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import com.github.betterbuiltfool.blocks.block_entities.StructureJointBlockEntity;
import com.github.betterbuiltfool.geometry.CommonGeometry;
import com.github.betterbuiltfool.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class JointBlock extends FrameBlock {
    public static final String BLOCK_ID = "joint_block";
    private static final AtomicInteger CALLS = new AtomicInteger();
    private static int shapeBuilds = 0;
    private final Map<BlockState, VoxelShape> shapeCache = new HashMap<>();
    
    // Represents connections. Size.NONE means unconnected.
    public static final EnumProperty<Size> NORTH = EnumProperty.create("north", Size.class);
    public static final EnumProperty<Size> SOUTH = EnumProperty.create("south", Size.class);
    public static final EnumProperty<Size> EAST = EnumProperty.create("east", Size.class);
    public static final EnumProperty<Size> WEST = EnumProperty.create("west", Size.class);
    public static final EnumProperty<Size> UP = EnumProperty.create("up", Size.class);
    public static final EnumProperty<Size> DOWN = EnumProperty.create("down", Size.class);
    
    public static final EnumProperty<Alignment> ALIGNMENT_TERTIARY =
            EnumProperty.create("alignment_tertiary", Alignment.class);
    
    private final Map<Direction, EnumProperty<Size>> connectionProperties = Map.of(
            Direction.NORTH, NORTH,
            Direction.SOUTH, SOUTH,
            Direction.EAST, EAST,
            Direction.WEST, WEST,
            Direction.UP, UP,
            Direction.DOWN, DOWN
    );
    
    public JointBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition()
                                      .any()
                                      .setValue(NORTH, Size.NONE)
                                      .setValue(SOUTH, Size.NONE)
                                      .setValue(EAST, Size.NONE)
                                      .setValue(WEST, Size.NONE)
                                      .setValue(UP, Size.NONE)
                                      .setValue(DOWN, Size.NONE)
                                      .setValue(ALIGNMENT_TERTIARY, Alignment.CENTER)
        );
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos,
                                                BlockState state
    ) {
        return new StructureJointBlockEntity(BlockEntityRegistry.JOINT_ENTITY.get(), pos, state);
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, ALIGNMENT_TERTIARY);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getShape(BlockState state,
                                        BlockGetter level,
                                        BlockPos pos,
                                        CollisionContext context
    ) {
        return shapeCache.computeIfAbsent(state, this::calcShape);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public @NotNull VoxelShape getOcclusionShape(BlockState state,
                                                 BlockGetter level,
                                                 BlockPos pos
    ) {
        return shapeCache.computeIfAbsent(state, this::calcShape);
    }
    
    
    @Override
    @SuppressWarnings("deprecation")
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }
    
    private VoxelShape calcShape(BlockState state) {
        Alignment[] alignments = new Alignment[]{
                state.getValue(ALIGNMENT_PRIMARY),
                state.getValue(ALIGNMENT_SECONDARY),
                state.getValue(ALIGNMENT_TERTIARY)
        };
        var sizeMap = getConnectionSizes(state);
        
        Size size = getMaxSize(sizeMap);
        
        VoxelShape shape = calcJointCenter(alignments[0], alignments[1], alignments[2], size);
        
        for (var entrySet : sizeMap.entrySet()) {
            var direction = entrySet.getKey();
            var connectionSize = entrySet.getValue();
            shape = Shapes.or(shape,
                              calcSubpartShape(direction, connectionSize, alignments[0], alignments[1], alignments[2])
            );
        }
        
        return shape;
    }
    
    private Map<Direction, Size> getConnectionSizes(BlockState state) {
        var sizeMap = new HashMap<Direction, Size>();
        
        for (var entrySet : connectionProperties.entrySet()) {
            var connectionProperty = entrySet.getValue();
            Size connectionSize = state.getValue(connectionProperty);
            if (connectionSize == Size.NONE) {
                continue;
            }
            sizeMap.put(entrySet.getKey(), connectionSize);
        }
        return sizeMap;
    }
    
    private Size getMaxSize(Map<Direction, Size> sizeMap) {
        if (sizeMap.isEmpty()) {
            return Size.FULL;
        }
        Size largest = Size.NONE;
        float largestScale = 0.0f;
        for (var size : sizeMap.values()) {
            float scale = size.getThickness();
            if (scale > largestScale) {
                largest = size;
                largestScale = scale;
            }
        }
        return largest;
    }
    
    private VoxelShape calcJointCenter(Alignment primary,
                                       Alignment secondary,
                                       Alignment tertiary,
                                       Size size
    ) {
        float scale = size.getThickness();
        
        float[] x = CommonGeometry.calcAxis(primary, scale);
        float[] y = CommonGeometry.calcAxis(secondary, scale);
        float[] z = CommonGeometry.calcAxis(tertiary, scale);
        return Shapes.box(
                x[0],
                y[0],
                z[0],
                x[1],
                y[1],
                z[1]
        );
    }
    
    private VoxelShape calcSubpartShape(Direction facing,
                                        Size size,
                                        Alignment primary,
                                        Alignment secondary,
                                        Alignment tertiary
    ) {
        var scale = size.getThickness();
        Map<Direction.Axis, float[]> bounds = new EnumMap<>(Direction.Axis.class);
        
        bounds.put(Direction.Axis.X, CommonGeometry.calcAxis(primary, scale));
        bounds.put(Direction.Axis.Y, CommonGeometry.calcAxis(secondary, scale));
        bounds.put(Direction.Axis.Z, CommonGeometry.calcAxis(tertiary, scale));
        
        float[] axisBounds = bounds.get(facing.getAxis());
        
        float lower = axisBounds[0];
        float upper = axisBounds[1];
        
        if (facing.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            axisBounds[0] = upper;
            axisBounds[1] = 1.0f;
        } else {
            axisBounds[0] = 0;
            axisBounds[1] = lower;
        }
        
        return Shapes.box(
                bounds.get(Direction.Axis.X)[0],
                bounds.get(Direction.Axis.Y)[0],
                bounds.get(Direction.Axis.Z)[0],
                bounds.get(Direction.Axis.X)[1],
                bounds.get(Direction.Axis.Y)[1],
                bounds.get(Direction.Axis.Z)[1]
        );
    }
}
