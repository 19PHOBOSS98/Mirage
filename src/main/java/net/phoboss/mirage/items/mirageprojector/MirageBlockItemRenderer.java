package net.phoboss.mirage.items.mirageprojector;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoItemRenderer;


public class MirageBlockItemRenderer extends GeoItemRenderer<MirageBlockItem> {
    public MirageBlockItemRenderer() {
        super(new MirageBlockItemModel());
    }

    @Override
    public RenderType getRenderType(MirageBlockItem animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getGeoModel().getTextureResource(animatable));
    }

}
