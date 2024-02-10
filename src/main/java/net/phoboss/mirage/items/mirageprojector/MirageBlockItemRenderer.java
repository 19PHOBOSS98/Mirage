package net.phoboss.mirage.items.mirageprojector;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class MirageBlockItemRenderer extends GeoItemRenderer<MirageBlockItem> {
    public MirageBlockItemRenderer() {
        super(new MirageBlockItemModel());
    }


    /*@Override
    public RenderLayer getRenderType(MirageBlockItem animatable, float partialTick, MatrixStack poseStack, @Nullable VertexConsumerProvider bufferSource, @Nullable VertexConsumer buffer, int packedLight, Identifier texture) {
        return RenderLayer.getEntityTranslucent(getTextureResource(animatable));
    }*/
    @Override
    public RenderLayer getRenderType(MirageBlockItem animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick) {
        return RenderLayer.getEntityTranslucent(getGeoModel().getTextureResource(animatable));
    }
}
