package net.phoboss.mirage.items.mirageprojector;

import net.minecraft.util.Identifier;
import net.phoboss.mirage.Mirage;
import software.bernie.geckolib.model.GeoModel;

public class MirageBlockItemModel extends GeoModel<MirageBlockItem> {
    @Override
    public Identifier getModelResource(MirageBlockItem object) {
        return new Identifier(Mirage.MOD_ID,"geo/zoetrope.geo.json");
    }

    @Override
    public Identifier getTextureResource(MirageBlockItem object) {
        return new Identifier(Mirage.MOD_ID,"textures/block/zoetrope.png");
    }

    @Override
    public Identifier getAnimationResource(MirageBlockItem animatable) {
        return new Identifier(Mirage.MOD_ID,"animations/zoetrope.animation.json");
    }
}
