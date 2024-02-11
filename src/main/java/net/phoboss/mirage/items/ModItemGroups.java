package net.phoboss.mirage.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.phoboss.decobeacons.DecoBeacons;
import net.phoboss.mirage.blocks.ModBlocks;


public class ModItemGroups {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS  = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DecoBeacons.MOD_ID);

    public static final RegistryObject<CreativeModeTab> DECO_BEACON = CREATIVE_MODE_TABS.register("mirage",
            ()-> CreativeModeTab.builder().icon(()-> new ItemStack(ModBlocks.MIRAGE_BLOCK.get()))
                    .title(Component.translatable("itemGroup.mirage"))
                    .displayItems((parameters,output) ->{
                        output.accept(ModBlocks.MIRAGE_BLOCK.get());
                    })
                    .build());

    public static void registerAll(IEventBus eventBus){
        CREATIVE_MODE_TABS.register(eventBus);
    }


}
