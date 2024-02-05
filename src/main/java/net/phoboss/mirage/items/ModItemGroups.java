package net.phoboss.mirage.items;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.phoboss.mirage.blocks.ModBlocks;


public class ModItemGroups {

    public static CreativeModeTab MIRAGE  = new CreativeModeTab("mirage") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModBlocks.MIRAGE_BLOCK.get());
        }
    };




}
