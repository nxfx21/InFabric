package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Frost {

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.FROST)) return;

        if (player.getWorld().getBlockState(player.getBlockPos().down()).isOf(Blocks.ICE)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 30, 2, false, false));
        }
    }

    public static void onTenHit(ServerPlayerEntity attacker, LivingEntity target) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.FROST)) return;

        target.setFrozenTicks(200);
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "frost")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_FROST : EffectMapping.FROST);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_FROST : EffectMapping.FROST);

        CooldownManager.setTimes(playerUUID, "frost", duration, cooldown);
        
        for (net.minecraft.entity.Entity entity : player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(10))) {
            if (entity instanceof LivingEntity living) {
                if (entity instanceof ServerPlayerEntity nearby && plugin.getDataManager().isTrusted(nearby.getUuid(), player.getUuid())) continue;
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(duration * 20 / 1000), 2, false, false));
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, (int)(duration * 20 / 1000), 2, false, false));
                living.setFrozenTicks(200);
            }
        }
    }
}
