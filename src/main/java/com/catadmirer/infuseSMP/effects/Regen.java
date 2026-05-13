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

public class Regen {

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.REGEN)) return;

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 30, 0, false, false));
        player.getHungerManager().setFoodLevel(20);
    }

    public static void onAttack(ServerPlayerEntity attacker, LivingEntity target, float damage) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.REGEN)) return;

        attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 1, false, false));
        
        if (CooldownManager.isEffectActive(attacker.getUuid(), "regen")) {
            for (net.minecraft.entity.Entity entity : attacker.getWorld().getOtherEntities(attacker, attacker.getBoundingBox().expand(5))) {
                if (entity instanceof ServerPlayerEntity nearby && plugin.getDataManager().isTrusted(attacker.getUuid(), nearby.getUuid())) {
                    nearby.heal(damage / 2.0f);
                }
            }
        }
    }

    public static void onTenHit(ServerPlayerEntity attacker, LivingEntity target) {
        if (!Infuse.getInstance().getDataManager().hasEffect(attacker, EffectMapping.REGEN)) return;
        if (target instanceof ServerPlayerEntity playerTarget) {
            playerTarget.getHungerManager().setFoodLevel(Math.max(0, playerTarget.getHungerManager().getFoodLevel() - 2));
        }
    }

    public static void onConsume(ServerPlayerEntity player) {
        if (!Infuse.getInstance().getDataManager().hasEffect(player, EffectMapping.REGEN)) return;
        player.getHungerManager().setSaturationLevel(player.getHungerManager().getSaturationLevel() + 6.0f);
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "regen")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_REGEN : EffectMapping.REGEN);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_REGEN : EffectMapping.REGEN);

        CooldownManager.setTimes(playerUUID, "regen", duration, cooldown);
    }
}
