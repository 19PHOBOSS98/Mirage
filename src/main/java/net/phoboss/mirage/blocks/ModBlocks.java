package net.phoboss.mirage.blocks;


import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlock;
import net.phoboss.mirage.items.ModItemGroups;
import net.phoboss.mirage.items.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,Mirage.MOD_ID);

    /*public static final RegistryObject<Block> MIRAGE_BLOCK = ModBlocks.registerBlock(
            "mirage_block",
            () -> new MirageBlock(BlockBehaviour
                    .Properties.of(Material.GLASS, MaterialColor.DIAMOND)
                    .noOcclusion()
                    .noCollission()
                    .lightLevel((state) -> state.getValue(BlockStateProperties.LIT) ? 15 : 0)),
            ModItemGroups.MIRAGE
            //new ModBlocks.ExtraItemSettings()
                    //.setStackLimit(1)
                    //.setTooltipKey("block.mirage.omni_beacon.tooltip")
                    //.setTooltipShiftKey("block.mirage.omni_beacon.tooltip.shift")
    );*/


    public static final RegistryObject<Block> MIRAGE_BLOCK = ModBlocks.registerBlockWithoutItem(
            "mirage_block",
            () -> new MirageBlock(BlockBehaviour
                    .Properties.of(Material.GLASS, MaterialColor.DIAMOND)
                    .noOcclusion()
                    .noCollission()
                    .lightLevel((state) -> state.getValue(BlockStateProperties.LIT) ? 15 : 0))
    );

    public static <T extends Block> RegistryObject<T> registerBlockWithoutItem(String name, Supplier<T> block){
        return BLOCKS.register(name,block);
    }

    public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block, CreativeModeTab group){
        RegistryObject<T> toReturn = BLOCKS.register(name,block);
        registerBlockItem(name, toReturn, group);
        return toReturn;
    }
    public static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block, CreativeModeTab group){
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(group)));
    }



    public static void registerAll(IEventBus eventBus) {
        Mirage.LOGGER.info("Registering Mod Blocks for " + Mirage.MOD_ID);
        BLOCKS.register(eventBus);
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
