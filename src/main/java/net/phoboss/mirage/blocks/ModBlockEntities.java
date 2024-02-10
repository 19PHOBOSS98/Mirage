package net.phoboss.mirage.blocks;


import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,Mirage.MOD_ID);

    public static final RegistryObject<BlockEntityType<MirageBlockEntity>> MIRAGE_BLOCK = registerBlockEntities("mirage_block",
            () ->   BlockEntityType.Builder.of(
                    MirageBlockEntity::new,
                    ModBlocks.MIRAGE_BLOCK.get()
                    //ModBlocks.MIRAGE_BLOCK
            ).build(null));

    public static <T extends BlockEntityType> RegistryObject<T> registerBlockEntities(String name, Supplier<T> block){
        return BLOCK_ENTITIES.register(name,block);
    }

    public static void registerAll(IEventBus eventBus) {
        Mirage.LOGGER.info("Registering Mod BlockEntities for " + Mirage.MOD_ID);
        BLOCK_ENTITIES.register(eventBus);
    }
}
