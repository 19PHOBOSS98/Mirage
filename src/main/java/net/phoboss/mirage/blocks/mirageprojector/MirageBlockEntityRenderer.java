package net.phoboss.mirage.blocks.mirageprojector;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.phoboss.mirage.client.rendering.customworld.MirageWorld;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class MirageBlockEntityRenderer extends GeoBlockRenderer<MirageBlockEntity>{
    public MirageBlockEntityRenderer(BlockEntityRendererProvider.Context rendererProvider) {
        super(rendererProvider, new MirageBlockModel());
    }

    @Override
    public void render(MirageBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight);
        boolean isPowered = blockEntity.isPowered();
        if(!isPowered) {
            return;
        }

        boolean isTopPowered = blockEntity.isTopPowered();

        boolean areSidesPowered = blockEntity.areSidesPowered();

            ConcurrentHashMap<Integer,MirageWorld> mirageWorldList = blockEntity.getMirageWorlds();
            if(mirageWorldList.isEmpty()) {
                blockEntity.savePreviousTopPowerState(isTopPowered);
                blockEntity.savePreviousBottomPowerState(isPowered);
                blockEntity.savePreviousSidesPowerState(areSidesPowered);
                return;
            }
            MirageProjectorBook mirageProjectorBook = blockEntity.getBookSettingsPOJO();

            if(mirageProjectorBook.isAutoPlay()) {
                if(!blockEntity.isPause()) {
                    blockEntity.nextMirageWorldIndex(mirageWorldList.size());
                }
            }else{
                if(blockEntity.isStepping()){
                    blockEntity.nextBookStep(mirageWorldList.size());
                }
                //blockEntity.setMirageWorldIndex(Math.abs(Math.max(0,Math.min(mirageProjectorBook.getStep(),mirageWorldList.size()-1))));
                blockEntity.setMirageWorldIndex(Math.abs(mirageProjectorBook.getStep()) % mirageWorldList.size());//better-ish clamping function for manual book step setting
            }

            int mirageWorldIndex = blockEntity.getMirageWorldIndex();

            MirageWorld mirageWorld = mirageWorldList.get(mirageWorldIndex);

            if (mirageWorld != null) {
                BlockPos projectorPos = blockEntity.getBlockPos();
                mirageWorld.render(projectorPos, partialTick, poseStack, bufferSource, packedLight, 0);
            }

        blockEntity.savePreviousTopPowerState(isTopPowered);
        blockEntity.savePreviousBottomPowerState(isPowered);
        blockEntity.savePreviousSidesPowerState(areSidesPowered);
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public RenderType getRenderType(MirageBlockEntity animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    public int getViewDistance() {
        return 512;
    }
}
