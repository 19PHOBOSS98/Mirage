package net.phoboss.mirage.items.mirageprojector;

import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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
            tooltip.add(new TranslatableText("block.mirage.item.tooltip.shift.0"));
            tooltip.add(new TranslatableText("block.mirage.item.tooltip.shift.1"));
            tooltip.add(new TranslatableText("block.mirage.item.tooltip.shift.2"));
            tooltip.add(new TranslatableText("block.mirage.item.tooltip.shift.3"));
            tooltip.add(new TranslatableText("block.mirage.item.tooltip.shift.4"));
        } else if(Screen.hasAltDown() && Screen.hasControlDown()){
            for(int i=0;i<15;++i) {
                tooltip.add(new TranslatableText("block.mirage.item.tooltip.shift_alt." + i));
            }
        } else if(Screen.hasControlDown()){
            for(int i=0;i<14;++i) {
                tooltip.add(new TranslatableText("block.mirage.item.tooltip.ctrl."+i));
            }
        } else {
            tooltip.add(new TranslatableText("block.mirage.item.tooltip.0"));
            tooltip.add(new TranslatableText("block.mirage.item.tooltip.1"));
            tooltip.add(new TranslatableText("block.mirage.item.tooltip.2"));
            tooltip.add(new TranslatableText("block.mirage.item.tooltip.3"));
            tooltip.add(new TranslatableText("block.mirage.item.tooltip.4"));
            tooltip.add(new TranslatableText("block.mirage.item.tooltip.5"));
        }
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
