package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Fire {
    
    public static void applyPassiveEffects(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.FIRE)) return;

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, false, false));
    }

    public static void onTenHit(ServerPlayerEntity attacker, LivingEntity target) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.FIRE)) return;

        target.setOnFireFor(5); // 5 seconds
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "fire")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1, 1);

        ServerWorld world = (ServerWorld) player.getWorld();
        for (Entity entity : world.getOtherEntities(player, player.getBoundingBox().expand(5, 5, 5))) {
            if (entity instanceof LivingEntity living) {
                living.setOnFireFor(5);
            }
        }

        // TODO: Port spawnSparkEffect (requires task scheduling)
        
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_FIRE : EffectMapping.FIRE);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_FIRE : EffectMapping.FIRE);

        CooldownManager.setTimes(playerUUID, "fire", duration, cooldown);
    }
}
