package com.nxfx21.infabric.managers;

import com.nxfx21.infabric.Infuse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class DataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File dataFile;
    private JsonObject config;

    public DataManager() {
        this.dataFile = new File(Infuse.getInstance().getDataFolder(), "data/playerdata.json");
        this.config = new JsonObject();
    }

    public boolean load() {
        if (!dataFile.getParentFile().exists()) {
            dataFile.getParentFile().mkdirs();
        }
        if (!dataFile.exists()) {
            save();
            return true;
        }
        try (FileReader reader = new FileReader(dataFile)) {
            config = JsonParser.parseReader(reader).getAsJsonObject();
            Infuse.LOGGER.info("Successfully loaded playerdata.json");
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not load playerdata.json", e);
            return false;
        }
    }

    public boolean save() {
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }
            try (FileWriter writer = new FileWriter(dataFile)) {
                GSON.toJson(config, writer);
            }
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not save playerdata.json", e);
            return false;
        }
    }

    private JsonObject getOrCreateObject(String key) {
        if (!config.has(key) || !config.get(key).isJsonObject()) {
            config.add(key, new JsonObject());
        }
        return config.getAsJsonObject(key);
    }

    public int getExistingCount(com.nxfx21.infabric.effects.InfuseEffect effect) {
        JsonObject existing = getOrCreateObject("existing-effects");
        return existing.has(effect.toString()) ? existing.get(effect.toString()).getAsInt() : 0;
    }

    public void setExistingCount(com.nxfx21.infabric.effects.InfuseEffect effect, int crafted) {
        JsonObject existing = getOrCreateObject("existing-effects");
        existing.addProperty(effect.toString(), crafted);
        save();
    }

    public List<UUID> getTrusted(UUID truster) {
        JsonObject pData = getOrCreateObject(truster.toString());
        List<UUID> trusted = new ArrayList<>();
        if (pData.has("trust")) {
            JsonArray arr = pData.getAsJsonArray("trust");
            for (JsonElement el : arr) {
                trusted.add(UUID.fromString(el.getAsString()));
            }
        }
        return trusted;
    }

    public void setTrusted(UUID truster, List<UUID> trusted) {
        JsonObject pData = getOrCreateObject(truster.toString());
        JsonArray arr = new JsonArray();
        for (UUID t : trusted) {
            arr.add(t.toString());
        }
        pData.add("trust", arr);
        save();
    }

    public void addTrust(UUID caster, UUID toTrust) {
        List<UUID> trustedPlayers = getTrusted(caster);
        if (!trustedPlayers.contains(toTrust)) {
            trustedPlayers.add(toTrust);
            setTrusted(caster, trustedPlayers);
        }
    }

    public void removeTrust(UUID caster, UUID trusted) {
        List<UUID> trustedSet = getTrusted(caster);
        trustedSet.remove(trusted);
        setTrusted(caster, trustedSet);
    }

    public boolean isTrusted(UUID caster, UUID trusted) {
        if (caster == null || trusted == null) return false;
        if (caster.equals(trusted)) return true;
        return getTrusted(caster).contains(trusted);
    }

    public void setEffect(UUID playerUUID, String slot, @Nullable com.nxfx21.infabric.effects.InfuseEffect effect) {
        JsonObject pData = getOrCreateObject(playerUUID.toString());
        if (effect == null) {
            pData.remove(slot);
        } else {
            pData.addProperty(slot, effect.toString());
        }
        save();
    }

    @Nullable
    public com.nxfx21.infabric.effects.InfuseEffect getEffect(UUID playerUUID, String slot) {
        JsonObject pData = getOrCreateObject(playerUUID.toString());
        if (!pData.has(slot)) return null;
        String effectKey = pData.get(slot).getAsString();
        com.nxfx21.infabric.effects.InfuseEffect effect = com.nxfx21.infabric.effects.InfuseEffect.fromString(effectKey);
        if (effectKey != null && effect == null) {
            Infuse.LOGGER.warn("No valid ability found for the equipped effect: " + effectKey);
        }
        return effect;
    }

    public boolean hasEffect(UUID player, com.nxfx21.infabric.effects.InfuseEffect effect) {
        return hasEffect(player, effect, false);
    }

    public boolean hasEffect(UUID player, com.nxfx21.infabric.effects.InfuseEffect effect, boolean differentiateAugmented) {
        return hasEffect(player, effect, differentiateAugmented, "1") || hasEffect(player, effect, differentiateAugmented, "2");
    }

    public boolean hasEffect(UUID player, com.nxfx21.infabric.effects.InfuseEffect effect, String slot) {
        return hasEffect(player, effect, false, slot);
    }

    public boolean hasEffect(net.minecraft.server.network.ServerPlayerEntity player, com.nxfx21.infabric.effects.InfuseEffect effect) {
        return hasEffect(player.getUuid(), effect);
    }

    public boolean hasEffect(UUID player, com.nxfx21.infabric.effects.InfuseEffect effect, boolean differentiateAugmented, String slot) {
        com.nxfx21.infabric.effects.InfuseEffect equippedEffect = getEffect(player, slot);
        if (equippedEffect == null) return false;
        if (differentiateAugmented) {
            return effect.equals(equippedEffect);
        }
        return effect.getId() == equippedEffect.getId();
    }

    public boolean hasAnyData(UUID playerUUID) {
        return config.has(playerUUID.toString());
    }

    public void removeEffect(UUID playerUUID, String slot) {
        JsonObject pData = getOrCreateObject(playerUUID.toString());
        pData.remove(slot);
        save();
    }

    public void setControlMode(UUID playerUUID, String defaultMode) {
        JsonObject pData = getOrCreateObject(playerUUID.toString());
        pData.addProperty("controls", defaultMode);
        save();
    }

    public String getControlMode(UUID playerUUID) {
        JsonObject pData = getOrCreateObject(playerUUID.toString());
        return pData.has("controls") ? pData.get("controls").getAsString() : "offhand";
    }

    public void applyUpdates() {
        save();
    }
}