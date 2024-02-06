package net.phoboss.mirage.blocks.mirageprojector;

import net.minecraft.util.Identifier;
import net.phoboss.mirage.Mirage;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class MirageBlockModel extends AnimatedGeoModel<MirageBlockEntity> {
    @Override
    public Identifier getModelLocation(MirageBlockEntity object) {
        return new Identifier(Mirage.MOD_ID,"geo/zoetrope.geo.json");
    }

    @Override
    public Identifier getTextureLocation(MirageBlockEntity object) {
        return new Identifier(Mirage.MOD_ID,"textures/block/zoetrope.png");
    }

    @Override
    public Identifier getAnimationFileLocation(MirageBlockEntity animatable) {
        return new Identifier(Mirage.MOD_ID,"animations/zoetrope.animation.json");
    }
}
