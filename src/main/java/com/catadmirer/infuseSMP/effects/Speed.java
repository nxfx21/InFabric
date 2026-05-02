package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import com.catadmirer.infuseSMP.managers.ParticleManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Speed {
    private static final Map<UUID, Integer> speedLevels = new HashMap<>();
    private static final Map<UUID, Long> lastHitTime = new HashMap<>();

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.SPEED)) return;

        UUID uuid = player.getUuid();
        long lastHit = lastHitTime.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - lastHit > 1000L) {
            speedLevels.put(uuid, 1);
        }

        int currentLevel = speedLevels.getOrDefault(uuid, 1);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 40, Math.max(0, currentLevel - 1), false, false, false));
    }

    public static void onAttack(ServerPlayerEntity attacker, LivingEntity target) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.SPEED)) return;

        UUID uuid = attacker.getUuid();
        long currentTime = System.currentTimeMillis();
        long lastHit = lastHitTime.getOrDefault(uuid, 0L);
        if (currentTime - lastHit >= 50L) {
            lastHitTime.put(uuid, currentTime);
            speedLevels.put(uuid, speedLevels.getOrDefault(uuid, 1) + 1);
            
            // In Fabric/Vanilla, noDamageTicks is handled differently but we can still reduce it.
            // target.hurtTime = target.hurtTime / 2;
        }
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "speed")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);
        ParticleManager.spawnEffectCloud(player, new java.awt.Color(0xD1A44B));

        Vec3d direction = player.getRotationVector().normalize();
        double multiplier = plugin.getMainConfig().speedPlayerVelocityMultiplier();
        player.setVelocity(direction.multiply(multiplier));
        player.velocityDirty = true;

        // Note: The trail effect would need a TickEvent or a custom scheduler in Fabric.
        // For the sake of the port, we'll keep it as a TODO or use a simple line.
        ParticleManager.drawLine(player.getServerWorld(), player.getPos(), player.getPos().add(direction.multiply(2)), 10, 0xE6DCAA, 1.5f);

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_SPEED : EffectMapping.SPEED);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_SPEED : EffectMapping.SPEED);

        CooldownManager.setTimes(playerUUID, "speed", duration, cooldown);
    }
}
