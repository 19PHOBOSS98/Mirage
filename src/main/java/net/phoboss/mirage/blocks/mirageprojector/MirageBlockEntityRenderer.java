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
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class MirageBlockEntityRenderer extends GeoBlockRenderer<MirageBlockEntity>{
    public MirageBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(new MirageBlockModel());
    }

    @Override
    public void render(MirageBlockEntity blockEntity, float tickDelta, MatrixStack matrices,VertexConsumerProvider vertexConsumers, int light) {
        super.render(blockEntity, tickDelta, matrices, vertexConsumers, light);

        if(!blockEntity.isPowered()) {
            return;
        }
        if(blockEntity.getWorld() instanceof MirageWorld){ //TODO: Make this a configurable option
            return;//recursive mirages are unsafe
        }
        ConcurrentHashMap<Integer,MirageWorld> mirageWorldList = blockEntity.getMirageWorlds();
        int mirageWorldIndex = blockEntity.getMirageWorldIndex();
        if(!mirageWorldList.containsKey(mirageWorldIndex)){
            return;
        }

        MirageWorld mirageWorld = mirageWorldList.get(mirageWorldIndex);

        if (mirageWorld != null) {
            BlockPos projectorPos = blockEntity.getPos();
            //poseStack.pushPose();//TODO: add this as book settings
            //poseStack.mulPose(new Quaternion(new Vector3f(0,0,1),45,true));
            mirageWorld.render(projectorPos, tickDelta, matrices, vertexConsumers, light, 0);
            //poseStack.popPose();
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
        return RenderLayer.getEntityTranslucent(getTextureResource(animatable));
    }
}
