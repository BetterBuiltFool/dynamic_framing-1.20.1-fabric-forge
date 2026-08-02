package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.blocks.FrameBlock;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.sounds.SoundSource;

public class SoundEventHook {
    
    public static void registerHooks() {
        InteractionEvent.LEFT_CLICK_BLOCK.register(((player, hand, pos, face) -> {
            var level = player.level();
            var state = level.getBlockState(pos);
            
            if (!(state.getBlock() instanceof FrameBlock frameBlock)) {
                return EventResult.pass();
            }
            var composedMaterial = frameBlock.getComposedMaterial(state, level, pos);
            if (composedMaterial == null) {
                return EventResult.pass();
            }
            
            var soundType = composedMaterial.getSoundType();
            
            level.playSound(player, pos, soundType.getHitSound(), SoundSource.BLOCKS,
                            soundType.getVolume(), soundType.getPitch()
            );
            return EventResult.interruptTrue();
        }
                                                   ));
        
        BlockEvent.BREAK.register(((level, pos, state, player, xp) -> {
            if (!(state.getBlock() instanceof FrameBlock frameBlock)) {
                return EventResult.pass();
            }
            var composedMaterial = frameBlock.getComposedMaterial(state, level, pos);
            if (composedMaterial == null) {
                return EventResult.pass();
            }
            
            var soundType = composedMaterial.getSoundType();
            
            level.playSound(player, pos, soundType.getBreakSound(), SoundSource.BLOCKS,
                            soundType.getVolume(), soundType.getPitch()
            );
            return EventResult.interruptTrue();
            
        }
                                  ));
    }
}
