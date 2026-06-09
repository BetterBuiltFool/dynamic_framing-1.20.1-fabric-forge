package com.github.betterbuiltfool.forge.client;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.ui.NodeViewOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DynamicFraming.MOD_ID, value = Dist.CLIENT)
public class GuiOverlayForge {
    
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        NodeViewOverlay.renderOverlay(event.getPoseStack());
    }
}
