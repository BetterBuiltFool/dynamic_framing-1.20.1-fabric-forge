package com.github.betterbuiltfool.registry;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.blocks.BeamBlock;
import com.github.betterbuiltfool.blocks.JointBlock;
import com.github.betterbuiltfool.blocks.PostBlock;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            DynamicFraming.MOD_ID, Registries.BLOCK
    );
    
    public static RegistrySupplier<Block> POST_BLOCK;
    public static RegistrySupplier<Block> BEAM_BLOCK;
    public static RegistrySupplier<Block> JOINT_BLOCK;
    
    public static void register() {
        DynamicFraming.LOGGER.info("Registering blocks");
        POST_BLOCK = register(
                PostBlock.BLOCK_ID,
                () -> new PostBlock(BlockBehaviour.Properties.of())
        );
        
        BEAM_BLOCK = register(
                BeamBlock.BLOCK_ID,
                () -> new BeamBlock(BlockBehaviour.Properties.of())
        );
        
        JOINT_BLOCK = register(
                JointBlock.BLOCK_ID,
                () -> new JointBlock(BlockBehaviour.Properties.of())
        );
        BLOCKS.register();
    }
    
    public static RegistrySupplier<Block> register(String name,
                                                   Supplier<Block> block
    ) {
        DynamicFraming.LOGGER.info("Registering block '{}'", name);
        return BLOCKS.register(new ResourceLocation(DynamicFraming.MOD_ID, name), block);
    }
}
