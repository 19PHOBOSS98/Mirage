package net.phoboss.mirage.blocks.mirageprojector;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.phoboss.mirage.client.rendering.customworld.MirageWorld;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

import java.util.List;


public class MirageBlockEntityRenderer extends GeoBlockRenderer<MirageBlockEntity>{
    public MirageBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        super(new MirageBlockModel());
    }

    @Override
    public void render(MirageBlockEntity blockEntity, float tickDelta, MatrixStack matrices,VertexConsumerProvider vertexConsumers, int light) {
        super.render(blockEntity, tickDelta, matrices, vertexConsumers, light);

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
                BlockPos projectorPos = blockEntity.getPos();
                mirageWorld.render(projectorPos, tickDelta, matrices, vertexConsumers, light, 0);
            }
        }
        blockEntity.savePreviousTopPowerState(isTopPowered);
        blockEntity.savePreviousBottomPowerState(isPowered);
    }

    @Override
    public int getRenderDistance() {
        return 512;
    }

    @Override
    public boolean rendersOutsideBoundingBox(BlockEntity blockEntity) {
        return true;
    }

    /*@Override
    public boolean rendersOutsideBoundingBox(MirageBlockEntity blockEntity) {
        return true;
    }*/

    @Override
    public RenderLayer getRenderType(MirageBlockEntity animatable, float partialTick, MatrixStack poseStack, VertexConsumerProvider bufferSource, VertexConsumer buffer, int packedLight, Identifier texture) {
        return RenderLayer.getEntityTranslucent(getTextureResource(animatable));
    }
}
