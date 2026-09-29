package com.github.betterbuiltfool.registry;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.blocks.block_entities.BeamBlockEntity;
import com.github.betterbuiltfool.blocks.block_entities.JointBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            DynamicFraming.MOD_ID, Registries.BLOCK_ENTITY_TYPE
    );
    
    public static RegistrySupplier<BlockEntityType<JointBlockEntity>> JOINT_ENTITY;
    public static RegistrySupplier<BlockEntityType<BeamBlockEntity>> MEMBER_ENTITY;
    
    public static void register() {
        JOINT_ENTITY = register("joint_entity", () -> BlockEntityType.Builder.of(
                                                                             (blockPos, blockState) -> new JointBlockEntity(JOINT_ENTITY.get(), blockPos, blockState),
                                                                             BlockRegistry.JOINT_BLOCK.get()
                                                                     )
                                                                             .build(null)
        );
        MEMBER_ENTITY = register("member_entity", () -> BlockEntityType.Builder.of(
                                                                               (blockPos, blockState) -> new BeamBlockEntity(MEMBER_ENTITY.get(), blockPos, blockState),
                                                                              BlockRegistry.BEAM_BLOCK.get()
                                                                      )
                                                                              .build(null)
        );
        BLOCK_ENTITIES.register();
    }
    
    
    public static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String name,
                                                                                        Supplier<BlockEntityType<T>> blockEntity
    ) {
        DynamicFraming.LOGGER.info("Registering block '{}'", name);
        return BLOCK_ENTITIES.register(new ResourceLocation(DynamicFraming.MOD_ID, name), blockEntity);
    }
}
