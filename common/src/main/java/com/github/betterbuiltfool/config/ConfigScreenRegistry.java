package com.github.betterbuiltfool.config;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class ConfigScreenRegistry {
    @ExpectPlatform
    public static void register() {
        throw new AssertionError("Unreachable");
    }
}
