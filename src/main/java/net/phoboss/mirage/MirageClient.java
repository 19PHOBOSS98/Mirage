package net.phoboss.mirage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.phoboss.mirage.client.rendering.ModRendering;
import net.phoboss.mirage.network.MirageNBTPacketHandler;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.Executors;

@Environment(EnvType.CLIENT)
public class MirageClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModRendering.registerAll();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            System.gc();
            Mirage.CLIENT_THREAD_POOL = Executors.newFixedThreadPool(2,new BasicThreadFactory.Builder()
                    .namingPattern("ClientMirageLoader-%d")
                    .priority(Thread.MAX_PRIORITY)
                    .build());
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            Mirage.CLIENT_THREAD_POOL.shutdownNow();
        });

        MirageNBTPacketHandler.registerS2CPackets();
    }
}
