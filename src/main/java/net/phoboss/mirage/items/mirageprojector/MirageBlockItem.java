package net.phoboss.mirage.items.mirageprojector;

import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.List;

public class MirageBlockItem extends BlockItem implements IAnimatable {
    public MirageBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    public AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this,"controller",0,this::predicate));
    }

    private PlayState predicate(AnimationEvent<MirageBlockEntity> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("projecting", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("block.mirage.item.tooltip.shift_0"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.shift_1"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.shift_2"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.shift_3"));
        } else {
            tooltip.add(Text.translatable("block.mirage.item.tooltip_0"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip_1"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip_2"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip_3"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip_4"));
        }
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
