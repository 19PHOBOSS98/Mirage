package net.phoboss.mirage.client.rendering.customworld;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;


/*
this class is fed into renderers to collect the vertices to be drawn.
 */

public class MirageImmediate implements MultiBufferSource {
    private Object2ObjectLinkedOpenHashMap<RenderType, MirageBufferBuilder> layerBuffers;
    public MirageImmediate() {
        layerBuffers = new Object2ObjectLinkedOpenHashMap<>();
    }
    public MirageImmediate(Object2ObjectLinkedOpenHashMap<RenderType, MirageBufferBuilder> defaultBuffers) {
        layerBuffers = defaultBuffers;
    }
    private BlockPos actualPos = new BlockPos(0,0,0);
    public void setActualPos(BlockPos actualPos) {
        this.actualPos = actualPos;
    }
    @Override
    public VertexConsumer getBuffer(RenderType renderLayer) {
        if(renderLayer == RenderType.lines()){
            BufferBuilder sink = new MirageBufferBuilder(RenderType.lines().bufferSize());
            sink.begin(renderLayer.mode(), renderLayer.format());
            return sink;//don't render lines, they somehow glitch-out the mirage; BlockEntity renders start to float in camera
        }
        if(!layerBuffers.containsKey(renderLayer)){
            layerBuffers.put(renderLayer, new MirageBufferBuilder(renderLayer.bufferSize()));
        }
        MirageBufferBuilder mirageBufferBuilder = layerBuffers.get(renderLayer);
        mirageBufferBuilder.setActualPos(actualPos);

        if(!mirageBufferBuilder.building()){
            mirageBufferBuilder.begin(renderLayer.mode(), renderLayer.format());
        }
        return mirageBufferBuilder;
    }

    public Object2ObjectLinkedOpenHashMap<RenderType, MirageBufferBuilder> getLayerBuffers(){
        return layerBuffers;
    }

    public void reset(){
        layerBuffers.forEach(((renderType, mirageBufferBuilder) -> {
            mirageBufferBuilder.clear();
        }));
    }
}
