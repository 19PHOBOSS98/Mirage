package net.phoboss.mirage.items.mirageprojector;


import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
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

    public AnimationFactory factory = GeckoLibUtil.createFactory(this);

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = new MirageBlockItemRenderer();
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pFlag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("block.mirage.item.tooltip.shift.0"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.shift.1"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.shift.2"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.shift.3"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.shift.4"));
        } else if(Screen.hasAltDown() && Screen.hasControlDown()){
            for(int i=0;i<15;++i) {
                tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl_alt." + i));
            }
        } else if(Screen.hasControlDown()){
            for(int i=0;i<14;++i) {
                tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl."+i));
            }
        }else {
            tooltip.add(Component.translatable("block.mirage.item.tooltip.0"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.1"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.2"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.3"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.4"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.5"));
        }
        super.appendHoverText(pStack, pLevel, tooltip, pFlag);
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
