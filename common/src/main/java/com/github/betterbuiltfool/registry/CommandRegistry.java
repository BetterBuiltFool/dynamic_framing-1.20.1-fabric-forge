package com.github.betterbuiltfool.registry;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.commands.ClearAllCommand;
import com.github.betterbuiltfool.commands.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.ArrayList;
import java.util.List;

public class CommandRegistry {
    private static final List<Command> COMMANDS = new ArrayList<>();
    
    static {
        COMMANDS.add(new ClearAllCommand());
    }
    
    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(DynamicFraming.MOD_ID);
            
            for (var subcommand:COMMANDS) {
                LiteralArgumentBuilder<CommandSourceStack> subBuilder = Commands.literal(subcommand.getName())
                                                                                .requires(source -> source.hasPermission(
                                                                                        subcommand.getPermissionLevel()))
                                                                                .executes(subcommand::execute);
                subcommand.build(subBuilder);
                root.then(subBuilder);
            }
            dispatcher.register(root);
        });
    }
}
