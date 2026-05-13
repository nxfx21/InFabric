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
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.FEATHER)) return;

        // No fall damage is handled in DamageMixin or dedicated listener
    }

    public static void onFallDamage(ServerPlayerEntity player, net.minecraft.entity.damage.DamageSource source, float amount, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (Infuse.getInstance().getDataManager().hasEffect(player, EffectMapping.FEATHER)) {
            if (source.isOf(net.minecraft.entity.damage.DamageTypes.FALL)) {
                cir.setReturnValue(false);
            }
        }
    }

    public static void onAttack(ServerPlayerEntity attacker, LivingEntity target) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(attacker, EffectMapping.FEATHER)) return;

        if (attacker.fallDistance >= 7.0f) {
            attacker.getWorld().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.ITEM_MACE_SMASH_AIR, SoundCategory.PLAYERS, 1.0f, 1.0f);
            attacker.getServerWorld().spawnParticles(net.minecraft.particle.ParticleTypes.GUST_EMITTER_SMALL, target.getX(), target.getY(), target.getZ(), 1, 0, 0, 0, 0);
            attacker.setVelocity(attacker.getVelocity().x, 1.8, attacker.getVelocity().z);
            attacker.velocityModified = true;
        }
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
