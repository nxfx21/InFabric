package com.nxfx21.infabric;

import com.nxfx21.infabric.effects.InfuseEffect;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class MainConfig {
    private final File dataFolder;
    private final File configFile;
    private Map<String, Object> config = new HashMap<>();

    public MainConfig(File dataFolder) {
        this.dataFolder = dataFolder;
        this.configFile = new File(dataFolder, "config.yml");
    }

    @SuppressWarnings("unchecked")
    public boolean load() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Copy default config from resources if not exists
        if (!configFile.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                } else {
                    configFile.createNewFile();
                }
            } catch (IOException e) {
                Infuse.LOGGER.error("Could not copy default config.yml", e);
            }
        }

        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(configFile)) {
            Object loaded = yaml.load(in);
            if (loaded instanceof Map<?, ?> map) {
                config = (Map<String, Object>) map;
            } else {
                config = new HashMap<>();
            }
            Infuse.LOGGER.info("Successfully loaded config.yml");
            return true;
        } catch (Exception e) {
            Infuse.LOGGER.error("Could not read config.yml", e);
            return false;
        }
    }

    public boolean save() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);

        try (Writer writer = new FileWriter(configFile)) {
            yaml.dump(config, writer);
            return true;
        } catch (IOException e) {
            Infuse.LOGGER.error("Could not save config.yml", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private Object getNested(String path) {
        if (path == null || path.isEmpty()) return null;
        String[] parts = path.split("\\.");
        Map<String, Object> curr = config;
        for (int i = 0; i < parts.length - 1; i++) {
            Object next = curr.get(parts[i]);
            if (!(next instanceof Map<?, ?> map)) {
                return null;
            }
            curr = (Map<String, Object>) map;
        }
        return curr.get(parts[parts.length - 1]);
    }

    public boolean getBoolean(String path, boolean def) {
        Object val = getNested(path);
        if (val instanceof Boolean b) return b;
        if (val != null) return Boolean.parseBoolean(String.valueOf(val));
        return def;
    }

    public int getInt(String path, int def) {
        Object val = getNested(path);
        if (val instanceof Number n) return n.intValue();
        if (val != null) {
            try { return Integer.parseInt(String.valueOf(val)); } catch (Exception ignored) {}
        }
        return def;
    }

    public long getLong(String path, long def) {
        Object val = getNested(path);
        if (val instanceof Number n) return n.longValue();
        if (val != null) {
            try { return Long.parseLong(String.valueOf(val)); } catch (Exception ignored) {}
        }
        return def;
    }

    public double getDouble(String path, double def) {
        Object val = getNested(path);
        if (val instanceof Number n) return n.doubleValue();
        if (val != null) {
            try { return Double.parseDouble(String.valueOf(val)); } catch (Exception ignored) {}
        }
        return def;
    }

    public String getString(String path, String def) {
        Object val = getNested(path);
        if (val != null) return String.valueOf(val);
        return def;
    }

    // Config Getters matching Upstream 1:1
    public String lang() { return getString("lang", "en_US"); }
    public boolean allowInfiniteEffects() { return getBoolean("allow_infinite_effects", false); }
    public int ritualDuration() { return getInt("ritual_duration", 600); }
    public int ritualDurationEnder() { return getInt("ritual_duration_ender", 200); }
    public boolean ritualBeacon() { return getBoolean("ritual_beacon", true); }
    public boolean emptyEffectIcon() { return getBoolean("empty_effect_icon", true); }
    public boolean playerHeadDrops() { return getBoolean("player_head_drops", true); }
    public boolean enableDiscordBroadcasts() { return getBoolean("enable_discord_broadcasts", false); }
    public String discordWebhookUrl() { return getString("discord_webhook_url", ""); }
    public boolean brewingGui() { return getBoolean("brewing_gui", true); }
    public String effectDrops() { return getString("effect_drops", "random"); }
    public void setEffectDrops(String value) { config.put("effect_drops", value); save(); }
    public int hitCounterDecaySeconds() { return getInt("hit_counter_decay_seconds", 15); }
    public boolean joinEffectsEnabled() { return getBoolean("join_effects_enabled", false); }
    public boolean regularBroadcast() { return getBoolean("regular_effect_broadcast", true); }

    public boolean enableApophis() { return getBoolean("extra_effects.Apophis", getBoolean("extra_effects_Apophis", true)); }
    public boolean enableThief() { return getBoolean("extra_effects.Thief", getBoolean("extra_effects_Thief", true)); }

    public double emeraldLockDurationSeconds() { return getDouble("emerald.lock_duration_seconds", 10.0); }
    public double apophisLockDurationSeconds() { return getDouble("apophis.lock_duration_seconds", 10.0); }
    public double emeraldMultiplierStandard() { return getDouble("emerald.multiplier-xp.standard", 2.0); }
    public double emeraldMultiplierUseEffect() { return getDouble("emerald.multiplier-xp.use-effect", 4.0); }
    public boolean invisHideKills() { return getBoolean("invis.hide_kills", false); }
    public boolean invisHideDeaths() { return getBoolean("invis.hide_deaths", false); }

    public int speedDashMultiplier() { return getInt("speed.dashMultiplier", 20); }
    public double speedPlayerVelocityMultiplier() { return getDouble("speed.playerVelocityMultiplier", 1.5); }
    public int oceanPullInterval() { return getInt("ocean_pulling.pull.interval", 10); }
    public int oceanPullRadius() { return getInt("ocean_pulling.pull.radius", 5); }
    public double oceanPullStrength() { return getDouble("ocean_pulling.pull.strength", 0.5); }

    public int emeraldExpPerHit() { return getInt("emerald.xp_stolen_per_hit", 15); }
    public float emeraldExpPercent() { return (float) Math.clamp(getDouble("emerald.xp_stolen_percent", 1.0), 0.0, 1.0); }
    public float emeraldPercentExpToShare() { return (float) Math.clamp(getDouble("emerald.percent_xp_to_share", 0.5), 0.0, 1.0); }

    public int apophisExpPerHit() { return getInt("apophis.xp_stolen_per_hit", 15); }
    public float apophisExpPercent() { return (float) Math.clamp(getDouble("apophis.xp_stolen_percent", 1.0), 0.0, 1.0); }
    public float apophisPercentExpToShare() { return (float) Math.clamp(getDouble("apophis.percent_xp_to_share", 0.5), 0.0, 1.0); }
    public int apophisLootingLevel() { return getInt("apophis.enchantment.looting_level", 5); }
    public int emeraldLootingLevel() { return getInt("emerald.enchantment.looting_level", 5); }

    public int hasteFortuneLevel() { return getInt("haste.enchantment.fortune_level", 5); }
    public int hasteEfficiencyLevel() { return getInt("haste.enchantment.efficiency_level", 10); }
    public int hasteUnbreakingLevel() { return getInt("haste.enchantment.unbreaking_level", 5); }

    public double enderPassiveRadius() { return getDouble("ender.passive.radius", 10.0); }
    public int enderSparkMaxDistance() { return getInt("ender.spark.max-distance", 15); }
    public boolean enderOnehitMobs() { return getBoolean("ender.onehit_mobs", true); }
    public boolean enderCurseHit() { return getBoolean("ender.curse_hit", true); }
    public boolean emeraldPreserveConsumables() { return getBoolean("emerald.preserve_consumables", true); }
    public boolean emeraldEnchantBonus() { return getBoolean("emerald.enchant_bonus", true); }
    public boolean regenCanAlwaysEat() { return getBoolean("regen.can_always_eat", true); }

    public double featherLandRadius() { return getDouble("feather.land.radius", 4.0); }
    public double featherLandDamage() { return getDouble("feather.land.damage", 8.0); }

    public double firePassiveWalkSpeed() { return getDouble("fire.passive.walk-speed", 0.6); }
    public double fireSparkRadius() { return getDouble("fire.spark.radius", 5.0); }
    public double fireSparkExplosionRadius() { return getDouble("fire.spark.explosion-radius", 5.0); }

    public int frostPassiveSnowChangingRadius() { return getInt("frost.passive.snow-changing-radius", 3); }
    public double frostPassiveWalkSpeed() { return getDouble("frost.passive.walk-speed", 0.6); }
    public double frostSparkRadius() { return getDouble("frost.spark.radius", 5.0); }

    public int oceanPassiveDrownStrength() { return getInt("ocean.passive.drown-strength", 5); }
    public int oceanPassiveDrownDamage() { return getInt("ocean.passive.drown-damage", 1); }
    public int oceanSparkDrownStrength() { return getInt("ocean.spark.drown-strength", 20); }
    public int oceanSparkDrownDamage() { return getInt("ocean.spark.drown-damage", 2); }

    public double regenSparkHealTrustedRadius() { return getDouble("regen.spark.heal-trusted-radius", 5.0); }

    public int getCraftLimit(InfuseEffect effect) {
        if (allowInfiniteEffects()) {
            return Integer.MAX_VALUE;
        }
        if (effect == null) return 1;
        String mode = effect.isAugmented() ? "augmented" : "default";
        return getInt(effect.getKey() + ".limit." + mode, 1);
    }

    public long cooldown(InfuseEffect effect) {
        if (effect == null) return 60L;
        String mode = effect.isAugmented() ? "augmented" : "default";
        return getLong(effect.getKey() + ".cooldown." + mode, 60L);
    }

    public long duration(InfuseEffect effect) {
        if (effect == null) return 15L;
        String mode = effect.isAugmented() ? "augmented" : "default";
        return getLong(effect.getKey() + ".duration." + mode, 15L);
    }

    public List<InfuseEffect> joinEffects() {
        Object val = getNested("join_effects");
        if (!(val instanceof List<?> list)) {
            return Collections.emptyList();
        }
        List<InfuseEffect> effects = new ArrayList<>();
        for (Object item : list) {
            InfuseEffect eff = InfuseEffect.fromString(String.valueOf(item));
            if (eff != null) {
                effects.add(eff);
            }
        }
        return effects;
    }

    public void applyUpdates() {
        save();
    }
}
