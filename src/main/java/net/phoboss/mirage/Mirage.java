package net.phoboss.mirage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.phoboss.mirage.blocks.ModBlockEntities;
import net.phoboss.mirage.blocks.ModBlocks;
import net.phoboss.mirage.items.ModItems;
import net.phoboss.mirage.network.MirageNBTPacketHandler;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib3.GeckoLib;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Mirage implements ModInitializer {

	public static final String MOD_ID = "mirage";
    public static final Logger LOGGER = LoggerFactory.getLogger("mirage");
	public static Path SCHEMATICS_FOLDER;
	public static Path CONFIG_FILE;

	public static JsonObject CONFIGS;

	public static ExecutorService SERVER_THREAD_POOL;
	public static ExecutorService CLIENT_THREAD_POOL;
	@Override
	public void onInitialize() {
		GeckoLib.initialize();

		initConfigFile();
		initSchematicsFolder();

		ModBlocks.registerAll();
		ModBlockEntities.registerAll();
		ModItems.registerAll();
		MirageNBTPacketHandler.registerC2SPackets();
		initServerThread();
	}

	public static void initServerThread(){
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			System.gc();
			Mirage.SERVER_THREAD_POOL = Executors.newFixedThreadPool(2,new BasicThreadFactory.Builder()
					.namingPattern("ServerMirageLoader-%d")
					.priority(Thread.MAX_PRIORITY)
					.build());
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			Mirage.SERVER_THREAD_POOL.shutdownNow();
		});
	}

	public static void initFolder(Path path){
		try {
			Files.createDirectories(path);
		}catch(Exception e){
			LOGGER.error(e.getMessage(),e);
		}
	}
	public static void initConfigFile(){
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve("mirage");
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
			SCHEMATICS_FOLDER = FabricLoader.getInstance().getGameDir().resolve(directoryName);
			initFolder(SCHEMATICS_FOLDER);
		}catch(Exception e){
			LOGGER.error(e.getMessage(),e);
		}
	}
}