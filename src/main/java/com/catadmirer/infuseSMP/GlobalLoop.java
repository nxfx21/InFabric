package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public class GlobalLoop {
    private int ticks = 0;
    
    // Stub sets for curses and boosts until the effects are fully ported
    public static final Set<UUID> cursedPlayers = new HashSet<>();

    public GlobalLoop() {}

    public void start() {
        ServerTickEvents.START_SERVER_TICK.register(this::onTick);
    }

    private void onTick(MinecraftServer server) {
        ticks++;
        if (ticks % 20 != 0) return; // Run every 20 ticks (1 second)

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // Getting the player's equipped effects
            EffectMapping lEffect = Infuse.getInstance().getDataManager().getEffect(player.getUuid(), "1");
            EffectMapping rEffect = Infuse.getInstance().getDataManager().getEffect(player.getUuid(), "2");

            if (lEffect != null) {
                lEffect.applyPassiveEffects(player);
                Infuse.getInstance().getParticleManager().spawnEffectParticles(player, "1");
            }

            if (rEffect != null) {
                rEffect.applyPassiveEffects(player);
                Infuse.getInstance().getParticleManager().spawnEffectParticles(player, "2");
            }

            if (!Infuse.getInstance().getDataManager().hasEffect(player.getUuid(), EffectMapping.APOPHIS)) {
                EntityAttributeInstance playerHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                if (playerHealth != null) {
                    playerHealth.removeModifier(com.catadmirer.infuseSMP.effects.Apophis.APOPHIS_BOOST_ID);
                }
            }

            if (!Infuse.getInstance().getDataManager().hasEffect(player.getUuid(), EffectMapping.HEART)) {
                EntityAttributeInstance playerHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                if (playerHealth != null) {
                    playerHealth.removeModifier(com.catadmirer.infuseSMP.effects.Heart.HEART_BOOST_ID);
                }
            }

            if (!Infuse.getInstance().getDataManager().hasEffect(player.getUuid(), EffectMapping.EMERALD)) {
                net.minecraft.item.ItemStack stack = player.getMainHandStack();
                player.getWorld().getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                    .flatMap(r -> r.getOptional(net.minecraft.enchantment.Enchantments.LOOTING))
                    .ifPresent(entry -> {
                        com.catadmirer.infuseSMP.util.ItemUtil.removeSpecialEnchant(stack, "infuse:emerald_looting", entry);
                    });
            }

            if (!com.catadmirer.infuseSMP.managers.CooldownManager.isEffectActive(player.getUuid(), "heart")) {
                EntityAttributeInstance playerHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                if (playerHealth != null) {
                    playerHealth.removeModifier(com.catadmirer.infuseSMP.effects.Heart.HEART_SPARK_BOOST_ID);
                }
            }

            // Spawning particles on cursed players
            if (cursedPlayers.contains(player.getUuid())) {
                ServerWorld world = (ServerWorld) player.getWorld();
                world.spawnParticles(
                    ParticleTypes.WITCH, 
                    player.getX(), player.getY() + 1, player.getZ(), 
                    10, 0.3, 0.5, 0.3, 0.01
                );
            }
        }
        Infuse.getInstance().getHitTracker().tick();
    }
}
