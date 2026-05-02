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

public class Emerald {

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.EMERALD)) return;

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 40, 2, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 40, 9, false, false));
        
        net.minecraft.item.ItemStack stack = player.getMainHandStack();
        if (com.catadmirer.infuseSMP.util.ItemUtil.isSword(stack)) {
            player.getWorld().getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .flatMap(r -> r.getOptional(net.minecraft.enchantment.Enchantments.LOOTING))
                .ifPresent(entry -> {
                    com.catadmirer.infuseSMP.util.ItemUtil.applySpecialEnchantment(stack, "infuse:emerald_looting", entry, plugin.getMainConfig().emeraldLootingLevel());
                });
        }
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "emerald")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_EMERALD : EffectMapping.EMERALD);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_EMERALD : EffectMapping.EMERALD);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, (int) duration * 20, 4));

        CooldownManager.setTimes(playerUUID, "emerald", duration, cooldown);
    }
}
