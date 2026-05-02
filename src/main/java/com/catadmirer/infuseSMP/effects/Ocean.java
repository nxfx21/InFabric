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
        
        if (player.isSubmergedInWater()) {
            for (net.minecraft.entity.Entity entity : player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(8))) {
                if (entity instanceof ServerPlayerEntity nearby) {
                    if (plugin.getDataManager().isTrusted(nearby.getUuid(), player.getUuid())) continue;
                    if (nearby.isSubmergedInWater()) {
                        nearby.setAir(Math.max(0, nearby.getAir() - 10));
                    }
                }
            }
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
        
        // TODO: Port pull and particle logic
    }
}
