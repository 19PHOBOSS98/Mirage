package net.phoboss.mirage.items.mirageprojector;

import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.RenderUtils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class MirageBlockItem extends BlockItem implements GeoItem {
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

    public MirageBlockItem(Block block, Settings settings) {
        super(block, settings);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
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
        }
        else {
            tooltip.add(Text.translatable("block.mirage.item.tooltip.0"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.1"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.2"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.3"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.4"));
            tooltip.add(Text.translatable("block.mirage.item.tooltip.5"));
        }
    }
    
    @Override
    public void createRenderer(Consumer<Object> consumer) {
        consumer.accept(new RenderProvider() {
            private final MirageBlockItemRenderer renderer = new MirageBlockItemRenderer();
            @Override
            public BuiltinModelItemRenderer getCustomRenderer() {
                return this.renderer;
            }
        });
    }

    @Override
    public Supplier<Object> getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> geoAnimatableAnimationState) {
        AnimationController controller = geoAnimatableAnimationState.getController();
        controller.setAnimation(RawAnimation.begin().then("projecting", Animation.LoopType.LOOP));
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
}
