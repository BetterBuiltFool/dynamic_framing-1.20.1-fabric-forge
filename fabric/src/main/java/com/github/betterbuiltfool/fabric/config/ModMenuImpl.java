package com.github.betterbuiltfool.fabric.config;

import com.github.betterbuiltfool.config.CommonConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuImpl implements ModMenuApi {
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> CommonConfig.createGui(parent)
                                     .build();
    }
}
