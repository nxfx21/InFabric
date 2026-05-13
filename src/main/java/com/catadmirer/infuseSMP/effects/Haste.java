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
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.HASTE)) return;

        net.minecraft.item.ItemStack stack = player.getMainHandStack();
        if (com.catadmirer.infuseSMP.util.ItemUtil.isPickaxe(stack)) {
            var registry = player.getWorld().getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.FORTUNE).ifPresent(entry -> 
                com.catadmirer.infuseSMP.util.ItemUtil.applySpecialEnchantment(stack, "infuse:haste_fortune", entry, plugin.getMainConfig().hasteFortuneLevel()));
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.EFFICIENCY).ifPresent(entry -> 
                com.catadmirer.infuseSMP.util.ItemUtil.applySpecialEnchantment(stack, "infuse:haste_efficiency", entry, plugin.getMainConfig().hasteEfficiencyLevel()));
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.UNBREAKING).ifPresent(entry -> 
                com.catadmirer.infuseSMP.util.ItemUtil.applySpecialEnchantment(stack, "infuse:haste_unbreaking", entry, plugin.getMainConfig().hasteUnbreakingLevel()));
        }
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "haste")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_HASTE : EffectMapping.HASTE);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_HASTE : EffectMapping.HASTE);

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, (int) duration * 20, 3));
        CooldownManager.setTimes(playerUUID, "haste", duration, cooldown);
    }

    public static void cleanupInventory(net.minecraft.inventory.Inventory inventory, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.HASTE)) return;

        for (int i = 0; i < inventory.size(); i++) {
            net.minecraft.item.ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            var registry = player.getWorld().getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.FORTUNE).ifPresent(entry -> 
                com.catadmirer.infuseSMP.util.ItemUtil.removeSpecialEnchant(stack, "infuse:haste_fortune", entry));
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.EFFICIENCY).ifPresent(entry -> 
                com.catadmirer.infuseSMP.util.ItemUtil.removeSpecialEnchant(stack, "infuse:haste_efficiency", entry));
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.UNBREAKING).ifPresent(entry -> 
                com.catadmirer.infuseSMP.util.ItemUtil.removeSpecialEnchant(stack, "infuse:haste_unbreaking", entry));
        }
    }
}
