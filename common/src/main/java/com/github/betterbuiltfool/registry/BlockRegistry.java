package com.github.betterbuiltfool.registry;

import com.github.betterbuiltfool.DynamicFraming;
import dev.architectury.registry.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            DynamicFraming.MOD_ID, Registries.BLOCK
    );
    
    public static void register() {
    
    }
}
