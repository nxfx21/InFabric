package com.nxfx21.infabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MessageConfig {
    public static final File file = new File(Infuse.getInstance().getDataFolder(), "messages.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static JsonObject config = new JsonObject();

    public static boolean load() {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            save(); // Save defaults when not found
        }
        try (FileReader reader = new FileReader(file)) {
            config = JsonParser.parseReader(reader).getAsJsonObject();
            
            // Migration for renamed keys
            if (config.has("infuse_cleareffect_usage")) {
                config.add("infuse_cleareffects_usage", config.remove("infuse_cleareffect_usage"));
            }
            if (config.has("infuse_cleareffect_success")) {
                config.add("infuse_cleareffects_success", config.remove("infuse_cleareffect_success"));
            }
            save();

            Infuse.LOGGER.info("Successfully loaded messages.json");
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not load messages.json", e);
            return false;
        }
    }

    public static boolean save() {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(config, writer);
            }
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not save messages.json", e);
            return false;
        }
    }

    public static String getMessage(Message.MessageType messageType) {
        String key = messageType.configKey;
        if (!config.has(key)) {
            Infuse.LOGGER.warn("Could not find \"{}\" in messages.json, using default.", key);
            if (messageType.defaultValue.contains("\n")) {
                JsonArray array = new JsonArray();
                for (String line : messageType.defaultValue.split("\n")) {
                    array.add(line);
                }
                config.add(key, array);
            } else {
                config.addProperty(key, messageType.defaultValue);
            }
            save();
            return messageType.defaultValue;
        }

        JsonElement element = config.get(key);
        if (element.isJsonArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonElement el : element.getAsJsonArray()) {
                sb.append(el.getAsString()).append("\n");
            }
            if (sb.length() > 0) sb.setLength(sb.length() - 1);
            return sb.toString();
        } else {
            return element.getAsString();
        }
    }

    public static void applyUpdates() {
        save();
    }
}
