package com.github.betterbuiltfool.registry;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.items.FroeTool;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            DynamicFraming.MOD_ID,
            Registries.ITEM
    );
    
    public static RegistrySupplier<Item> FROE;
    
    public static void register() {
        DynamicFraming.LOGGER.info("Registering items");
        FROE = register(
                FroeTool.ITEM_ID,
                () -> new FroeTool(new Item.Properties().arch$tab(TabRegistry.FRAMING_TOOLS))
        );
        
        ITEMS.register();
    }
    
    public static RegistrySupplier<Item> register(String name, Supplier<Item> item) {
        DynamicFraming.LOGGER.info("Registering item '{}'", name);
        return ITEMS.register(new ResourceLocation(DynamicFraming.MOD_ID, name), item);
    }
}
