package net.phoboss.mirage.blocks.mirageprojector;

import net.minecraft.util.Identifier;
import net.phoboss.mirage.Mirage;
import software.bernie.geckolib.model.GeoModel;

public class MirageBlockModel extends GeoModel<MirageBlockEntity> {

    @Override
    public Identifier getModelResource(MirageBlockEntity object) {
        return new Identifier(Mirage.MOD_ID,"geo/zoetrope.geo.json");
    }

    @Override
    public Identifier getTextureResource(MirageBlockEntity object) {
        return new Identifier(Mirage.MOD_ID,"textures/block/zoetrope.png");
    }

    @Override
    public Identifier getAnimationResource(MirageBlockEntity animatable) {
        return new Identifier(Mirage.MOD_ID,"animations/zoetrope.animation.json");
    }
}
