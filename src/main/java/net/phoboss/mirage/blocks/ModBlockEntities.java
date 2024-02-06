package net.phoboss.mirage.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static BlockEntityType<MirageBlockEntity> MIRAGE_BLOCK;

    public static void registerAll(){
        MIRAGE_BLOCK = Registry.register(
                Registry.BLOCK_ENTITY_TYPE,
                new Identifier(Mirage.MOD_ID,"mirage_block"),
                FabricBlockEntityTypeBuilder.create(
                        MirageBlockEntity::new,
                        ModBlocks.MIRAGE_BLOCK).build(null));
    }
}
