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

public class Haste {

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        // TODO: Port enchantment logic
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "haste")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_HASTE : EffectMapping.HASTE);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_HASTE : EffectMapping.HASTE);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, 20 * 15, 3));

        CooldownManager.setTimes(playerUUID, "haste", duration, cooldown);
    }
}
