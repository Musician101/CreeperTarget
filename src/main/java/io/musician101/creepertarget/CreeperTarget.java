package io.musician101.creepertarget;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CreeperTarget.MOD_ID)
public class CreeperTarget {

    public static final String MOD_ID = "creeper_target";
    public static final String MOD_NAME = "CreeperTarget";
    public static final String VERSION = "@VERSION@";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private final List<String> targets = new ArrayList<>();
    private boolean ignoreSuperCreepers;
    private File configDir;
    private final Logger logger = LogManager.getLogger(MOD_NAME);

    public CreeperTarget() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onSpawn);
    }

    public static CreeperTarget instance() {
        return ModList.get().getModObjectById(MOD_ID).filter(CreeperTarget.class::isInstance).map(CreeperTarget.class::cast).orElseThrow(() -> new IllegalStateException("ChickenTarget is not enabled."));
    }

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

    private void preInit(FMLCommonSetupEvent event) {
        configDir = new File("config");
        loadConfig();
        NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID), () -> VERSION, s -> true, s -> true);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(LiteralArgumentBuilder.<CommandSource>literal("ctr").requires(source -> source.hasPermissionLevel(3)).executes(context -> {
            loadConfig();
            context.getSource().sendFeedback(new StringTextComponent("CreeperTarget config reloaded."), true);
            return 1;
        }));
    }

    private void onSpawn(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof CreeperEntity)) {
            return;
        }

        CreeperEntity creeper = (CreeperEntity) entity;
        GoalSelector goalSelector = creeper.goalSelector;
        goalSelector.removeGoal(goalSelector.getRunningGoals().collect(Collectors.toList()).iterator().next().getGoal());
        goalSelector.addGoal(1, new CreeperTargetPlayer(creeper));
    }
}
