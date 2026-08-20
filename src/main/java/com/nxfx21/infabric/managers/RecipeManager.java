package com.nxfx21.infabric.managers;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.InfuseEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class RecipeManager {
    private final Infuse plugin;
    private final File recipesFile;
    private Map<String, Object> config = new HashMap<>();
    private boolean enderRecipeUpdated = false;

    public RecipeManager() {
        this.plugin = Infuse.getInstance();
        this.recipesFile = new File(plugin.getDataFolder(), "recipes.yml");
        load();
    }

    @SuppressWarnings("unchecked")
    public void load() {
        if (!recipesFile.getParentFile().exists()) {
            recipesFile.getParentFile().mkdirs();
        }
        if (!recipesFile.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/recipes.yml")) {
                if (in != null) {
                    Files.copy(in, recipesFile.toPath());
                } else {
                    recipesFile.createNewFile();
                }
            } catch (IOException e) {
                Infuse.LOGGER.error("Could not copy default recipes.yml", e);
            }
        }

        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(recipesFile)) {
            Object loaded = yaml.load(in);
            if (loaded instanceof Map<?, ?> map) {
                config = (Map<String, Object>) map;
            } else {
                config = new HashMap<>();
            }
            Infuse.LOGGER.info("Successfully loaded recipes.yml");
        } catch (Exception e) {
            Infuse.LOGGER.error("Could not load recipes.yml", e);
        }
    }

    public void save() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try (Writer writer = new FileWriter(recipesFile)) {
            yaml.dump(config, writer);
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not save recipes.yml", e);
        }
    }

    public void reload() {
        load();
        updateEnderRecipe();
    }

    public void registerRecipes() {
        reload();
    }

    public boolean isRecipeEnabled(InfuseEffect mapping) {
        if (mapping == null) return false;
        String key = mapping.getRegularVersion().getKey();
        Object val = config.get(key);
        if (val instanceof Map<?, ?> map) {
            Object enabled = map.get("enabled");
            if (enabled instanceof Boolean b) return b;
            if (enabled != null) return Boolean.parseBoolean(String.valueOf(enabled));
        }
        return false;
    }

    public List<String> getRecipeShape(InfuseEffect mapping) {
        if (mapping == null) return Collections.emptyList();
        String key = mapping.getRegularVersion().getKey();
        Object val = config.get(key);
        if (val instanceof Map<?, ?> map) {
            Object shape = map.get("shape");
            if (shape instanceof List<?> list) {
                List<String> rows = new ArrayList<>();
                for (Object o : list) rows.add(String.valueOf(o));
                return rows;
            }
        }
        return Collections.emptyList();
    }

    public Map<Character, Item> getRecipeIngredients(InfuseEffect mapping) {
        Map<Character, Item> ingredients = new HashMap<>();
        if (mapping == null) return ingredients;
        String key = mapping.getRegularVersion().getKey();
        Object val = config.get(key);
        if (val instanceof Map<?, ?> map) {
            Object ingObj = map.get("ingredients");
            if (ingObj instanceof Map<?, ?> ingMap) {
                for (Map.Entry<?, ?> entry : ingMap.entrySet()) {
                    String charKey = String.valueOf(entry.getKey());
                    if (charKey.isEmpty()) continue;
                    char c = charKey.charAt(0);
                    String matName = String.valueOf(entry.getValue()).toLowerCase();

                    // Check if ender recipe replacement applies
                    if (mapping.getId() == com.nxfx21.infabric.EffectIds.ENDER && enderRecipeUpdated && "dragon_egg".equals(matName)) {
                        Object eggRep = map.get("egg_replacement");
                        if (eggRep != null) {
                            matName = String.valueOf(eggRep).toLowerCase();
                        }
                    }

                    Item item = getItemFromName(matName);
                    if (item != null && item != Items.AIR) {
                        ingredients.put(c, item);
                    }
                }
            }
        }
        return ingredients;
    }

    public static Item getItemFromName(String name) {
        if (name == null) return Items.AIR;
        String cleaned = name.trim().toLowerCase().replace("minecraft:", "");
        if (cleaned.equals("potion")) return Items.POTION;
        return Registries.ITEM.get(Identifier.of("minecraft", cleaned));
    }

    public void updateEnderRecipe() {
        InfuseEffect augEnder = InfuseEffect.fromString("aug_ender");
        if (augEnder != null && plugin != null && plugin.getDataManager() != null) {
            int craftedCount = plugin.getDataManager().getExistingCount(augEnder);
            if (craftedCount > 0) {
                enderRecipeUpdated = true;
                Infuse.LOGGER.info("Ender recipe updated: dragon egg requirement removed.");
            }
        }
    }

    public boolean isEnderRecipeUpdated() {
        return enderRecipeUpdated;
    }

    public ItemStack getItemToCraft(InfuseEffect effect) {
        if (effect == null) return null;
        if (plugin == null || plugin.getMainConfig() == null || plugin.getDataManager() == null) {
            return effect.createItem();
        }

        InfuseEffect regular = effect.getRegularVersion();
        InfuseEffect augmented = effect.getAugmentedVersion();

        int augCrafted = plugin.getDataManager().getExistingCount(augmented);
        int augLimit = plugin.getMainConfig().getCraftLimit(augmented);
        if (augLimit > augCrafted) {
            return augmented.createItem();
        }

        int regCrafted = plugin.getDataManager().getExistingCount(regular);
        int regLimit = plugin.getMainConfig().getCraftLimit(regular);
        if (regLimit > regCrafted) {
            return regular.createItem();
        }

        return null;
    }

    /**
     * Checks if a 3x3 crafting grid matches an effect recipe.
     */
    public InfuseEffect matchRecipe(List<ItemStack> grid) {
        if (grid.size() != 9) return null;

        for (InfuseEffect effect : InfuseEffect.getRegisteredEffects().values()) {
            if (effect.isAugmented()) continue;
            if (!isRecipeEnabled(effect)) continue;

            List<String> shape = getRecipeShape(effect);
            Map<Character, Item> ingredients = getRecipeIngredients(effect);
            if (shape.size() != 3) continue;

            boolean match = true;
            for (int row = 0; row < 3; row++) {
                String rowStr = shape.get(row);
                for (int col = 0; col < 3; col++) {
                    char expectedChar = col < rowStr.length() ? rowStr.charAt(col) : ' ';
                    ItemStack actualStack = grid.get(row * 3 + col);
                    if (expectedChar == ' ') {
                        if (!actualStack.isEmpty()) {
                            match = false;
                            break;
                        }
                    } else {
                        Item expectedItem = ingredients.get(expectedChar);
                        if (expectedItem == null || actualStack.isEmpty() || !actualStack.isOf(expectedItem)) {
                            match = false;
                            break;
                        }
                    }
                }
                if (!match) break;
            }

            if (match) {
                return effect;
            }
        }
        return null;
    }
}
