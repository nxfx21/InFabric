package com.nxfx21.infabric.managers;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.InfuseEffect;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.item.ItemStack;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RecipeManager {
    private final Infuse plugin;
    private final File recipesFile;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private JsonObject config = new JsonObject();
    private final Map<String, JsonObject> activeRecipes = new HashMap<>();
    private boolean enderRecipeUpdated = false;

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
            initDefaultConfig();
            save();
        } else {
            try (FileReader reader = new FileReader(recipesFile)) {
                config = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception e) {
                Infuse.LOGGER.error("Could not load recipes.json", e);
                initDefaultConfig();
            }
        }
        registerRecipes();
    }

    private void initDefaultConfig() {
        config = new JsonObject();
        String[] defaultKeys = {
            "emerald", "ender", "feather", "fire", "frost", "haste",
            "heart", "invis", "ocean", "regen", "speed", "strength",
            "thunder", "apophis", "thief"
        };
        for (String key : defaultKeys) {
            JsonObject recipeObj = new JsonObject();
            recipeObj.addProperty("enabled", true);
            if ("ender".equals(key)) {
                recipeObj.addProperty("egg_replacement", "crying_obsidian");
            }
            config.add(key, recipeObj);
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
        updateEnderRecipe();
    }

    public boolean isRecipeEnabled(InfuseEffect mapping) {
        if (mapping == null) return false;
        String key = mapping.getRegularVersion().getKey();
        if (config.has(key) && config.getAsJsonObject(key).has("enabled")) {
            return config.getAsJsonObject(key).get("enabled").getAsBoolean();
        }
        return true;
    }

    public void registerRecipes() {
        activeRecipes.clear();
        for (InfuseEffect effect : InfuseEffect.getRegisteredEffects().values()) {
            if (effect.isAugmented()) continue;
            String key = effect.getKey();
            if (isRecipeEnabled(effect)) {
                JsonObject obj = config.has(key) ? config.getAsJsonObject(key) : new JsonObject();
                obj.addProperty("enabled", true);
                activeRecipes.put(key, obj);
            }
        }
        Infuse.LOGGER.info("Registered {} dynamic recipes.", activeRecipes.size());
    }

    public void updateEnderRecipe() {
        InfuseEffect augEnder = InfuseEffect.fromString("aug_ender");
        Infuse pluginInstance = Infuse.getInstance();
        if (augEnder != null && pluginInstance != null && pluginInstance.getDataManager() != null) {
            int craftedCount = pluginInstance.getDataManager().getExistingCount(augEnder);
            if (craftedCount > 0) {
                enderRecipeUpdated = true;
                if (config.has("ender")) {
                    JsonObject enderConfig = config.getAsJsonObject("ender");
                    enderConfig.addProperty("dragon_egg_required", false);
                    save();
                }
                Infuse.LOGGER.info("Ender recipe updated: dragon egg requirement removed.");
            }
        }
    }

    public boolean isEnderRecipeUpdated() {
        return enderRecipeUpdated;
    }

    public ItemStack getItemToCraft(InfuseEffect effect) {
        if (effect == null) return null;
        Infuse pluginInstance = Infuse.getInstance();
        if (pluginInstance == null || pluginInstance.getMainConfig() == null || pluginInstance.getDataManager() == null) {
            return effect.createItem();
        }

        InfuseEffect regular = effect.getRegularVersion();
        InfuseEffect augmented = effect.getAugmentedVersion();

        int augCrafted = pluginInstance.getDataManager().getExistingCount(augmented);
        int augLimit = pluginInstance.getMainConfig().getCraftLimit(augmented);
        if (augLimit > augCrafted) {
            return augmented.createItem();
        }

        int regCrafted = pluginInstance.getDataManager().getExistingCount(regular);
        int regLimit = pluginInstance.getMainConfig().getCraftLimit(regular);
        if (regLimit > regCrafted) {
            return regular.createItem();
        }

        return null;
    }
}
