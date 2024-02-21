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
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

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
            BlockPos projectorPos = blockEntity.getBlockPos();
            //poseStack.pushPose();//TODO: add this as book settings
            //poseStack.mulPose(new Quaternion(new Vector3f(0,0,1),45,true));
            mirageWorld.render(projectorPos, partialTick, poseStack, bufferSource, packedLight, 0);
            //poseStack.popPose();
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
