package com.nxfx21.infabric.managers;

import com.nxfx21.infabric.EffectIds;
import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.Ender;
import com.nxfx21.infabric.effects.InfuseEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ParticleManager {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public ParticleManager() {
    }

    public static void spawnEffectParticles(ServerPlayerEntity player, InfuseEffect effect) {
        if (effect == null || player == null || !(player.getWorld() instanceof ServerWorld world)) return;

        if (effect.getId() == EffectIds.ENDER) {
            world.spawnParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), 32, 0.3, 0.5, 0.3, 0);
            return;
        }

        int argb = effect.getPotionColor().getRGB();
        EntityEffectParticleEffect particleEffect = EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, argb);
        world.spawnParticles(particleEffect, player.getX(), player.getY() + 1, player.getZ(), 2, 0.3, 0.5, 0.3, 0.1);
    }

    public void spawnEffectParticles(ServerPlayerEntity player, String slot) {
        InfuseEffect effect = Infuse.getInstance().getDataManager().getEffect(player.getUuid(), slot);
        if (effect != null) {
            spawnEffectParticles(player, effect);
        }
    }

    public static void spawnCursedParticles(ServerPlayerEntity player) {
        if (player == null || !Ender.cursedPlayers.contains(player.getUuid())) return;
        if (!(player.getWorld() instanceof ServerWorld world)) return;

        world.spawnParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1, player.getZ(), 10, 0.3, 0.5, 0.3, 0.01);
    }

    public static void spawnEffectCloud(ServerPlayerEntity player, Color color) {
        if (player == null || !(player.getWorld() instanceof ServerWorld world)) return;
        EntityEffectParticleEffect particleEffect = EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, color.getRGB());
        world.spawnParticles(particleEffect, player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 0.6, 0.5, 0);
    }

    public static void drawLine(ServerWorld world, Vec3d start, Vec3d end) {
        drawLine(world, start, end, 5, 0xFFFFFF, 1.0f);
    }

    public static void drawLine(ServerWorld world, Vec3d start, Vec3d end, int count) {
        drawLine(world, start, end, count, 0xFFFFFF, 1.0f);
    }

    public static void drawLine(ServerWorld world, Vec3d start, Vec3d end, int count, int color, float scale) {
        if (world == null) return;
        Vec3d diff = end.subtract(start);
        int points = (int) (diff.length() * 10);
        if (points == 0) return;
        Vec3d step = diff.multiply(1.0 / points);

        DustParticleEffect dust = new DustParticleEffect(color, scale);

        Vec3d current = start;
        for (int i = 0; i <= points; i++) {
            world.spawnParticles(dust, current.getX(), current.getY(), current.getZ(), count, 0, 0, 0, 0);
            current = current.add(step);
        }
    }

    public static void dropEffect(ServerWorld world, boolean bottomToTop, InfuseEffect effect, Vec3d location) {
        if (world == null || effect == null) return;
        int color = effect.getPotionColor().getRGB() & 0xFFFFFF;
        DustParticleEffect dust = new DustParticleEffect(color, 0.7F);
        final int points = 16;
        final double radius = 0.6;
        final Vec3d base = location.add(0, bottomToTop ? 0 : 2, 0);

        world.playSound(null, base.x, base.y, base.z, SoundEvents.ENTITY_TURTLE_EGG_BREAK, SoundCategory.PLAYERS, 1.3F, 1.2F);

        world.getServer().execute(() -> {
            for (int step = 0; step <= 14; step++) {
                final int s = step;
                SCHEDULER.schedule(() -> {
                    world.getServer().execute(() -> {
                        double yOffset = s * 0.15;
                        double ringY = bottomToTop ? yOffset : 2.0 - yOffset;
                        for (int i = 0; i < points; i++) {
                            double angle = Math.PI * 2 * i / points;
                            double x = Math.cos(angle) * radius;
                            double z = Math.sin(angle) * radius;
                            world.spawnParticles(dust, base.x + x, base.y + ringY, base.z + z, 1, 0, 0, 0, 0);
                        }
                    });
                }, step * 50L, TimeUnit.MILLISECONDS);
            }
        });
    }
}
