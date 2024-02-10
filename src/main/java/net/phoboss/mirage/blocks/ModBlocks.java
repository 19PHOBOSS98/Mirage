package net.phoboss.mirage.blocks;


import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlock;


public class ModBlocks {
    public static AbstractBlock.Settings mirageProjectorBlockBehavior = FabricBlockSettings
            .copyOf(Blocks.GLASS)
            .luminance((state) -> state.get(Properties.LIT) ? 15 : 0)
            .nonOpaque()
            .noCollision();
    public static final Block MIRAGE_BLOCK = registerBlockWithoutItem("mirage_block",
            new MirageBlock(mirageProjectorBlockBehavior));

    public static Block registerBlockWithoutItem(String name, Block block){
        return Registry.register(Registries.BLOCK, new Identifier(Mirage.MOD_ID,name),block);
    }

    public static void registerAll() {
        Mirage.LOGGER.info("Registering Mod Blocks for " + Mirage.MOD_ID);
    }


}
