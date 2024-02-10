package net.phoboss.mirage.client.rendering.customworld;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;

import java.util.ArrayList;
import java.util.List;

public class MirageBufferStorage {
    public Object2ObjectLinkedOpenHashMap<RenderLayer, VertexBuffer> mirageVertexBuffers = new Object2ObjectLinkedOpenHashMap<>();

    public MirageBufferStorage() {
        reset();
    }

    public Object2ObjectLinkedOpenHashMap<RenderLayer, MirageBufferBuilder> getDefaultBuffers(){
        Object2ObjectLinkedOpenHashMap<RenderLayer, MirageBufferBuilder> map = new Object2ObjectLinkedOpenHashMap<>();
        getDefaultRenderLayers().forEach((renderLayer)->{
            map.put(renderLayer,new MirageBufferBuilder(renderLayer.getExpectedBufferSize()));
        });
        return map;
    }
    public List<RenderLayer> getDefaultRenderLayers(){
        List<RenderLayer> layers = new ArrayList<>();
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
        layers.add(RenderLayer.getEntitySolid(MinecraftClient.getInstance().getPaintingManager().getBackSprite().getAtlasId()));


        layers.add(RenderLayer.getArmorGlint());
        layers.add(RenderLayer.getArmorEntityGlint());
        return layers;
    }


    public void uploadBufferBuildersToVertexBuffers(MirageImmediate mirageImmediate) {
        mirageImmediate.getLayerBuffers().forEach((renderLayer,bufferBuilder)->{
            if(bufferBuilder.isBuilding()){
                if(!this.mirageVertexBuffers.containsKey(renderLayer)){
                    this.mirageVertexBuffers.put(renderLayer, new VertexBuffer(VertexBuffer.Usage.STATIC));
                }
                this.mirageVertexBuffers.get(renderLayer).bind();
                this.mirageVertexBuffers.get(renderLayer).upload(bufferBuilder.end());
            }
        });

    }

    public void reset() {
        this.mirageVertexBuffers = new Object2ObjectLinkedOpenHashMap<>();
    }

    public MirageImmediate getMirageImmediate(){
        return new MirageImmediate(getDefaultBuffers());
    }
}
