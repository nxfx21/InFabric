package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Strength {

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        // Strength passive
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "strength")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);
        
        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_STRENGTH : EffectMapping.STRENGTH);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_STRENGTH : EffectMapping.STRENGTH);

        CooldownManager.setTimes(playerUUID, "strength", duration, cooldown);
    }

    public static float getExtraDamage(ServerPlayerEntity attacker, float damage) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.STRENGTH)) return damage;

        float health = attacker.getHealth();
        if (health < 2f) {
            damage += 3f;
        } else if (health < 4f) {
            damage += 2f;
        } else if (health < 6f) {
            damage += 1f;
        }
        return damage;
    }

    public static boolean shouldAutoCrit(ServerPlayerEntity player) {
        return CooldownManager.isEffectActive(player.getUuid(), "strength");
    }

    public static float applySparkAutoCrit(ServerPlayerEntity player, float damage) {
        if (shouldAutoCrit(player)) {
            damage *= 1.35f;
            // Spawn crit particles and play sound? (Logic would go in the damage mixin/event)
        }
        return damage;
    }
}
