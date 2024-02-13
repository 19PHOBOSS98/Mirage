package net.phoboss.mirage.items.mirageprojector;


import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;
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
import java.util.function.Consumer;

public class MirageBlockItem extends BlockItem implements IAnimatable {
    public MirageBlockItem(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pFlag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.shift.0"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.shift.1"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.shift.2"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.shift.3"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.shift.4"));
        } else if(Screen.hasControlDown()){
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.0"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.1"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.2"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.3"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.4"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.5"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.6"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.7"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.8"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.9"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.10"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.11"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.12"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.13"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.14"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.ctrl.15"));
        }
        else {
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.0"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.1"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.2"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.3"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.4"));
            tooltip.add(new TranslatableComponent("block.mirage.item.tooltip.5"));
        }
        super.appendHoverText(pStack, pLevel, tooltip, pFlag);
    }

    public AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IItemRenderProperties() {
            private final BlockEntityWithoutLevelRenderer renderer = new MirageBlockItemRenderer();
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this,"controller",0,this::predicate));
    }

    private PlayState predicate(AnimationEvent<MirageBlockEntity> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("projecting", ILoopType.EDefaultLoopTypes.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
}
