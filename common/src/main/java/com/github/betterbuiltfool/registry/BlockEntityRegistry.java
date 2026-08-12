package com.github.betterbuiltfool.registry;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.blocks.block_entities.StructureJointBlockEntity;
import com.github.betterbuiltfool.blocks.block_entities.StructureMemberBlockEntity;
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
    
    public static RegistrySupplier<BlockEntityType<StructureJointBlockEntity>> JOINT_ENTITY;
    public static RegistrySupplier<BlockEntityType<StructureMemberBlockEntity>> MEMBER_ENTITY;
    
    public static void register() {
        JOINT_ENTITY = register("joint_entity", () -> BlockEntityType.Builder.of(
                                                                             (blockPos, blockState) -> new StructureJointBlockEntity(JOINT_ENTITY.get(), blockPos, blockState),
                                                                             BlockRegistry.JOINT_BLOCK.get()
                                                                     )
                                                                             .build(null)
        );
        MEMBER_ENTITY = register("member_entity", () -> BlockEntityType.Builder.of(
                                                                              (blockPos, blockState) -> new StructureMemberBlockEntity(JOINT_ENTITY.get(), blockPos, blockState),
                                                                              BlockRegistry.BEAM_BLOCK.get(),
                                                                              BlockRegistry.POST_BLOCK.get()
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
