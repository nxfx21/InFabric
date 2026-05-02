package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.commands.InfuseCommandManager;
import com.catadmirer.infuseSMP.managers.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

public class Infuse implements ModInitializer {
    public static final String MOD_ID = "infusesmp";
    public static final Logger LOGGER = LoggerFactory.getLogger("Infuse");
    public static final Identifier EFFECT_KEY = Identifier.of(MOD_ID, "effect_key");

    private static Infuse instance;
    private MainConfig mainConfig;
    private File dataFolder;
    
    // Managers
    private DataManager dataManager;
    private GlobalLoop loop;
    private RecipeManager recipeManager;
    private ParticleManager particleManager;
    private InfuseCommandManager commandManager;
    private HitTracker hitTracker;
    private Drop dropManager;
    private ActionBarUpdater actionBarUpdater;
    private EffectCraftManager effectCraftManager;

    public static Infuse getInstance() {
        return instance;
    }

    @Override
    public void onInitialize() {
        instance = this;
        dataFolder = new File("config/infusesmp");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Initialize configs
        this.mainConfig = new MainConfig(dataFolder);
        mainConfig.load();
        MessageConfig.load();

        // Initialize Managers
        this.dataManager = new DataManager();
        this.loop = new GlobalLoop();
        this.recipeManager = new RecipeManager();
        this.particleManager = new ParticleManager();
        this.commandManager = new InfuseCommandManager();
        this.hitTracker = new HitTracker();
        this.dropManager = new Drop();
        this.actionBarUpdater = new ActionBarUpdater();
        this.effectCraftManager = new EffectCraftManager();

        // Register Commands & Events
        commandManager.register();
        hitTracker.registerEvents();
        dropManager.registerEvents();
        effectCraftManager.registerEvents();

        // Register TenHit listeners
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Fire::onTenHit);
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Frost::onTenHit);
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Invisibility::onTenHit);
        
        // Start loops
        loop.start();
        actionBarUpdater.start();

        // Register Server Startup Event
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("InfuseSMP server started.");
            recipeManager.registerRecipes();
        });

        LOGGER.info("InfuseSMP Fabric Mod Initialized!");
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public DataManager getDataManager() { return dataManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
    public ParticleManager getParticleManager() { return particleManager; }
    public HitTracker getHitTracker() { return hitTracker; }
    public Drop getDropManager() { return dropManager; }
}