package net.phoboss.mirage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import net.phoboss.mirage.blocks.ModBlockEntities;
import net.phoboss.mirage.blocks.ModBlocks;
import net.phoboss.mirage.items.ModItemGroups;
import net.phoboss.mirage.items.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.GeckoLib;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Mirage implements ModInitializer {

	public static final String MOD_ID = "mirage";
    public static final Logger LOGGER = LoggerFactory.getLogger("mirage");
	public static Path SCHEMATICS_FOLDER;
	public static Path CONFIG_FILE;

	@Override
	public void onInitialize() {
		GeckoLib.initialize();

		initConfigFile();
		initSchematicsFolder();

		ModItemGroups.registerAll();
		ModBlocks.registerAll();
		ModBlockEntities.registerAll();
		ModItems.registerAll();
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
			return;
		}
		JsonObject mirageConfig = new JsonObject();
		mirageConfig.addProperty("schematicsDirectoryName", "schematics");

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		try (JsonWriter writer = new JsonWriter(new FileWriter(CONFIG_FILE.toFile()))) {
			gson.toJson(mirageConfig, mirageConfig.getClass(), writer);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(),e);
		}
	}
	public static void initSchematicsFolder(){
		try{
			JsonObject configs = JsonParser.parseReader(new FileReader(CONFIG_FILE.toFile())).getAsJsonObject();
			String directoryName = configs.get("schematicsDirectoryName").getAsString();
			SCHEMATICS_FOLDER = FabricLoader.getInstance().getGameDir().resolve(directoryName);
			initFolder(SCHEMATICS_FOLDER);
		}catch(Exception e){
			LOGGER.error(e.getMessage(),e);
		}
	}
}