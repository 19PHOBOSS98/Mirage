package net.phoboss.mirage.items.mirageprojector;

import net.minecraft.resources.ResourceLocation;
import net.phoboss.mirage.Mirage;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class MirageBlockItemModel extends AnimatedGeoModel<MirageBlockItem> {
    @Override
    public ResourceLocation getModelLocation(MirageBlockItem object) {
        return new ResourceLocation(Mirage.MOD_ID,"geo/zoetrope.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(MirageBlockItem object) {
        return new ResourceLocation(Mirage.MOD_ID,"textures/block/zoetrope.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(MirageBlockItem animatable) {
        return new ResourceLocation(Mirage.MOD_ID,"animations/zoetrope.animation.json");
    }
}
