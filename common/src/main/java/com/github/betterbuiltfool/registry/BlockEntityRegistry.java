package com.github.betterbuiltfool.registry;

import com.github.betterbuiltfool.DynamicFraming;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            DynamicFraming.MOD_ID, Registries.BLOCK_ENTITY_TYPE
    );
    
    public static void register() {
    
    }
}
