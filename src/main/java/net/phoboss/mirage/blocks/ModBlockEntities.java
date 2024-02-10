package net.phoboss.mirage.blocks;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.phoboss.mirage.Mirage;
import net.phoboss.mirage.blocks.mirageprojector.MirageBlockEntity;

public class ModBlockEntities {

    public static BlockEntityType<MirageBlockEntity> MIRAGE_BLOCK;

    public static void registerAll(){
        MIRAGE_BLOCK = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                new Identifier(Mirage.MOD_ID,"mirage_block"),
                FabricBlockEntityTypeBuilder.create(
                        MirageBlockEntity::new,
                        ModBlocks.MIRAGE_BLOCK).build(null));
    }
}
