package net.phoboss.mirage.items.mirageprojector;

import net.minecraft.util.Identifier;
import net.phoboss.mirage.Mirage;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class MirageBlockItemModel extends AnimatedGeoModel<MirageBlockItem> {
    @Override
    public Identifier getModelLocation(MirageBlockItem object) {
        return new Identifier(Mirage.MOD_ID,"geo/zoetrope.geo.json");
    }

    @Override
    public Identifier getTextureLocation(MirageBlockItem object) {
        return new Identifier(Mirage.MOD_ID,"textures/block/zoetrope.png");
    }

    @Override
    public Identifier getAnimationFileLocation(MirageBlockItem animatable) {
        return new Identifier(Mirage.MOD_ID,"animations/zoetrope.animation.json");
    }
}
