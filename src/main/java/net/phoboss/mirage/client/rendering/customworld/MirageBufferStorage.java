package net.phoboss.mirage.client.rendering.customworld;

import com.mojang.blaze3d.vertex.VertexBuffer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;


import java.util.ArrayList;
import java.util.List;


public class MirageBufferStorage {
    public Object2ObjectLinkedOpenHashMap<RenderType, VertexBuffer> mirageVertexBuffers = new Object2ObjectLinkedOpenHashMap<>();;

    private static final List<RenderType> DEFAULT_RENDER_LAYERS = Util.make(new ArrayList<>(), (layers) -> {
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
    });
    public Object2ObjectLinkedOpenHashMap<RenderType, MirageBufferBuilder> mirageBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (map) -> {
        for(RenderType renderLayer : DEFAULT_RENDER_LAYERS){
            map.put(renderLayer,new MirageBufferBuilder(renderLayer.bufferSize()));
        }
    });

    public MirageImmediate mirageImmediate = new MirageImmediate(mirageBuffers);


    public void uploadBufferBuildersToVertexBuffers(MirageImmediate mirageImmediate) {
        mirageImmediate.getLayerBuffers().forEach((renderLayer,bufferBuilder)->{
            if(bufferBuilder.building()){
                bufferBuilder.end();
                if(!this.mirageVertexBuffers.containsKey(renderLayer)){
                    this.mirageVertexBuffers.put(renderLayer, new VertexBuffer());
                }
                this.mirageVertexBuffers.get(renderLayer).upload(bufferBuilder);
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
