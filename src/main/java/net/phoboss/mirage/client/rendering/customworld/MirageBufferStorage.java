package net.phoboss.mirage.client.rendering.customworld;

import com.mojang.blaze3d.vertex.VertexBuffer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;


import java.util.ArrayList;
import java.util.List;


public class MirageBufferStorage {
    public Object2ObjectLinkedOpenHashMap<RenderType, VertexBuffer> mirageVertexBuffers;

    public static List<RenderType> DEFAULT_RENDER_LAYERS = getDefaultRenderLayers();

    public MirageImmediate mirageImmediate;

    public MirageBufferStorage() {
        mirageVertexBuffers = new Object2ObjectLinkedOpenHashMap<>();
        mirageImmediate = new MirageImmediate(getDefaultBuffers());
    }

    public Object2ObjectLinkedOpenHashMap<RenderType, MirageBufferBuilder> getDefaultBuffers(){
        Object2ObjectLinkedOpenHashMap<RenderType, MirageBufferBuilder> map = new Object2ObjectLinkedOpenHashMap<>();
        for(RenderType renderLayer : DEFAULT_RENDER_LAYERS){
            map.put(renderLayer,new MirageBufferBuilder(renderLayer.bufferSize()));
        }
        return map;
    }
    public static List<RenderType> getDefaultRenderLayers(){
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
        layers.add(RenderType.entitySolid(Minecraft.getInstance().getPaintingTextures().getBackSprite().atlasLocation()));


        layers.add(RenderType.armorGlint());
        layers.add(RenderType.armorEntityGlint());
        return layers;
    }


    public void uploadBufferBuildersToVertexBuffers(MirageImmediate mirageImmediate) {
        mirageImmediate.getLayerBuffers().forEach((renderLayer,bufferBuilder)->{
            if(bufferBuilder.building()){

                if(!this.mirageVertexBuffers.containsKey(renderLayer)){
                    this.mirageVertexBuffers.put(renderLayer, new VertexBuffer(VertexBuffer.Usage.STATIC));
                }
                this.mirageVertexBuffers.get(renderLayer).bind();
                this.mirageVertexBuffers.get(renderLayer).upload(bufferBuilder.end());
            }
        });

    }

    public void reset() {
        this.mirageVertexBuffers.forEach(((renderType, vertexBuffer) -> {
            vertexBuffer.close();//Should only be called from "renderThread"
        }));
    }

    public MirageImmediate getMirageImmediate(){
        mirageImmediate.reset();
        return mirageImmediate;
    }
}
