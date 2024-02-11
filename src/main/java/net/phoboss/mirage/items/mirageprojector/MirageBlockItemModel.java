package net.phoboss.mirage.items.mirageprojector;

import net.minecraft.resources.ResourceLocation;
import net.phoboss.mirage.Mirage;
import software.bernie.geckolib.model.GeoModel;

public class MirageBlockItemModel extends GeoModel<MirageBlockItem> {
    @Override
    public ResourceLocation getModelResource(MirageBlockItem object) {
        return new ResourceLocation(Mirage.MOD_ID,"geo/zoetrope.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MirageBlockItem object) {
        return new ResourceLocation(Mirage.MOD_ID,"textures/block/zoetrope.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MirageBlockItem animatable) {
        return new ResourceLocation(Mirage.MOD_ID,"animations/zoetrope.animation.json");
    }
}
