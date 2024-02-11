package net.phoboss.mirage.blocks.mirageprojector;

import net.minecraft.resources.ResourceLocation;
import net.phoboss.mirage.Mirage;
import software.bernie.geckolib.model.GeoModel;

public class MirageBlockModel extends GeoModel<MirageBlockEntity> {
    @Override
    public ResourceLocation getModelResource(MirageBlockEntity object) {
        return new ResourceLocation(Mirage.MOD_ID,"geo/zoetrope.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MirageBlockEntity object) {
        return new ResourceLocation(Mirage.MOD_ID,"textures/block/zoetrope.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MirageBlockEntity animatable) {
        return new ResourceLocation(Mirage.MOD_ID,"animations/zoetrope.animation.json");
    }

}
