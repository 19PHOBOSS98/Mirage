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
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.RenderUtils;

import java.util.List;
import java.util.function.Consumer;

public class MirageBlockItem extends BlockItem implements GeoItem {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public MirageBlockItem(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> tooltip, TooltipFlag pFlag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("block.mirage.item.tooltip.shift.0"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.shift.1"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.shift.2"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.shift.3"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.shift.4"));
        } else if(Screen.hasControlDown()){
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.0"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.1"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.2"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.3"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.4"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.5"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.6"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.7"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.8"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.9"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.10"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.11"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.12"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.13"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.14"));
            tooltip.add(Component.translatable("block.mirage.item.tooltip.ctrl.15"));
        }
        else {
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
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController(this,"controller",0,this::predicate));
    }

    private PlayState predicate(AnimationState animationState) {
        animationState.getController().setAnimation(RawAnimation.begin().then("projecting", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object itemStack) {
        return RenderUtils.getCurrentTick();
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new IClientItemExtensions() {
            private final BlockEntityWithoutLevelRenderer renderer = new MirageBlockItemRenderer();
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return this.renderer;
            }
        });
    }
}
