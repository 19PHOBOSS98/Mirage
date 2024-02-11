package net.phoboss.mirage.blocks.mirageprojector;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.phoboss.mirage.client.rendering.customworld.MirageWorld;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.util.List;


public class MirageBlockEntityRenderer extends GeoBlockRenderer<MirageBlockEntity> {

    public MirageBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new MirageBlockModel());
    }

    @Override
    public void renderFinal(PoseStack poseStack, MirageBlockEntity blockEntity, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderFinal(poseStack, blockEntity, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        boolean isTopPowered = blockEntity.isTopPowered();
        boolean isPowered = blockEntity.isPowered();
        if(isPowered) {
            List<MirageWorld> mirageWorldList = blockEntity.getMirageWorlds();
            if(mirageWorldList.isEmpty()) {
                blockEntity.savePreviousTopPowerState(isTopPowered);
                blockEntity.savePreviousBottomPowerState(isPowered);
                return;
            }
            MirageProjectorBook mirageProjectorBook = blockEntity.getBookSettingsPOJO();

            if(mirageProjectorBook.isAutoPlay()) {
                if(!isTopPowered) {
                    blockEntity.nextMirageWorldIndex(mirageWorldList.size());
                }
            }else{
                if(isTopPowered && !blockEntity.wasTopPowered()){
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
        }
        blockEntity.savePreviousTopPowerState(isTopPowered);
        blockEntity.savePreviousBottomPowerState(isPowered);
    }


    @Override
    public int getViewDistance() {
        return 512;
    }

    @Override
    public boolean shouldRenderOffScreen(MirageBlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public RenderType getRenderType(MirageBlockEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
