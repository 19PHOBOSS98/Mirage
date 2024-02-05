package net.phoboss.mirage.event;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.ModBlockEntities;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntityRenderer;
import net.phoboss.mirage.client.rendering.ModRendering;

@Mod.EventBusSubscriber(modid = Mirage.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventClientBusEvents {
    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event){
        ModRendering.registerBlockEntityRenderers(event);
    }
}
