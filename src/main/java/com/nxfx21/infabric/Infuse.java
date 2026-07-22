package com.nxfx21.infabric;

import com.nxfx21.infabric.commands.InfuseCommandManager;
import com.nxfx21.infabric.managers.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

public class Infuse implements ModInitializer {
    public static final String MOD_ID = "infabric";
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
        dataFolder = new File("config/infabric");
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
        com.nxfx21.infabric.effects.Ender.registerEvents();

        // Register connection events
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            effectManager.handleJoin(handler.getPlayer());
            com.nxfx21.infabric.effects.Frost.onPlayerJoin(handler.getPlayer());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            effectManager.handleQuit(handler.getPlayer());
        });

        // Register TenHit listeners
        com.nxfx21.infabric.events.TenHitEvent.EVENT.register(com.nxfx21.infabric.effects.Fire::onTenHit);
        com.nxfx21.infabric.events.TenHitEvent.EVENT.register(com.nxfx21.infabric.effects.Frost::onTenHit);
        com.nxfx21.infabric.events.TenHitEvent.EVENT.register(com.nxfx21.infabric.effects.Invis::onTenHit);
        com.nxfx21.infabric.events.TenHitEvent.EVENT.register(com.nxfx21.infabric.effects.Emerald::onTenHit);
        com.nxfx21.infabric.events.TenHitEvent.EVENT.register(com.nxfx21.infabric.effects.Feather::onTenHit);
        com.nxfx21.infabric.events.TenHitEvent.EVENT.register(com.nxfx21.infabric.effects.Heart::onTenHit);
        com.nxfx21.infabric.events.TenHitEvent.EVENT.register(com.nxfx21.infabric.effects.Regen::onTenHit);
        com.nxfx21.infabric.events.TenHitEvent.EVENT.register(com.nxfx21.infabric.effects.Thunder::onTenHit);
        com.nxfx21.infabric.events.TenHitEvent.EVENT.register(com.nxfx21.infabric.effects.Apophis::onTenHit);
        
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
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Emerald());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Ender());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Feather());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Fire());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Frost());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Haste());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Heart());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Invis());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Ocean());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Regen());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Speed());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Strength());
        com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Thunder());

        if (mainConfig.enableApophis()) {
            com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Apophis());
        }
        if (mainConfig.enableThief()) {
            com.nxfx21.infabric.effects.InfuseEffect.register(new com.nxfx21.infabric.effects.Thief());
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