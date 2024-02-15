package net.phoboss.mirage.client.rendering;


import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.phoboss.mirage.blocks.ModBlockEntities;
import net.phoboss.mirage.blocks.ModBlocks;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntityRenderer;


public class ModRendering {

    public static void registerRenderType() {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.MIRAGE_BLOCK.get(), RenderType.translucent());

    }

    public static void registerBlockEntityRenderers() {
        BlockEntityRenderers.register(ModBlockEntities.MIRAGE_BLOCK.get(), MirageBlockEntityRenderer::new);
    }

    public static void registerAll() {
        registerRenderType();
        registerBlockEntityRenderers();
    }
}
