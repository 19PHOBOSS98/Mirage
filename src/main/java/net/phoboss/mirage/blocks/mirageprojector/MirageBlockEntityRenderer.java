package net.phoboss.mirage.blocks.mirageprojector;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.phoboss.mirage.client.rendering.customworld.MirageWorld;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class MirageBlockEntityRenderer extends GeoBlockRenderer<MirageBlockEntity> {

    public MirageBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(new MirageBlockModel());
    }


    @Override
    public void renderFinal(MatrixStack poseStack, MirageBlockEntity blockEntity, BakedGeoModel model, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderFinal(poseStack, blockEntity, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        boolean isPowered = blockEntity.isPowered();
        if(!isPowered) {
            return;
        }

        ConcurrentHashMap<Integer,MirageWorld> mirageWorldList = blockEntity.getMirageWorlds();
        int mirageListLength = mirageWorldList.size();
        if(mirageListLength < 1){
            return;
        }

        boolean isTopPowered = blockEntity.isTopPowered();

        boolean areSidesPowered = blockEntity.areSidesPowered();


        if(mirageWorldList.isEmpty()) {
            blockEntity.savePreviousTopPowerState(isTopPowered);
            blockEntity.savePreviousBottomPowerState(isPowered);
            blockEntity.savePreviousSidesPowerState(areSidesPowered);
            return;
        }
        MirageProjectorBook mirageProjectorBook = blockEntity.getBookSettingsPOJO();

        if(mirageProjectorBook.isAutoPlay()) {
            if(!blockEntity.isPause()) {
                blockEntity.nextMirageWorldIndex(mirageListLength);
            }
        }else{
            if(blockEntity.isStepping()){
                blockEntity.nextBookStep(mirageListLength);
            }
            //blockEntity.setMirageWorldIndex(Math.abs(Math.max(0,Math.min(mirageProjectorBook.getStep(),mirageWorldList.size()-1))));
            blockEntity.setMirageWorldIndex(Math.abs(mirageProjectorBook.getStep()) % mirageListLength);//better-ish clamping function for manual book step setting
        }

        int mirageWorldIndex = blockEntity.getMirageWorldIndex();

        MirageWorld mirageWorld = mirageWorldList.get(mirageWorldIndex);

        if (mirageWorld != null) {
            BlockPos projectorPos = blockEntity.getPos();
            //poseStack.pushPose();
            //poseStack.mulPose(new Quaternion(new Vector3f(0,0,1),45,true));
            mirageWorld.render(projectorPos, partialTick, poseStack, bufferSource, packedLight, 0);
            //poseStack.popPose();
        }

        blockEntity.savePreviousTopPowerState(isTopPowered);
        blockEntity.savePreviousBottomPowerState(isPowered);
        blockEntity.savePreviousSidesPowerState(areSidesPowered);
    }

    @Override
    public boolean isInRenderDistance(MirageBlockEntity blockEntity, Vec3d pos) {
        return true;
    }

    @Override
    public boolean rendersOutsideBoundingBox(MirageBlockEntity blockEntity) {
        return true;
    }

    @Override
    public RenderLayer getRenderType(MirageBlockEntity animatable, Identifier texture, VertexConsumerProvider bufferSource, float partialTick) {
        return RenderLayer.getEntityTranslucent(getGeoModel().getTextureResource(animatable));
    }
}
