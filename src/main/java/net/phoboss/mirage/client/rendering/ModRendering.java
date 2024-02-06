package net.phoboss.mirage.client.rendering;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import net.phoboss.mirage.blocks.ModBlockEntities;
import net.phoboss.mirage.blocks.ModBlocks;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntityRenderer;
import net.phoboss.mirage.items.ModItems;
import net.phoboss.mirage.items.mirageprojector.MirageBlockItemRenderer;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;


public class ModRendering {

    public static void registerRenderType() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MIRAGE_BLOCK, RenderLayer.getTranslucent());
    }

    public static void registerBlockEntityRenderers() {
        BlockEntityRendererRegistry.register(ModBlockEntities.MIRAGE_BLOCK, MirageBlockEntityRenderer::new);
        GeoItemRenderer.registerItemRenderer(ModItems.MIRAGE_BLOCK_ITEM, new MirageBlockItemRenderer());
    }

    public static void registerAll() {
        registerRenderType();
        registerBlockEntityRenderers();
    }
}
