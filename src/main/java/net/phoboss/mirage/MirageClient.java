package net.phoboss.mirage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.phoboss.mirage.client.rendering.ModRendering;

@Environment(EnvType.CLIENT)
public class MirageClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModRendering.registerAll();
        ClientTickEvents.START_WORLD_TICK.register((client) -> {
            System.gc();
        });
        ClientTickEvents.END_WORLD_TICK.register((client) -> {
            for(Thread thread:Thread.getAllStackTraces().keySet()){
                if(thread.getName().equals("MirageLoader") && !thread.isInterrupted()){
                    thread.interrupt();
                }
            }
        });
    }

}
