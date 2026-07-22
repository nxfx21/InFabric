package com.nxfx21.infabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MainConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File file;
    private JsonObject config;

    public MainConfig(File dataFolder) {
        this.file = new File(dataFolder, "config.json");
        this.config = new JsonObject();
    }

    public boolean load() {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            save(); // Save defaults
        }

        try (FileReader reader = new FileReader(file)) {
            config = JsonParser.parseReader(reader).getAsJsonObject();
            Infuse.LOGGER.info("Successfully loaded config.json");
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not read config.json", e);
            return false;
        }
    }

    public boolean save() {
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
            Infuse.LOGGER.error("Could not save config.json", e);
            return false;
        }
    }

    private boolean getBoolean(String path, boolean def) {
        if (config.has(path)) return config.get(path).getAsBoolean();
        config.addProperty(path, def);
        return def;
    }

    private boolean getBooleanOption(String camelPath, String snakePath, boolean def) {
        if (config.has(camelPath)) return config.get(camelPath).getAsBoolean();
        return getBoolean(snakePath, def);
    }

    private int getInt(String path, int def) {
        if (config.has(path)) return config.get(path).getAsInt();
        config.addProperty(path, def);
        return def;
    }

    private double getDouble(String path, double def) {
        if (config.has(path)) return config.get(path).getAsDouble();
        config.addProperty(path, def);
        return def;
    }

    private String getString(String path, String def) {
        if (config.has(path)) return config.get(path).getAsString();
        config.addProperty(path, def);
        return def;
    }

    public boolean allowInfiniteEffects() { return getBoolean("allow_infinite_effects", false); }
    public int ritualDuration() { return getInt("ritual_duration", 100); }
    public int ritualDurationEnder() { return getInt("ritual_duration_ender", 200); }
    public boolean ritualBeacon() { return getBoolean("ritual_beacon", true); }
    public boolean emptyEffectIcon() { return getBoolean("empty_effect_icon", true); }
    public boolean playerHeadDrops() { return getBoolean("player_head_drops", true); }
    public boolean enableDiscordBroadcasts() { return getBoolean("enable_discord_broadcasts", false); }
    public String discordWebhookUrl() { return getString("discord_webhook_url", ""); }
    public boolean brewingGui() { return getBoolean("brewing_gui", true); }
    public String effectDrops() { return getString("effect_drops", "DEFAULT"); }
    public boolean joinEffectsEnabled() { return getBoolean("join_effects_enabled", false); }
    
    public boolean enableApophis() { return getBoolean("extra_effects_Apophis", false); }
    public boolean regularBroadcast() { return getBoolean("regular_effect_broadcast", true); }
    public boolean enableThief() { return getBoolean("extra_effects_Thief", false); }

    public double emeraldLockDurationSeconds() { return getDouble("emerald_lock_duration_seconds", 10.0); }
    public double apophisLockDurationSeconds() { return getDouble("apophis_lock_duration_seconds", 10.0); }
    public double emeraldMultiplierStandard() { return getDouble("emerald_multiplier_standard", 2.0); }
    public double emeraldMultiplierUseEffect() { return getDouble("emerald_multiplier_use_effect", 4.0); }
    public boolean invisHideKills() { return getBoolean("invis_hide_kills", false); }
    public boolean invisHideDeaths() { return getBoolean("invis_hide_deaths", false); }

    public int speedDashMultiplier() { return getInt("speed_dashMultiplier", 2); }
    public double speedPlayerVelocityMultiplier() { return getDouble("speed_playerVelocityMultiplier", 1.5); }
    public int oceanPullInterval() { return getInt("ocean_pulling_pull_interval", 10); }
    public int oceanPullRadius() { return getInt("ocean_pulling_pull_radius", 5); }
    public double oceanPullStrength() { return getDouble("ocean_pulling_pull_strength", 0.5); }
    public double enderPassiveRadius() { return getDouble("ender_passive_radius", 10.0); }
    public int enderSparkMaxDistance() { return getInt("ender_spark_max_distance", 15); }
    public int oceanPassiveDrownStrength() { return getInt("ocean_passive_drown_strength", 5); }
    public int oceanPassiveDrownDamage() { return getInt("ocean_passive_drown_damage", 1); }
    public int oceanSparkDrownStrength() { return getInt("ocean_spark_drown_strength", 20); }
    public int oceanSparkDrownDamage() { return getInt("ocean_spark_drown_damage", 2); }
    public int hitCounterDecaySeconds() { return getInt("hit_counter_decay_seconds", 15); }
    public int emeraldExpPerHit() {
        if (config.has("emeraldExpPerHit")) return config.get("emeraldExpPerHit").getAsInt();
        if (config.has("emerald_exp_per_hit")) return config.get("emerald_exp_per_hit").getAsInt();
        return getInt("emerald_xp_stolen_per_hit", 15);
    }
    public float emeraldExpPercent() { return (float) getDouble("emerald_xp_stolen_percent", 1.0); }
    public float emeraldPercentExpToShare() { return (float) getDouble("emerald_percent_xp_to_share", 0.5); }

    public int apophisLootingLevel() { return getInt("apophis_enchantment_looting_level", 5); }
    public int emeraldLootingLevel() { return getInt("emerald_enchantment_looting_level", 3); }
    public int hasteFortuneLevel() { return getInt("haste_enchantment_fortune_level", 5); }
    public int hasteEfficiencyLevel() { return getInt("haste_enchantment_efficiency_level", 10); }
    public int hasteUnbreakingLevel() { return getInt("haste_enchantment_unbreaking_level", 5); }

    public boolean emeraldPreserveConsumables() { return getBooleanOption("emeraldPreserveConsumables", "emerald_preserve_consumables", true); }
    public boolean emeraldEnchantBonus() { return getBooleanOption("emeraldEnchantBonus", "emerald_enchant_bonus", true); }
    public boolean enderOnehitMobs() { return getBooleanOption("enderOnehitMobs", "ender_onehit_mobs", true); }
    public boolean enderCurseHit() { return getBooleanOption("enderCurseHit", "ender_curse_hit", true); }
    public boolean strengthLengthenShieldCooldown() { return getBooleanOption("strengthLengthenShieldCooldown", "strength_lengthen_shield_cooldown", true); }
    public boolean strengthDoubleDamage() { return getBooleanOption("strengthDoubleDamage", "strength_double_damage", true); }
    public boolean regenCanAlwaysEat() { return getBooleanOption("regenCanAlwaysEat", "regen_can_always_eat", true); }

    public long cooldown(com.nxfx21.infabric.effects.InfuseEffect effect) {
        return getInt("cooldowns." + effect.getKey(), 60);
    }

    public long duration(com.nxfx21.infabric.effects.InfuseEffect effect) {
        return getInt("durations." + effect.getKey(), 30);
    }

    public java.util.List<com.nxfx21.infabric.effects.InfuseEffect> joinEffects() {
        if (!config.has("join_effects") || !config.get("join_effects").isJsonArray()) {
            return java.util.Collections.emptyList();
        }
        com.google.gson.JsonArray arr = config.getAsJsonArray("join_effects");
        java.util.List<com.nxfx21.infabric.effects.InfuseEffect> list = new java.util.ArrayList<>();
        for (com.google.gson.JsonElement el : arr) {
            com.nxfx21.infabric.effects.InfuseEffect eff = com.nxfx21.infabric.effects.InfuseEffect.fromString(el.getAsString());
            if (eff != null) {
                list.add(eff);
            }
        }
        return list;
    }

    public void applyUpdates() {
        save(); // Saves any defaults populated by getter calls
    }
}
