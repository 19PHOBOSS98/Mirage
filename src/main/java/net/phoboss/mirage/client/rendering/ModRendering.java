package net.phoboss.mirage.client.rendering;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.phoboss.mirage.blocks.ModBlockEntities;
import net.phoboss.mirage.blocks.ModBlocks;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntityRenderer;


public class ModRendering {

    public static void registerRenderType() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MIRAGE_BLOCK, RenderLayer.getTranslucent());
    }

    public static void registerBlockEntityRenderers() {
        BlockEntityRendererFactories.register(ModBlockEntities.MIRAGE_BLOCK, MirageBlockEntityRenderer::new);

    }

    public static void registerAll() {
        registerRenderType();
        registerBlockEntityRenderers();
    }
}
