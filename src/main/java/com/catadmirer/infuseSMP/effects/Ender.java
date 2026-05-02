package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Ender {
    public static final Set<UUID> cursedPlayers = new HashSet<>();

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.ENDER)) return;

        ServerWorld world = (ServerWorld) player.getWorld();
        for (Entity entity : world.getOtherEntities(player, player.getBoundingBox().expand(10))) {
            if (entity instanceof ServerPlayerEntity nearby) {
                if (plugin.getDataManager().isTrusted(nearby.getUuid(), player.getUuid())) continue;
                nearby.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 40, 1, false, false));
            }
        }
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "ender")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        Vec3d startPos = player.getEyePos();
        Vec3d direction = player.getRotationVector().normalize();
        int maxDistance = 15;

        Vec3d targetPos = null;
        for (int i = 1; i <= maxDistance; i++) {
            Vec3d checkPos = startPos.add(direction.multiply(i));
            if (isSafeTeleportLocation(player.getServerWorld(), checkPos)) {
                targetPos = checkPos;
            } else {
                break;
            }
        }

        if (targetPos != null) {
            player.requestTeleport(targetPos.x, targetPos.y, targetPos.z);
        }

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_ENDER : EffectMapping.ENDER);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_ENDER : EffectMapping.ENDER);

        CooldownManager.setTimes(playerUUID, "ender", duration, cooldown);
    }

    private static boolean isSafeTeleportLocation(ServerWorld world, Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        return world.getBlockState(blockPos).isAir() && world.getBlockState(blockPos.up()).isAir();
    }
}
