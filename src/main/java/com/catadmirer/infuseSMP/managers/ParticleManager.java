package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.awt.Color;

public class ParticleManager {

    public ParticleManager() {
    }

    public void spawnEffectParticles(ServerPlayerEntity player, String slot) {
        com.catadmirer.infuseSMP.effects.InfuseEffect effect = Infuse.getInstance().getDataManager().getEffect(player.getUuid(), slot);
        if (effect == null) return;

        ServerWorld world = (ServerWorld) player.getWorld();

        if (effect.getKey().equals("ender")) {
            world.spawnParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), 32, 0.3, 0.5, 0.3, 0);
            return;
        }

        int argb = effect.getPotionColor().getRGB();
        // Fabric EntityEffectParticleEffect
        EntityEffectParticleEffect particleEffect = EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, argb);
        world.spawnParticles(particleEffect, player.getX(), player.getY() + 1, player.getZ(), 2, 0.3, 0.5, 0.3, 0.1);
    }

    public static void spawnEffectCloud(ServerPlayerEntity player, Color color) {
        ServerWorld world = (ServerWorld) player.getWorld();
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
        Vec3d diff = end.subtract(start);
        int points = (int) (diff.length() * 10);
        if (points == 0) return;
        Vec3d step = diff.multiply(1.0 / points);
        
        DustParticleEffect dust = new DustParticleEffect(color, scale);
        
        Vec3d current = start;
        for (int i = 0; i <= points; i++) {
            world.spawnParticles(dust, current.getX(), current.getY(), current.z, count, 0, 0, 0, 0);
            current = current.add(step);
        }
    }
}
