package net.phoboss.mirage.items;


import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.ModBlocks;
import net.phoboss.mirage.items.mirageprojector.MirageBlockItem;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,Mirage.MOD_ID);

    public static final RegistryObject<Item> MIRAGE_BLOCK_ITEM = ITEMS.register("mirage_block",
            () -> new MirageBlockItem(ModBlocks.MIRAGE_BLOCK.get(),new Item.Properties()));

    public static void registerAll(IEventBus eventBus){
        Mirage.LOGGER.debug("Registering Mod Items for "+ Mirage.MOD_ID);
        ITEMS.register(eventBus);
    }
}
