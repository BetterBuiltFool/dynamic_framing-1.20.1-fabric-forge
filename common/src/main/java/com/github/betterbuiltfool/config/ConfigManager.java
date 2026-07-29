package com.github.betterbuiltfool.config;

import com.github.betterbuiltfool.DynamicFraming;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;

import java.awt.*;
import java.io.*;

public class ConfigManager {
    public static final Gson GSON = new GsonBuilder()
                                            .setPrettyPrinting()
                                            .create();
    private static final File CONFIG_FILE = Platform.getConfigFolder().resolve(
            DynamicFraming.MOD_ID + ".json"
    ).toFile();
    
    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }
        
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData temp = GSON.fromJson(reader, ConfigData.class);
            if (temp == null) return;
            CommonConfig.unpack(temp);
        
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    
    public static void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            
            ConfigData temp = CommonConfig.pack();
            
            try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
                GSON.toJson(temp, writer);
            }
        
        } catch (IOException exception){
            exception.printStackTrace();
        }
    }
}
