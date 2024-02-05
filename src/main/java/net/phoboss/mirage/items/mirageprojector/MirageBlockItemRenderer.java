package net.phoboss.mirage.items.mirageprojector;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

public class MirageBlockItemRenderer extends GeoItemRenderer<MirageBlockItem> {
    public MirageBlockItemRenderer() {
        super(new MirageBlockItemModel());
    }

    @Override//BlockItem has translucent parts too :)
    public RenderType getRenderType(MirageBlockItem animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

}
