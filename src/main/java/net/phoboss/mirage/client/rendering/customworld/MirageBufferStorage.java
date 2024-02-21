package net.phoboss.mirage.client.rendering.customworld;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MirageBufferStorage {
    public Object2ObjectLinkedOpenHashMap<RenderLayer, VertexBuffer> mirageVertexBuffers = new Object2ObjectLinkedOpenHashMap<>();

    private static final List<RenderLayer> DEFAULT_RENDER_LAYERS = Util.make(new ArrayList<>(), (layers) -> {
        layers.add(TexturedRenderLayers.getEntitySolid());
        layers.add(TexturedRenderLayers.getEntityCutout());
        layers.add(TexturedRenderLayers.getBannerPatterns());
        layers.add(TexturedRenderLayers.getEntityTranslucentCull());

        layers.add(TexturedRenderLayers.getShieldPatterns());
        layers.add(TexturedRenderLayers.getBeds());
        layers.add(TexturedRenderLayers.getShulkerBoxes());
        layers.add(TexturedRenderLayers.getSign());
        layers.add(TexturedRenderLayers.getChest());

        layers.add(RenderLayer.getTranslucentNoCrumbling());


        layers.add(RenderLayer.getGlint());
        layers.add(RenderLayer.getDirectGlint());
        layers.add(RenderLayer.getGlintTranslucent());
        layers.add(RenderLayer.getEntityGlint());
        layers.add(RenderLayer.getDirectEntityGlint());
        layers.add(RenderLayer.getWaterMask());

        layers.addAll(RenderLayer.getBlockLayers());
        layers.add(RenderLayer.getEntitySolid(MinecraftClient.getInstance().getPaintingManager().getBackSprite().getAtlas().getId()));


        layers.add(RenderLayer.getArmorGlint());
        layers.add(RenderLayer.getArmorEntityGlint());
    });
    public Object2ObjectLinkedOpenHashMap<RenderLayer, MirageBufferBuilder> mirageBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
        for(RenderLayer renderLayer : DEFAULT_RENDER_LAYERS){
            map.put(renderLayer,new MirageBufferBuilder(renderLayer.getExpectedBufferSize()));
        }
    });

    public MirageImmediate mirageImmediate = new MirageImmediate(mirageBuffers);


    public void uploadBufferBuildersToVertexBuffers(MirageImmediate mirageImmediate) {
        mirageImmediate.getLayerBuffers().forEach((renderLayer,bufferBuilder)->{
            if(bufferBuilder.isBuilding()){
                if(!this.mirageVertexBuffers.containsKey(renderLayer)){
                    this.mirageVertexBuffers.put(renderLayer, new VertexBuffer());
                }
                this.mirageVertexBuffers.get(renderLayer).bind();
                this.mirageVertexBuffers.get(renderLayer).upload(bufferBuilder.end());
            }
        });

    }

    public void reset() {//Should only be called from "renderThread"
        resetMirageImmediateBuffers();
        resetVertexBuffers();
    }
    public void resetMirageImmediateBuffers() {
        this.mirageImmediate.reset();
    }
    public void resetVertexBuffers() {
        this.mirageVertexBuffers.forEach(((renderType, vertexBuffer) -> {
            vertexBuffer.close();//Should only be called from "renderThread"
        }));
    }

    public MirageImmediate getMirageImmediate(){
        resetMirageImmediateBuffers();
        return this.mirageImmediate;
    }
}
