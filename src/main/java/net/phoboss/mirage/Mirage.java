package net.phoboss.mirage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.phoboss.mirage.blocks.ModBlockEntities;
import net.phoboss.mirage.blocks.ModBlocks;
import net.phoboss.mirage.client.rendering.ModRendering;
import net.phoboss.mirage.items.ModItemGroups;
import net.phoboss.mirage.items.ModItems;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Mirage.MOD_ID)
public class Mirage
{
    public static final String MOD_ID = "mirage";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static Path SCHEMATICS_FOLDER;
    public static Path CONFIG_FILE;

    public static JsonObject CONFIGS;

    public static ExecutorService THREAD_POOL;

    public Mirage()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItemGroups.registerAll(eventBus);

        ModBlocks.registerAll(eventBus);
        ModItems.registerAll(eventBus);

        ModBlockEntities.registerAll(eventBus);

        eventBus.addListener(this::setup);
        eventBus.addListener(this::setupClient);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setupClient(final FMLCommonSetupEvent event)
    {

    }

    private void setup(final FMLCommonSetupEvent event)
    {
        initConfigFile();
        initSchematicsFolder();
    }

    @Mod.EventBusSubscriber(modid = Mirage.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public class ClientModRegistryEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event){
            ModRendering.registerAll();
        }
    }

    @Mod.EventBusSubscriber(modid = Mirage.MOD_ID, value = Dist.CLIENT)
    public class ClientModEvents {
        @SubscribeEvent
        public static void onWorldLoad(LevelEvent.Load event){
            if(event.getLevel().isClientSide()) {
                System.gc();
                THREAD_POOL = Executors.newFixedThreadPool(2,new BasicThreadFactory.Builder()
                        .namingPattern("MirageLoader-%d")
                        .priority(Thread.MAX_PRIORITY)
                        .build());
            }
        }
        @SubscribeEvent
        public static void onWorldUnload(LevelEvent.Unload event){
            if(event.getLevel().isClientSide()) {
                THREAD_POOL.shutdownNow();
            }
        }

    }

    public static void initFolder(Path path){
        try {
            Files.createDirectories(path);
        }catch(Exception e){
            LOGGER.error(e.getMessage(),e);
        }
    }
    public static void initConfigFile(){
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("mirage");
        initFolder(configPath);
        CONFIG_FILE = configPath.resolve("mirage_config.json");
        if(CONFIG_FILE.toFile().exists()){
            try{
                CONFIGS = JsonParser.parseReader(new FileReader(CONFIG_FILE.toFile())).getAsJsonObject();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(),e);
            }
            return;
        }
        JsonObject mirageConfig = new JsonObject();
        mirageConfig.addProperty("schematicsDirectoryName", "schematics");
        mirageConfig.addProperty("enableRecursiveMirage", false);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (JsonWriter writer = new JsonWriter(new FileWriter(CONFIG_FILE.toFile()))) {
            gson.toJson(mirageConfig, mirageConfig.getClass(), writer);
            CONFIGS = mirageConfig;
        } catch (IOException e) {
            LOGGER.error(e.getMessage(),e);
        }
    }
    public static void initSchematicsFolder(){
        try{
            String directoryName = CONFIGS.get("schematicsDirectoryName").getAsString();
            SCHEMATICS_FOLDER = FMLPaths.GAMEDIR.get().resolve(directoryName);
            initFolder(SCHEMATICS_FOLDER);
        }catch(Exception e){
            LOGGER.error(e.getMessage(),e);
        }
    }
}
