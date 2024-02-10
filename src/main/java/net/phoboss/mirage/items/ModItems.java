package net.phoboss.mirage.items;


import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.ModBlocks;
import net.phoboss.mirage.items.mirageprojector.MirageBlockItem;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ModItems {
    public static final Item MIRAGE_BLOCK_ITEM = registerItem("mirage_block",
            new MirageBlockItem(
                    ModBlocks.MIRAGE_BLOCK,
                    new FabricItemSettings().maxCount(1)){
                        @Override
                        public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
                            if (Screen.hasShiftDown()) {
                                tooltip.add(Text.translatable("block.mirage.item.tooltip.shift"));
                            } else {
                                tooltip.add(Text.translatable("block.mirage.item.tooltip"));
                            }
                        }
                    });

            public static Item registerItem(String name, Item item){
                return Registry.register(Registries.ITEM,new Identifier(Mirage.MOD_ID,name),item);
            }

    public static void registerAll(){
        Mirage.LOGGER.debug("Registering Mod Items for "+ Mirage.MOD_ID);
    }


}
