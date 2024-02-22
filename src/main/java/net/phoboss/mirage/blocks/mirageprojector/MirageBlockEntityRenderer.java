package net.phoboss.mirage.blocks.mirageprojector;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.phoboss.mirage.client.rendering.customworld.MirageWorld;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;


public class MirageBlockEntityRenderer extends GeoBlockRenderer<MirageBlockEntity>{
    public MirageBlockEntityRenderer(BlockEntityRendererProvider.Context rendererProvider) {
        super(rendererProvider, new MirageBlockModel());
    }

    @Override
    public void render(MirageBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(blockEntity, partialTick, poseStack, bufferSource, packedLight);

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
            float f = (float)pGameTime + partialTick;

            BlockPos projectorPos = blockEntity.getBlockPos();

            MirageProjectorBook bookSettings = blockEntity.getBookSettingsPOJO();
            /*Vector3f pScale = new Vector3f(2,2,2);

            Vector3d pMove = new Vector3d(-1.25,0.5,-1.25);

            Vector3f pRotate = new Vector3f(35.26F,0F,45F);
            Vector3d pRotatePivot = new Vector3d(1.5,1.5,1.5);
            Vector3d pSpinPivot = new Vector3d(1.5,1.5,1.5);
            Vector3f pSpinAxis = new Vector3f(0F,1F,0F);
            float pSpinSpeed = 10F;
            pSpinAxis.normalize();*/

            float[] pScale = bookSettings.getPScale();
            float[] pMove = bookSettings.getPMove();
            Quaternion pRotate = bookSettings.getPRotateAsQuat();
            float[] pRotatePivot = bookSettings.getPRotatePivot();
            float[] pSpinPivot = bookSettings.getPSpinPivot();
            Vector3f pSpinAxis = bookSettings.getPSpinAxisAsVec3();
            float pSpinSpeed = bookSettings.getPSpinSpeed();

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
            poseStack.mulPose(pSpinAxis.rotationDegrees(f * pSpinSpeed));
            poseStack.translate(-pSpinPivot[0],-pSpinPivot[1],-pSpinPivot[2]);

            if(mwFrame != null) {
                float[] pSpinPivotFrame = mwFrame.getPSpinPivot();
                Vector3f pSpinAxisFrame = mwFrame.getPSpinAxisAsVec3();
                float pSpinSpeedFrame = mwFrame.getPSpinSpeed();
                poseStack.translate(pSpinPivotFrame[0],pSpinPivotFrame[1],pSpinPivotFrame[2]);
                poseStack.mulPose(pSpinAxisFrame.rotationDegrees(f * pSpinSpeedFrame));
                poseStack.translate(-pSpinPivotFrame[0],-pSpinPivotFrame[1],-pSpinPivotFrame[2]);
            }
            //TODO: add this as book settings
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
    public boolean shouldRenderOffScreen(BlockEntity pBlockEntity) {
        return true;
    }

    @Override
    public boolean shouldRender(BlockEntity pBlockEntity, Vec3 pCameraPos) {
        return true;
    }

    @Override
    public RenderType getRenderType(MirageBlockEntity animatable, float partialTick, PoseStack poseStack, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, int packedLight, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
