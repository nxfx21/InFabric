package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
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

public class Ender extends InfuseEffect {
    private final Infuse plugin;

    public Ender() {
        this(false);
    }

    public Ender(boolean augmented) {
        super("ender", EffectIds.ENDER, augmented, EffectConstants.potionColor(EffectIds.ENDER), EffectConstants.ritualColor(EffectIds.ENDER));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {}

    @Override
    public void unequip(ServerPlayerEntity owner) {}

    @Override
    public void applyPassives(ServerPlayerEntity owner) {
        ServerWorld world = owner.getServerWorld();
        double radius = plugin.getMainConfig().enderPassiveRadius();
        for (Entity entity : world.getOtherEntities(owner, owner.getBoundingBox().expand(radius))) {
            if (entity instanceof ServerPlayerEntity nearby) {
                if (plugin.getDataManager().isTrusted(nearby.getUuid(), owner.getUuid())) continue;
                nearby.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 40, 1, false, false));
            }
        }
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "ender")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        Vec3d startPos = owner.getEyePos();
        Vec3d direction = owner.getRotationVector().normalize();
        int maxDistance = plugin.getMainConfig().enderSparkMaxDistance();

        Vec3d targetPos = null;
        for (int i = 1; i <= maxDistance; i++) {
            Vec3d checkPos = startPos.add(direction.multiply(i));
            if (isSafeTeleportLocation(owner.getServerWorld(), checkPos)) {
                targetPos = checkPos;
            } else {
                break;
            }
        }

        if (targetPos != null) {
            owner.requestTeleport(targetPos.x, targetPos.y, targetPos.z);
        }

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "ender", duration, cooldown);
    }

    private static boolean isSafeTeleportLocation(ServerWorld world, Vec3d pos) {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        return world.getBlockState(blockPos).isAir() && world.getBlockState(blockPos.up()).isAir();
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Ender();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Ender(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_ENDER_NAME : MessageType.ENDER_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_ENDER_LORE : MessageType.ENDER_LORE);
    }
}
