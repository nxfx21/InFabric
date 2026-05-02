package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Thief {

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        // Thief passive
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "thief")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_THIEF : EffectMapping.THIEF);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_THIEF : EffectMapping.THIEF);

        CooldownManager.setTimes(playerUUID, "thief", duration, cooldown);
        
        for (net.minecraft.entity.Entity entity : player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(10))) {
            if (entity instanceof ServerPlayerEntity victim) {
                if (plugin.getDataManager().isTrusted(victim.getUuid(), player.getUuid())) continue;
                
                EffectMapping effect1 = plugin.getDataManager().getEffect(victim.getUuid(), "1");
                EffectMapping effect2 = plugin.getDataManager().getEffect(victim.getUuid(), "2");
                
                EffectMapping stolen = effect1 != null ? effect1 : effect2;
                if (stolen != null) {
                    plugin.getDataManager().setEffect(victim.getUuid(), effect1 != null ? "1" : "2", null);
                    plugin.getDataManager().setEffect(player.getUuid(), "1", stolen); // For now just set to slot 1
                    player.sendMessage(net.minecraft.text.Text.literal("Stole " + stolen.getKey() + " from " + victim.getName().getString()), true);
                    victim.sendMessage(net.minecraft.text.Text.literal("Your " + stolen.getKey() + " was stolen!"), true);
                    break;
                }
            }
        }
    }
}
