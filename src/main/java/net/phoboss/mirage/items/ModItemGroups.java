package net.phoboss.mirage.items;


import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.ModBlocks;


public class ModItemGroups {

/*    public static ItemGroup MIRAGE  = FabricItemGroupBuilder.build(
            new Identifier("mirage", "mirage"),
            () -> new ItemStack(
                    ModBlocks.MIRAGE_BLOCK)
    );*/

    public static ItemGroup MIRAGE = Registry.register(Registries.ITEM_GROUP, new Identifier(Mirage.MOD_ID,"mirage"),
            FabricItemGroup.builder()
                    .displayName(Text.translatable("itemGroup.mirage.mirage"))
                    .icon(() -> new ItemStack(ModBlocks.MIRAGE_BLOCK))
                    .entries(((displayContext, entries) -> {
                        entries.add(ModBlocks.MIRAGE_BLOCK);
                    }))
                    .build()
    );

    public static void registerAll(){

    }

}
