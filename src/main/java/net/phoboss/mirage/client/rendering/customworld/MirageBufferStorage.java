package net.phoboss.mirage.client.rendering.customworld;

import com.mojang.blaze3d.vertex.VertexBuffer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;


import java.util.ArrayList;
import java.util.List;


public class MirageBufferStorage {
    public Object2ObjectLinkedOpenHashMap<RenderType, VertexBuffer> mirageVertexBuffers = new Object2ObjectLinkedOpenHashMap<>();

    public MirageBufferStorage() {
        reset();
    }

    public Object2ObjectLinkedOpenHashMap<RenderType, MirageBufferBuilder> getDefaultBuffers(){
        Object2ObjectLinkedOpenHashMap<RenderType, MirageBufferBuilder> map = new Object2ObjectLinkedOpenHashMap<>();
        getDefaultRenderLayers().forEach((renderLayer)->{
            map.put(renderLayer,new MirageBufferBuilder(renderLayer.bufferSize()));
        });
        return map;
    }
    public List<RenderType> getDefaultRenderLayers(){
        List<RenderType> layers = new ArrayList<>();
        layers.add(Sheets.solidBlockSheet());
        layers.add(Sheets.cutoutBlockSheet());
        layers.add(Sheets.bannerSheet());
        layers.add(Sheets.translucentCullBlockSheet());

        layers.add(Sheets.shieldSheet());
        layers.add(Sheets.bedSheet());
        layers.add(Sheets.shulkerBoxSheet());
        layers.add(Sheets.signSheet());
        layers.add(Sheets.chestSheet());

        layers.add(RenderType.translucentNoCrumbling());


        layers.add(RenderType.glint());
        layers.add(RenderType.glintDirect());
        layers.add(RenderType.glintTranslucent());
        layers.add(RenderType.entityGlint());
        layers.add(RenderType.entityGlintDirect());
        layers.add(RenderType.waterMask());

        layers.addAll(RenderType.chunkBufferLayers());
        layers.add(RenderType.entitySolid(Minecraft.getInstance().getPaintingTextures().getBackSprite().atlas().location()));


        layers.add(RenderType.armorGlint());
        layers.add(RenderType.armorEntityGlint());
        return layers;
    }


    public void uploadBufferBuildersToVertexBuffers(MirageImmediate mirageImmediate) {
        mirageImmediate.getLayerBuffers().forEach((renderLayer,bufferBuilder)->{
            if(bufferBuilder.building()){

                if(!this.mirageVertexBuffers.containsKey(renderLayer)){
                    this.mirageVertexBuffers.put(renderLayer, new VertexBuffer());
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
