package net.phoboss.mirage.client.rendering.customworld;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;

/*
this class is fed into renderers to collect the vertices to be drawn.
 */

public class MirageImmediate implements VertexConsumerProvider {
    private Object2ObjectLinkedOpenHashMap<RenderLayer, MirageBufferBuilder> layerBuffers;
    public MirageImmediate() {
        layerBuffers = new Object2ObjectLinkedOpenHashMap<>();
    }
    public MirageImmediate(Object2ObjectLinkedOpenHashMap<RenderLayer, MirageBufferBuilder> defaultBuffers) {
        layerBuffers = defaultBuffers;
    }
    private BlockPos actualPos = new BlockPos(0,0,0);
    public void setActualPos(BlockPos actualPos) {
        this.actualPos = actualPos;
    }
    @Override
    public VertexConsumer getBuffer(RenderLayer renderLayer) {
        if(renderLayer == RenderLayer.getLines()){
            BufferBuilder sink = new MirageBufferBuilder(RenderLayer.getLines().getExpectedBufferSize());
            sink.begin(renderLayer.getDrawMode(), renderLayer.getVertexFormat());
            return sink;//don't render lines, they somehow glitch-out the mirage; BlockEntity renders start to float in camera
        }
        if(!layerBuffers.containsKey(renderLayer)){
            layerBuffers.put(renderLayer, new MirageBufferBuilder(renderLayer.getExpectedBufferSize()));
        }
        MirageBufferBuilder mirageBufferBuilder = layerBuffers.get(renderLayer);
        mirageBufferBuilder.setActualPos(actualPos);

        if(!mirageBufferBuilder.isBuilding()){
            mirageBufferBuilder.begin(renderLayer.getDrawMode(), renderLayer.getVertexFormat());
        }
        return mirageBufferBuilder;
    }

    public Object2ObjectLinkedOpenHashMap<RenderLayer, MirageBufferBuilder> getLayerBuffers(){
        return layerBuffers;
    }

    public void reset(){
        layerBuffers.forEach(((renderType, mirageBufferBuilder) -> {
            mirageBufferBuilder.clear();
        }));
    }
}
