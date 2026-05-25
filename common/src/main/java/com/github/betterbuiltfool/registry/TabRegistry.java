package com.github.betterbuiltfool.registry;

import com.github.betterbuiltfool.DynamicFraming;
import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class TabRegistry {
    
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
            DynamicFraming.MOD_ID,
            Registries.CREATIVE_MODE_TAB
    );
    
    public static RegistrySupplier<CreativeModeTab> FRAMING_TOOLS;
    
    public static void register(){
        FRAMING_TOOLS = TABS.register(
                "framing_tools",
                () -> CreativeTabRegistry.create(
                        Component.translatable("category.framing_tools_tab"),
                        () -> new ItemStack(ItemRegistry.FROE.get())
                )
        );
        
        TABS.register();
    }
}
