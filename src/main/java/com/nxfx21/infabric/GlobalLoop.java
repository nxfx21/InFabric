package com.nxfx21.infabric;


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
        Infuse.getInstance().getHitTracker().tick();
        ticks++;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            com.nxfx21.infabric.effects.Fire.onMove(player);
            com.nxfx21.infabric.effects.Frost.onMove(player);
        }

        if (ticks % 20 != 0) return; // Run every 20 ticks (1 second)

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            // Getting the player's equipped effects
            com.nxfx21.infabric.effects.InfuseEffect lEffect = Infuse.getInstance().getDataManager().getEffect(player.getUuid(), "1");
            com.nxfx21.infabric.effects.InfuseEffect rEffect = Infuse.getInstance().getDataManager().getEffect(player.getUuid(), "2");

            if (lEffect != null) {
                lEffect.applyPassives(player);
                Infuse.getInstance().getParticleManager().spawnEffectParticles(player, "1");
            }

            if (rEffect != null) {
                rEffect.applyPassives(player);
                Infuse.getInstance().getParticleManager().spawnEffectParticles(player, "2");
            }

            com.nxfx21.infabric.effects.InfuseEffect apophisEffect = com.nxfx21.infabric.effects.InfuseEffect.fromString("apophis");
            if (apophisEffect == null || !Infuse.getInstance().getDataManager().hasEffect(player.getUuid(), apophisEffect)) {
                EntityAttributeInstance playerHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                if (playerHealth != null && playerHealth.getModifier(com.nxfx21.infabric.effects.Apophis.APOPHIS_BOOST_ID) != null) {
                    playerHealth.removeModifier(com.nxfx21.infabric.effects.Apophis.APOPHIS_BOOST_ID);
                    if (player.getHealth() > player.getMaxHealth()) {
                        player.setHealth(player.getMaxHealth());
                    }
                }
            }

            com.nxfx21.infabric.effects.InfuseEffect heartEffect = com.nxfx21.infabric.effects.InfuseEffect.fromString("heart");
            if (heartEffect == null || !Infuse.getInstance().getDataManager().hasEffect(player.getUuid(), heartEffect)) {
                EntityAttributeInstance playerHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                if (playerHealth != null && playerHealth.getModifier(com.nxfx21.infabric.effects.Heart.HEART_BOOST_ID) != null) {
                    playerHealth.removeModifier(com.nxfx21.infabric.effects.Heart.HEART_BOOST_ID);
                    if (player.getHealth() > player.getMaxHealth()) {
                        player.setHealth(player.getMaxHealth());
                    }
                }
            }

            com.nxfx21.infabric.effects.InfuseEffect emeraldEffect = com.nxfx21.infabric.effects.InfuseEffect.fromString("emerald");
            if (emeraldEffect == null || !Infuse.getInstance().getDataManager().hasEffect(player.getUuid(), emeraldEffect)) {
                net.minecraft.item.ItemStack stack = player.getMainHandStack();
                player.getWorld().getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                    .flatMap(r -> r.getOptional(net.minecraft.enchantment.Enchantments.LOOTING))
                    .ifPresent(entry -> {
                        com.nxfx21.infabric.util.ItemUtil.removeSpecialEnchant(stack, "infuse:emerald_looting", entry);
                    });
            }

            com.nxfx21.infabric.effects.InfuseEffect hasteEffect = com.nxfx21.infabric.effects.InfuseEffect.fromString("haste");
            if (hasteEffect == null || !Infuse.getInstance().getDataManager().hasEffect(player.getUuid(), hasteEffect)) {
                net.minecraft.item.ItemStack stack = player.getMainHandStack();
                var registry = player.getWorld().getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
                
                registry.getOptional(net.minecraft.enchantment.Enchantments.FORTUNE).ifPresent(entry -> 
                    com.nxfx21.infabric.util.ItemUtil.removeSpecialEnchant(stack, "infuse:haste_fortune", entry));
                
                registry.getOptional(net.minecraft.enchantment.Enchantments.EFFICIENCY).ifPresent(entry -> 
                    com.nxfx21.infabric.util.ItemUtil.removeSpecialEnchant(stack, "infuse:haste_efficiency", entry));
                
                registry.getOptional(net.minecraft.enchantment.Enchantments.UNBREAKING).ifPresent(entry -> 
                    com.nxfx21.infabric.util.ItemUtil.removeSpecialEnchant(stack, "infuse:haste_unbreaking", entry));
            }

            if (!com.nxfx21.infabric.managers.CooldownManager.isEffectActive(player.getUuid(), "heart")) {
                EntityAttributeInstance playerHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                if (playerHealth != null && playerHealth.getModifier(com.nxfx21.infabric.effects.Heart.HEART_SPARK_BOOST_ID) != null) {
                    playerHealth.removeModifier(com.nxfx21.infabric.effects.Heart.HEART_SPARK_BOOST_ID);
                    if (player.getHealth() > player.getMaxHealth()) {
                        player.setHealth(player.getMaxHealth());
                    }
                }
            }

            if (!com.nxfx21.infabric.managers.CooldownManager.isEffectActive(player.getUuid(), "apophis")) {
                EntityAttributeInstance playerHealth = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
                if (playerHealth != null && playerHealth.getModifier(com.nxfx21.infabric.effects.Apophis.APOPHIS_SPARK_BOOST_ID) != null) {
                    playerHealth.removeModifier(com.nxfx21.infabric.effects.Apophis.APOPHIS_SPARK_BOOST_ID);
                    if (player.getHealth() > player.getMaxHealth()) {
                        player.setHealth(player.getMaxHealth());
                    }
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
    }
}
