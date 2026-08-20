package com.nxfx21.infabric.effects;

import com.nxfx21.infabric.EffectConstants;
import com.nxfx21.infabric.EffectIds;
import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.managers.CooldownManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Thief extends InfuseEffect {
    private final Infuse plugin;

    public Thief() {
        this(false);
    }

    public Thief(boolean augmented) {
        super("thief", EffectIds.THIEF, augmented, EffectConstants.potionColor(EffectIds.THIEF), EffectConstants.ritualColor(EffectIds.THIEF));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {}

    @Override
    public void unequip(ServerPlayerEntity owner) {}

    @Override
    public void applyPassives(ServerPlayerEntity owner) {}

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "thief")) return;
        if (CooldownManager.isOnCooldown(playerUUID, "thief_stolen")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "thief", duration, cooldown);
        
        for (net.minecraft.entity.Entity entity : owner.getWorld().getOtherEntities(owner, owner.getBoundingBox().expand(10))) {
            if (entity instanceof ServerPlayerEntity victim) {
                if (plugin.getDataManager().isTrusted(victim.getUuid(), owner.getUuid())) continue;
                
                InfuseEffect effect1 = plugin.getDataManager().getEffect(victim.getUuid(), "1");
                InfuseEffect effect2 = plugin.getDataManager().getEffect(victim.getUuid(), "2");
                
                InfuseEffect stolen = effect1 != null ? effect1 : effect2;
                if (stolen != null) {
                    plugin.getDataManager().setEffect(victim.getUuid(), effect1 != null ? "1" : "2", null);
                    plugin.getDataManager().setEffect(owner.getUuid(), "1", stolen); // Assign stolen effect to primary slot (slot 1)
                    owner.sendMessage(net.minecraft.text.Text.literal("Stole " + stolen.getKey() + " from " + victim.getName().getString()), true);
                    victim.sendMessage(net.minecraft.text.Text.literal("Your " + stolen.getKey() + " was stolen!"), true);
                    break;
                }
            }
        }
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Thief();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Thief(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_THIEF_NAME : MessageType.THIEF_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_THIEF_LORE : MessageType.THIEF_LORE);
    }

    public static void onPlayerHit(ServerPlayerEntity attacker, ServerPlayerEntity victim) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect thiefEffect = InfuseEffect.fromString("thief");
        if (thiefEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), thiefEffect)) return;

        if (plugin.getDataManager().isTrusted(victim.getUuid(), attacker.getUuid())) return;

        // Hotbar item stealing (slots 0-8)
        java.util.List<Integer> validSlots = new java.util.ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (!victim.getInventory().getStack(i).isEmpty()) {
                validSlots.add(i);
            }
        }
        if (!validSlots.isEmpty()) {
            int chosenSlot = validSlots.get(net.minecraft.util.math.random.Random.create().nextInt(validSlots.size()));
            net.minecraft.item.ItemStack stolenStack = victim.getInventory().getStack(chosenSlot);
            victim.getInventory().setStack(chosenSlot, net.minecraft.item.ItemStack.EMPTY);
            if (!attacker.getInventory().insertStack(stolenStack)) {
                attacker.dropItem(stolenStack, false);
            }
        }

        // Effect stealing when Thief spark is active
        if (CooldownManager.isEffectActive(attacker.getUuid(), "thief")) {
            InfuseEffect effect1 = plugin.getDataManager().getEffect(victim.getUuid(), "1");
            InfuseEffect effect2 = plugin.getDataManager().getEffect(victim.getUuid(), "2");

            InfuseEffect stolen = null;
            String slotStolenFrom = null;
            if (effect1 != null && effect2 != null) {
                if (Math.random() > 0.5) {
                    stolen = effect1;
                    slotStolenFrom = "1";
                } else {
                    stolen = effect2;
                    slotStolenFrom = "2";
                }
            } else if (effect1 != null) {
                stolen = effect1;
                slotStolenFrom = "1";
            } else if (effect2 != null) {
                stolen = effect2;
                slotStolenFrom = "2";
            }

            if (stolen != null) {
                plugin.getDataManager().removeEffect(victim.getUuid(), slotStolenFrom);
                stolen.activateSpark(attacker);
                attacker.sendMessage(net.minecraft.text.Text.literal("Stole and activated " + stolen.getKey() + " spark from " + victim.getName().getString() + "!"), true);
                victim.sendMessage(net.minecraft.text.Text.literal("Your " + stolen.getKey() + " effect was stolen by " + attacker.getName().getString() + "!"), true);
                CooldownManager.setDuration(attacker.getUuid(), "thief", 0);
            }
        }
    }

    public static void onAttack(ServerPlayerEntity attacker, net.minecraft.entity.LivingEntity target) {
        if (target instanceof ServerPlayerEntity victim) {
            onPlayerHit(attacker, victim);
        }
    }

    public static final java.util.Map<UUID, net.minecraft.entity.boss.ServerBossBar> activeDisguises = new java.util.concurrent.ConcurrentHashMap<>();

    public static void disguiseAs(ServerPlayerEntity killer, ServerPlayerEntity victim) {
        if (killer == null || victim == null) return;
        Infuse plugin = Infuse.getInstance();
        UUID killerUuid = killer.getUuid();

        java.io.File disguiseFile = new java.io.File(plugin.getDataFolder(), "data/ThiefPlayers/" + killerUuid + ".txt");
        disguiseFile.getParentFile().mkdirs();

        if (!disguiseFile.exists()) {
            try (java.io.FileWriter writer = new java.io.FileWriter(disguiseFile)) {
                com.mojang.authlib.GameProfile profile = killer.getGameProfile();
                java.util.Optional<com.mojang.authlib.properties.Property> textures = profile.getProperties().get("textures").stream().findFirst();

                writer.write(killer.getName().getString() + "\n");
                if (textures.isEmpty()) {
                    writer.write("null\nnull");
                } else {
                    writer.write(textures.get().value() + "\n");
                    writer.write(String.valueOf(textures.get().signature()));
                }
            } catch (java.io.IOException e) {
                Infuse.LOGGER.error("Failed to save original skin for thief disguise", e);
            }
        }

        com.mojang.authlib.GameProfile victimProfile = victim.getGameProfile();
        java.util.Optional<com.mojang.authlib.properties.Property> victimTextures = victimProfile.getProperties().get("textures").stream().findFirst();

        if (victimTextures.isPresent()) {
            killer.getGameProfile().getProperties().removeAll("textures");
            killer.getGameProfile().getProperties().put("textures", victimTextures.get());
        }

        net.minecraft.entity.boss.ServerBossBar bossBar = activeDisguises.computeIfAbsent(killerUuid, u -> {
            net.minecraft.entity.boss.ServerBossBar bar = new net.minecraft.entity.boss.ServerBossBar(
                    net.minecraft.text.Text.literal("Disguise Time Remaining"),
                    net.minecraft.entity.boss.BossBar.Color.RED,
                    net.minecraft.entity.boss.BossBar.Style.PROGRESS
            );
            bar.addPlayer(killer);
            return bar;
        });

        final long durationSeconds = 3600;

        plugin.getHitTracker().scheduleTask(20L, new Runnable() {
            int elapsed = 0;
            @Override
            public void run() {
                elapsed++;
                long remaining = durationSeconds - elapsed;
                if (remaining <= 0 || killer.isDisconnected()) {
                    removeDisguise(killer);
                    return;
                }
                float progress = Math.max(0.0f, (float) remaining / durationSeconds);
                long mins = remaining / 60;
                long secs = remaining % 60;
                bossBar.setName(net.minecraft.text.Text.literal(String.format("Disguise: %dm %02ds", mins, secs)));
                bossBar.setPercent(progress);
                plugin.getHitTracker().scheduleTask(20L, this);
            }
        });
    }

    public static void removeDisguise(ServerPlayerEntity player) {
        if (player == null) return;
        UUID uuid = player.getUuid();

        net.minecraft.entity.boss.ServerBossBar bar = activeDisguises.remove(uuid);
        if (bar != null) {
            bar.clearPlayers();
        }

        java.io.File disguiseFile = new java.io.File(Infuse.getInstance().getDataFolder(), "data/ThiefPlayers/" + uuid + ".txt");
        if (disguiseFile.exists()) {
            try (java.util.Scanner scanner = new java.util.Scanner(disguiseFile)) {
                com.mojang.authlib.GameProfile profile = player.getGameProfile();
                if (scanner.hasNextLine()) scanner.nextLine(); // Name
                String value = scanner.hasNextLine() ? scanner.nextLine() : "";
                String signature = scanner.hasNextLine() ? scanner.nextLine() : null;
                if ("null".equals(signature)) signature = null;

                profile.getProperties().removeAll("textures");
                if (!"null".equals(value) && !value.isEmpty()) {
                    profile.getProperties().put("textures", new com.mojang.authlib.properties.Property("textures", value, signature));
                }
            } catch (Exception ignored) {}
            disguiseFile.delete();
        }
    }
}
