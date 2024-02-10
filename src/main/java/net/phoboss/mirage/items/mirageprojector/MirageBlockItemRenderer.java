package net.phoboss.mirage.items.mirageprojector;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class MirageBlockItemRenderer extends GeoItemRenderer<MirageBlockItem> {
    public MirageBlockItemRenderer() {
        super(new MirageBlockItemModel());
    }

    @Override
    public RenderLayer getRenderType(MirageBlockItem animatable, float partialTick, MatrixStack poseStack, @Nullable VertexConsumerProvider bufferSource, @Nullable VertexConsumer buffer, int packedLight, Identifier texture) {
        return RenderLayer.getEntityTranslucent(getTextureResource(animatable));
    }
}
