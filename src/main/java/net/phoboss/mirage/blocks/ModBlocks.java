package net.phoboss.mirage.blocks;


import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;

import net.minecraft.item.ItemGroup;

import net.minecraft.state.property.Properties;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlock;
import net.phoboss.mirage.items.ModItemGroups;


public class ModBlocks {
    public static final Block MIRAGE_BLOCK = registerBlockWithoutItem("mirage_block",
            new MirageBlock(AbstractBlock
                                .Settings.of(Material.GLASS, MapColor.DIAMOND_BLUE)
                                .nonOpaque()
                                .noCollision()
                                .luminance((state) -> state.get(Properties.LIT) ? 15 : 0)),
            ModItemGroups.MIRAGE
    );




    public static Block registerBlockWithoutItem(String name, Block block, ItemGroup group){
        return Registry.register(Registry.BLOCK, new Identifier(Mirage.MOD_ID,name),block);
    }

    public static void registerAll() {
        Mirage.LOGGER.info("Registering Mod Blocks for " + Mirage.MOD_ID);
    }

    public static class ExtraItemSettings {
        public int stackLimit=64;
        public String tooltipShiftKey;

        public String tooltipKey;

        public ExtraItemSettings setStackLimit(int stackLimit) {
            this.stackLimit = stackLimit;
            return this;
        }


        public ExtraItemSettings setTooltipShiftKey(String tooltipShiftKey) {
            this.tooltipShiftKey = tooltipShiftKey;
            return this;
        }

        public ExtraItemSettings setTooltipKey(String tooltipKey) {
            this.tooltipKey = tooltipKey;
            return this;
        }

        public ExtraItemSettings() {
        }
    }
}
