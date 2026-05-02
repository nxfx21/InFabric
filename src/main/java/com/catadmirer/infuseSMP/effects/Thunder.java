package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Thunder {

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        // Thunder passive
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "thunder")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_THUNDER : EffectMapping.THUNDER);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_THUNDER : EffectMapping.THUNDER);

        CooldownManager.setTimes(playerUUID, "thunder", duration, cooldown);
        
        ServerWorld world = (ServerWorld) player.getWorld();
        for (net.minecraft.entity.Entity entity : world.getOtherEntities(player, player.getBoundingBox().expand(10))) {
            if (entity instanceof LivingEntity living) {
                if (entity instanceof ServerPlayerEntity nearby && plugin.getDataManager().isTrusted(nearby.getUuid(), player.getUuid())) continue;
                strikeLighting(living, player);
            }
        }
    }

    public static void strikeLighting(LivingEntity target, LivingEntity attacker) {
        ServerWorld world = (ServerWorld) target.getWorld();
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, net.minecraft.entity.SpawnReason.EVENT);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(target.getPos());
            lightning.setCosmetic(true);
            world.spawnEntity(lightning);
        }
        target.damage(world, world.getDamageSources().lightningBolt(), 2.0f);
    }
}
