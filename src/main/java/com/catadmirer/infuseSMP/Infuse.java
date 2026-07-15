package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.commands.InfuseCommandManager;
import com.catadmirer.infuseSMP.managers.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
    private EffectManager effectManager;
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

        // Register effects first, just like upstream
        registerEffects();

        // Initialize Managers
        this.dataManager = new DataManager();
        this.effectManager = new EffectManager(this);
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
        PlayerDeathListener.register();

        // Register connection events
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            effectManager.handleJoin(handler.getPlayer());
            com.catadmirer.infuseSMP.effects.Frost.onPlayerJoin(handler.getPlayer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            effectManager.handleQuit(handler.getPlayer());
        });

        // Register TenHit listeners
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Fire::onTenHit);
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Frost::onTenHit);
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Invis::onTenHit);
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Emerald::onTenHit);
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Feather::onTenHit);
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Heart::onTenHit);
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Regen::onTenHit);
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Thunder::onTenHit);
        com.catadmirer.infuseSMP.events.TenHitEvent.EVENT.register(com.catadmirer.infuseSMP.effects.Apophis::onTenHit);
        
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

    private void registerEffects() {
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Emerald());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Ender());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Feather());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Fire());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Frost());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Haste());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Heart());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Invis());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Ocean());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Regen());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Speed());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Strength());
        com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Thunder());

        if (mainConfig.enableApophis()) {
            com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Apophis());
        }
        if (mainConfig.enableThief()) {
            com.catadmirer.infuseSMP.effects.InfuseEffect.register(new com.catadmirer.infuseSMP.effects.Thief());
        }
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public DataManager getDataManager() { return dataManager; }
    public EffectManager getEffectManager() { return effectManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
    public ParticleManager getParticleManager() { return particleManager; }
    public HitTracker getHitTracker() { return hitTracker; }
    public Drop getDropManager() { return dropManager; }
}