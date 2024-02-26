package net.phoboss.mirage.blocks.mirageprojector;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.phoboss.mirage.client.rendering.customworld.MirageWorld;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class MirageBlockEntityRenderer extends GeoBlockRenderer<MirageBlockEntity> {

    public MirageBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(new MirageBlockModel());
    }

    @Override
    public void renderFinal(PoseStack poseStack, MirageBlockEntity blockEntity, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderFinal(poseStack, blockEntity, model, bufferSource, buffer, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if(!blockEntity.isPowered()) {
            return;
        }
        if(blockEntity.getLevel() instanceof MirageWorld){ //TODO: Make this a configurable option
            return;//recursive mirages are unsafe
        }
        ConcurrentHashMap<Integer,MirageWorld> mirageWorldList = blockEntity.getMirageWorlds();
        int mirageWorldIndex = blockEntity.getMirageWorldIndex();
        if(!mirageWorldList.containsKey(mirageWorldIndex)){
            return;
        }

        MirageWorld mirageWorld = mirageWorldList.get(mirageWorldIndex);

        if (mirageWorld != null) {
            long pGameTime = blockEntity.getLevel().getGameTime();
            float time = (float)pGameTime + partialTick;

            BlockPos projectorPos = blockEntity.getBlockPos();

            MirageProjectorBook bookSettings = blockEntity.getBookSettingsPOJO();

            float[] pScale = bookSettings.getPScale();
            float[] pMove = bookSettings.getPMove();
            Quaternionf pRotate = bookSettings.getPRotateAsQuat();
            float[] pRotatePivot = bookSettings.getPRotatePivot();
            float[] pSpinPivot = bookSettings.getPSpinPivot();
            Vector3f pSpinAxis = bookSettings.getPSpinAxisAsVec3();
            float pSpinSpeed = bookSettings.getPSpinSpeed();
            float pSpinOffset = bookSettings.getPSpinOffset();
            HashMap<Integer,Frame> frames = blockEntity.getBookSettingsPOJO().getFrames();
            Frame mwFrame = frames.get(mirageWorldIndex);

            poseStack.pushPose();

            poseStack.scale(pScale[0],pScale[1],pScale[2]);

            if(mwFrame != null) {
                float[] pScaleFrame = mwFrame.getPScale();
                poseStack.scale(pScaleFrame[0],pScaleFrame[1],pScaleFrame[2]);
            }

            poseStack.pushPose();
            poseStack.translate(pMove[0],pMove[1],pMove[2]);

            if(mwFrame != null){
                float[] pMoveFrame = mwFrame.getPMove();
                poseStack.translate(pMoveFrame[0],pMoveFrame[1],pMoveFrame[2]);
            }

            poseStack.translate(pSpinPivot[0],pSpinPivot[1],pSpinPivot[2]);
            poseStack.mulPose(Axis.of(pSpinAxis).rotationDegrees(time * pSpinSpeed * 0.05F - pSpinOffset));// derived from BeaconBlockEntityRenderer: 45 deg/sec:2.25F (2.25/45=0.05) didn't bother to look into it more :)
            poseStack.translate(-pSpinPivot[0],-pSpinPivot[1],-pSpinPivot[2]);

            if(mwFrame != null) {
                float[] pSpinPivotFrame = mwFrame.getPSpinPivot();
                Vector3f pSpinAxisFrame = mwFrame.getPSpinAxisAsVec3();
                float pSpinSpeedFrame = mwFrame.getPSpinSpeed();
                float pSpinOffsetFrame = mwFrame.getPSpinOffset();
                poseStack.translate(pSpinPivotFrame[0],pSpinPivotFrame[1],pSpinPivotFrame[2]);
                poseStack.mulPose(Axis.of(pSpinAxisFrame).rotationDegrees(time * pSpinSpeedFrame * 0.05F - pSpinOffsetFrame));
                poseStack.translate(-pSpinPivotFrame[0],-pSpinPivotFrame[1],-pSpinPivotFrame[2]);
            }

            poseStack.translate(pRotatePivot[0],pRotatePivot[1],pRotatePivot[2]);
            poseStack.mulPose(pRotate);
            poseStack.translate(-pRotatePivot[0],-pRotatePivot[1],-pRotatePivot[2]);

            if(mwFrame != null) {
                float[] pRotatePivotFrame = mwFrame.getPRotatePivot();
                poseStack.translate(pRotatePivotFrame[0],pRotatePivotFrame[1],pRotatePivotFrame[2]);
                poseStack.mulPose(mwFrame.getPRotateAsQuat());
                poseStack.translate(-pRotatePivotFrame[0],-pRotatePivotFrame[1],-pRotatePivotFrame[2]);
            }
            mirageWorld.render(projectorPos, partialTick, poseStack, bufferSource, packedLight, 0);
            poseStack.popPose();
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRender(MirageBlockEntity pBlockEntity, Vec3 pCameraPos) {
        return true;
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
