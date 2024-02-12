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
            tooltip.add(Text.translatable("block.mirage.item.tooltip.shift.0"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.shift.1"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.shift.2"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.shift.3"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.shift.4"));
        } else if(Screen.hasControlDown()){
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.0"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.1"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.2"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.3"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.4"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.5"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.6"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.7"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.8"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.9"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.10"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.11"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.12"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.13"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.14"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.ctrl.15"));
        }else {
            tooltip.add(Text.translatable("block.mirage.item.tooltip.0"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.1"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.2"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.3"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.4"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.5"));
        }
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
