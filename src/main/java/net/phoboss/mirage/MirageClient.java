package net.phoboss.mirage;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.phoboss.mirage.client.rendering.ModRendering;

@Environment(EnvType.CLIENT)
public class MirageClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModRendering.registerAll();
    }
}
