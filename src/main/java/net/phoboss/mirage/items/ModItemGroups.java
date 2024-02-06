package net.phoboss.mirage.items;


import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.phoboss.mirage.blocks.ModBlocks;


public class ModItemGroups {

    public static ItemGroup MIRAGE  = FabricItemGroupBuilder.build(
            new Identifier("mirage", "mirage"),
            () -> new ItemStack(
                    ModBlocks.MIRAGE_BLOCK)
    );

}
