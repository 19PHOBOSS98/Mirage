package net.phoboss.mirage.blocks.mirageprojector;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.client.rendering.customworld.MirageWorld;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class MirageBlockEntityRenderer extends GeoBlockRenderer<MirageBlockEntity>{
    public MirageBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(new MirageBlockModel());
    }

    @Override
    public void render(MirageBlockEntity blockEntity, float tickDelta, MatrixStack poseStack,VertexConsumerProvider vertexConsumers, int light) {
        super.render(blockEntity, tickDelta, poseStack, vertexConsumers, light);

        if(!blockEntity.isPowered()) {
            return;
        }
        if(!Mirage.CONFIGS.get("enableRecursiveMirage").getAsBoolean() && blockEntity.getWorld() instanceof MirageWorld){
                return;//recursive mirages are unsafe
        }
        ConcurrentHashMap<Integer,MirageWorld> mirageWorldList = blockEntity.getMirageWorlds();
        int mirageWorldIndex = blockEntity.getMirageWorldIndex();
        if(!mirageWorldList.containsKey(mirageWorldIndex)){
            return;
        }

        MirageWorld mirageWorld = mirageWorldList.get(mirageWorldIndex);

        if (mirageWorld != null) {
            long pGameTime = blockEntity.getWorld().getTime();
            float time = pGameTime + tickDelta;
            //float time = (float)pGameTime + tickDelta;

            BlockPos projectorPos = blockEntity.getPos();

            MirageProjectorBook bookSettings = blockEntity.getBookSettingsPOJO();

            float[] pScale = bookSettings.getPScale();
            float[] pMove = bookSettings.getPMove();
            Quaternion pRotate = bookSettings.getPRotateAsQuat();
            float[] pRotatePivot = bookSettings.getPRotatePivot();
            float[] pSpinPivot = bookSettings.getPSpinPivot();
            Vec3f pSpinAxis = bookSettings.getPSpinAxisAsVec3();
            float pSpinSpeed = bookSettings.getPSpinSpeed();
            float pSpinOffset = bookSettings.getPSpinOffset();
            HashMap<Integer,Frame> frames = blockEntity.getBookSettingsPOJO().getFrames();
            Frame mwFrame = frames.get(mirageWorldIndex);

            poseStack.push();

            poseStack.scale(pScale[0],pScale[1],pScale[2]);

            if(mwFrame != null) {
                float[] pScaleFrame = mwFrame.getPScale();
                poseStack.scale(pScaleFrame[0],pScaleFrame[1],pScaleFrame[2]);
            }

            poseStack.push();
            poseStack.translate(pMove[0],pMove[1],pMove[2]);

            if(mwFrame != null){
                float[] pMoveFrame = mwFrame.getPMove();
                poseStack.translate(pMoveFrame[0],pMoveFrame[1],pMoveFrame[2]);
            }

            poseStack.translate(pSpinPivot[0],pSpinPivot[1],pSpinPivot[2]);
            poseStack.multiply(pSpinAxis.getDegreesQuaternion(time * pSpinSpeed * 0.05F - pSpinOffset)); // derived from BeaconBlockEntityRenderer: 45 deg/sec:2.25F (2.25/45=0.05) didn't bother to look into it more :)
            poseStack.translate(-pSpinPivot[0],-pSpinPivot[1],-pSpinPivot[2]);

            if(mwFrame != null) {
                float[] pSpinPivotFrame = mwFrame.getPSpinPivot();
                Vec3f pSpinAxisFrame = mwFrame.getPSpinAxisAsVec3();
                float pSpinSpeedFrame = mwFrame.getPSpinSpeed();
                float pSpinOffsetFrame = mwFrame.getPSpinOffset();
                poseStack.translate(pSpinPivotFrame[0],pSpinPivotFrame[1],pSpinPivotFrame[2]);
                poseStack.multiply(pSpinAxisFrame.getDegreesQuaternion(time * pSpinSpeedFrame * 0.05F - pSpinOffsetFrame));
                poseStack.translate(-pSpinPivotFrame[0],-pSpinPivotFrame[1],-pSpinPivotFrame[2]);
            }

            poseStack.translate(pRotatePivot[0],pRotatePivot[1],pRotatePivot[2]);
            poseStack.multiply(pRotate);
            poseStack.translate(-pRotatePivot[0],-pRotatePivot[1],-pRotatePivot[2]);

            if(mwFrame != null) {
                float[] pRotatePivotFrame = mwFrame.getPRotatePivot();
                poseStack.translate(pRotatePivotFrame[0],pRotatePivotFrame[1],pRotatePivotFrame[2]);
                poseStack.multiply(mwFrame.getPRotateAsQuat());
                poseStack.translate(-pRotatePivotFrame[0],-pRotatePivotFrame[1],-pRotatePivotFrame[2]);
            }
            mirageWorld.render(projectorPos, tickDelta, poseStack, vertexConsumers, light, 0);

            poseStack.pop();
            poseStack.pop();
        }
    }

    @Override
    public boolean isInRenderDistance(BlockEntity blockEntity, Vec3d pos) {
        return true;
    }

    @Override
    public boolean rendersOutsideBoundingBox(BlockEntity blockEntity) {
        return true;
    }


    @Override
    public RenderLayer getRenderType(MirageBlockEntity animatable, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, VertexConsumer buffer, int packedLight, Identifier texture) {
        return RenderLayer.getEntityTranslucent(getTextureLocation(animatable));
    }
}
