package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Ocean {

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.OCEAN)) return;

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 40, 0, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 40, 0, false, false));
        
        int drownStrength = 5;
        if (CooldownManager.isEffectActive(player.getUuid(), "ocean")) {
            drownStrength = 20;
        }

        for (ServerPlayerEntity nearby : player.getServerWorld().getPlayers()) {
            if (nearby == player) continue;
            if (plugin.getDataManager().isTrusted(nearby.getUuid(), player.getUuid())) continue;
            if (nearby.getPos().distanceTo(player.getPos()) <= 5) {
                nearby.setAir(Math.max(-20, nearby.getAir() - drownStrength));
                if (nearby.getAir() <= 0) {
                    nearby.damage(player.getServerWorld().getDamageSources().drown(), CooldownManager.isEffectActive(player.getUuid(), "ocean") ? 2.0f : 1.0f);
                }
            }
        }

        if (CooldownManager.isEffectActive(player.getUuid(), "ocean")) {
            applyPullEffect(player);
            spawnSparkParticles(player);
        }
    }

    private static void applyPullEffect(ServerPlayerEntity caster) {
        Infuse plugin = Infuse.getInstance();
        double radius = plugin.getMainConfig().oceanPullRadius();
        double strength = plugin.getMainConfig().oceanPullStrength();

        for (ServerPlayerEntity p : caster.getServerWorld().getPlayers()) {
            if (p == caster) continue;
            if (plugin.getDataManager().isTrusted(caster.getUuid(), p.getUuid())) continue;
            if (p.getPos().distanceTo(caster.getPos()) > radius) continue;

            net.minecraft.util.math.Vec3d direction = caster.getPos().subtract(p.getPos()).normalize();
            p.setVelocity(p.getVelocity().add(direction.multiply(strength)));
            p.velocityModified = true;
        }
    }

    private static void spawnSparkParticles(ServerPlayerEntity player) {
        double radius = 5;
        net.minecraft.server.world.ServerWorld world = player.getServerWorld();
        for (int angle = 0; angle < 360; angle += 20) {
            double rad = Math.toRadians(angle);
            double x = player.getX() + radius * Math.cos(rad);
            double z = player.getZ() + radius * Math.sin(rad);
            world.spawnParticles(net.minecraft.particle.ParticleTypes.FALLING_WATER, x, player.getY() + 1, z, 1, 0, 0, 0, 0);
        }
    }
    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "ocean")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_OCEAN : EffectMapping.OCEAN);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_OCEAN : EffectMapping.OCEAN);

        CooldownManager.setTimes(playerUUID, "ocean", duration, cooldown);
    }
}
