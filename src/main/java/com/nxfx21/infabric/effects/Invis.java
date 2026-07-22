package com.nxfx21.infabric.effects;

import com.nxfx21.infabric.EffectConstants;
import com.nxfx21.infabric.EffectIds;
import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.managers.CooldownManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public class Invis extends InfuseEffect {
    public static final Set<UUID> activeVanish = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    private final Infuse plugin;

    public Invis() {
        this(false);
    }

    public Invis(boolean augmented) {
        super("invis", EffectIds.INVIS, augmented, EffectConstants.potionColor(EffectIds.INVIS), EffectConstants.ritualColor(EffectIds.INVIS));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, -1, 0, false, false));
    }

    @Override
    public void unequip(ServerPlayerEntity owner) {
        owner.removeStatusEffect(StatusEffects.INVISIBILITY);
    }

    public static void updateVanishStatus(ServerPlayerEntity player, boolean vanish) {
        if (vanish) {
            activeVanish.add(player.getUuid());
        } else {
            activeVanish.remove(player.getUuid());
        }
        
        try {
            ((com.nxfx21.infabric.util.ChunkLoadingManagerHelper) player.getServerWorld().getChunkManager().chunkLoadingManager).forceUpdateTracking(player);
        } catch (Exception e) {
            Infuse.LOGGER.error("Failed to update vanish tracking", e);
        }
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "invis")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "invis", duration, cooldown);

        final double radius = 10;
        final long durationTicks = duration * 20;
        final ServerWorld world = owner.getServerWorld();
        final Set<ServerPlayerEntity> vanishedPlayers = new HashSet<>();

        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.getUuid().equals(owner.getUuid()) || (player.squaredDistanceTo(owner) <= radius * radius && plugin.getDataManager().isTrusted(owner.getUuid(), player.getUuid()))) {
                vanishedPlayers.add(player);
            }
        }

        // Apply vanish and invisibility
        for (ServerPlayerEntity vanished : vanishedPlayers) {
            vanished.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, (int) durationTicks, 1, false, false));
            updateVanishStatus(vanished, true);
        }

        scheduleInvisTick(owner, world, radius, 0, durationTicks, vanishedPlayers);
    }

    private void scheduleInvisTick(ServerPlayerEntity owner, ServerWorld world, double radius, int ticksElapsed, long durationTicks, Set<ServerPlayerEntity> vanishedPlayers) {
        if (ticksElapsed >= durationTicks) {
            for (ServerPlayerEntity vanished : vanishedPlayers) {
                updateVanishStatus(vanished, false);
            }
            return;
        }

        net.minecraft.util.math.Vec3d center = owner.getPos();
        net.minecraft.particle.DustParticleEffect dust = new net.minecraft.particle.DustParticleEffect(0x000000, 1.5f);
        
        for (int angle = 0; angle < 360; angle += 2) {
            double rad = Math.toRadians(angle);
            double baseX = center.getX() + radius * Math.cos(rad);
            double baseZ = center.getZ() + radius * Math.sin(rad);

            double offsetX = (Math.random() - 0.5) * 0.3;
            double offsetZ = (Math.random() - 0.5) * 0.3;
            world.spawnParticles(dust, baseX + offsetX, center.getY(), baseZ + offsetZ, 1, 0, 0, 0, 0);
        }

        for (ServerPlayerEntity p : world.getPlayers()) {
            if (p.getWorld().equals(world) && p.getPos().distanceTo(center) <= radius && !plugin.getDataManager().isTrusted(p.getUuid(), owner.getUuid())) {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 40, 0, false, false));
            }
        }

        int nextTick = ticksElapsed + 10;
        plugin.getHitTracker().scheduleTask(10L, () -> scheduleInvisTick(owner, world, radius, nextTick, durationTicks, vanishedPlayers));
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Invis();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Invis(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_INVIS_NAME : MessageType.INVIS_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_INVIS_LORE : MessageType.INVIS_LORE);
    }

    public static void onAttack(ServerPlayerEntity attacker, LivingEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect invisEffect = InfuseEffect.fromString("invis");
        if (invisEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), invisEffect)) return;

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0, false, false));
        spawnBlackParticles(target, 4);
    }

    public static void onTenHit(ServerPlayerEntity attacker, ServerPlayerEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect invisEffect = InfuseEffect.fromString("invis");
        if (invisEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), invisEffect)) return;

        target.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, false, false));
        spawnBlackParticles(target, 4);
    }

    public static void spawnBlackParticles(LivingEntity target, int durationSeconds) {
        target.getWorld().getServer().execute(() -> {
            ((ServerWorld) target.getWorld()).spawnParticles(
                ParticleTypes.SQUID_INK,
                target.getX(), target.getY() + 1, target.getZ(),
                10, 0.5, 0.5, 0.5, 0.1
            );
        });
    }
}
