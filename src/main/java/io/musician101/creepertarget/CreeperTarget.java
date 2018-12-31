package io.musician101.creepertarget;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = CreeperTarget.MOD_ID, name = CreeperTarget.MOD_NAME, version = CreeperTarget.VERSION, serverSideOnly = true, acceptableRemoteVersions = "*")
public class CreeperTarget {

    public static final String MOD_ID = "creeper_target";
    public static final String MOD_NAME = "CreeperTarget";
    public static final String VERSION = "@VERSION@";

    @Instance(MOD_ID)
    public static CreeperTarget INSTANCE;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private final List<String> targets = new ArrayList<>();
    private boolean ignoreSuperCreepers;
    private File configDir;
    private Logger logger;

    public List<String> getTargets() {
        return targets;
    }

    public boolean ignoreSuperCreepers() {
        return ignoreSuperCreepers;
    }

    public void loadConfig() {
        targets.clear();
        File configFile = new File(configDir, "creeper_target.json");
        if (!configFile.exists()) {
            try {
                configDir.mkdirs();
                configFile.createNewFile();
                FileWriter writer = new FileWriter(configFile);
                JsonObject config = new JsonObject();
                config.addProperty("ignore_super_creepers", true);
                JsonArray targets = new JsonArray();
                targets.add("Musician101");
                config.add("targets", targets);
                gson.toJson(config, writer);
                writer.close();
            }
            catch (Exception e) {
                logger.error("Failed to create default config!", e);
            }

        }

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject config = gson.fromJson(reader, JsonObject.class);
            ignoreSuperCreepers = config.get("ignore_super_creepers").getAsBoolean();
            targets.addAll(gson.fromJson(config.get("targets"), new TypeToken<List<String>>() {

            }.getType()));
        }
        catch (Exception e) {
            logger.error("Failed to read the config!", e);
        }
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configDir = event.getModConfigurationDirectory();
        logger = event.getModLog();
        loadConfig();
        MinecraftForge.EVENT_BUS.register(new CreeperListener());
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new ReloadCommand());
    }
}
