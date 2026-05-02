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

public class Feather {

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        // Passive effects for Feather
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "feather")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        player.setVelocity(0, 1, 0);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 20, 10));
        player.velocityDirty = true;

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_FEATHER : EffectMapping.FEATHER);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_FEATHER : EffectMapping.FEATHER);

        CooldownManager.setTimes(playerUUID, "feather", duration, cooldown);
    }
}
