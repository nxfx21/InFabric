package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Invisibility {

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.INVIS)) return;

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, 40, 0, false, false));
    }

    public static void onAttack(ServerPlayerEntity attacker, LivingEntity target) {
        if (!Infuse.getInstance().getDataManager().hasEffect(attacker, EffectMapping.INVIS)) return;
        
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0, false, false));
        spawnBlackParticles(target, 4);
    }

    private static void spawnBlackParticles(LivingEntity target, int durationSeconds) {
        // Simple particle spawn for now
        target.getWorld().getServer().execute(() -> {
            ((net.minecraft.server.world.ServerWorld)target.getWorld()).spawnParticles(
                net.minecraft.particle.ParticleTypes.SQUID_INK,
                target.getX(), target.getY() + 1, target.getZ(),
                10, 0.5, 0.5, 0.5, 0.1
            );
        });
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "invis")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_INVIS : EffectMapping.INVIS);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_INVIS : EffectMapping.INVIS);

        CooldownManager.setTimes(playerUUID, "invis", duration, cooldown);
        
        // TODO: Port team-wide vanish and particle logic
    }
}
