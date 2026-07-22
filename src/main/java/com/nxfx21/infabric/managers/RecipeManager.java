package com.nxfx21.infabric.managers;

import com.nxfx21.infabric.Infuse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RecipeManager {
    private final Infuse plugin;
    private final File recipesFile;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private JsonObject config = new JsonObject();

    public RecipeManager() {
        this.plugin = Infuse.getInstance();
        this.recipesFile = new File(plugin.getDataFolder(), "recipes.json");
        load();
    }

    public void load() {
        if (!recipesFile.getParentFile().exists()) {
            recipesFile.getParentFile().mkdirs();
        }
        if (!recipesFile.exists()) {
            save(); // Save empty defaults
        }
        try (FileReader reader = new FileReader(recipesFile)) {
            config = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not load recipes.json", e);
        }
    }

    public void save() {
        try {
            if (!recipesFile.exists()) {
                recipesFile.getParentFile().mkdirs();
                recipesFile.createNewFile();
            }
            try (FileWriter writer = new FileWriter(recipesFile)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not save recipes.json", e);
        }
    }

    public void reload() {
        load();
    }

    public boolean isRecipeEnabled(com.nxfx21.infabric.effects.InfuseEffect mapping) {
        String key = mapping.getRegularVersion().getKey();
        if (config.has(key) && config.getAsJsonObject(key).has("enabled")) {
            return config.getAsJsonObject(key).get("enabled").getAsBoolean();
        }
        return false;
    }

    // Dynamic recipe registration and management is handled differently in Fabric.
    // For now, we stub these methods and rely on the crafting event to validate or generate items.
    
    public void registerRecipes() {
        Infuse.LOGGER.info("Recipe registration should be done via datapacks in Fabric, but dynamically controlled.");
    }

    public void updateEnderRecipe() {
        Infuse.LOGGER.info("Ender recipe update stubbed.");
    }
}
