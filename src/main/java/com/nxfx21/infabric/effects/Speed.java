package com.nxfx21.infabric.effects;

import com.nxfx21.infabric.EffectConstants;
import com.nxfx21.infabric.EffectIds;
import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.managers.CooldownManager;
import com.nxfx21.infabric.managers.ParticleManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Speed extends InfuseEffect {
    private static final Map<UUID, Integer> speedLevels = new HashMap<>();
    private static final Map<UUID, Long> lastHitTime = new HashMap<>();

    private final Infuse plugin;

    public Speed() {
        this(false);
    }

    public Speed(boolean augmented) {
        super("speed", EffectIds.SPEED, augmented, EffectConstants.potionColor(EffectIds.SPEED), EffectConstants.ritualColor(EffectIds.SPEED));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {
        speedLevels.put(owner.getUuid(), 0);
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, -1, 0, false, false, false));
    }

    @Override
    public void unequip(ServerPlayerEntity owner) {
        speedLevels.remove(owner.getUuid());
        owner.removeStatusEffect(StatusEffects.SPEED);
    }

    @Override
    public void applyPassives(ServerPlayerEntity owner) {
        UUID uuid = owner.getUuid();
        long lastHit = lastHitTime.getOrDefault(uuid, 0L);
        if (System.currentTimeMillis() - lastHit > 1000L) {
            speedLevels.put(uuid, 0);
        }
        updateSpeedEffect(owner);
    }

    private void updateSpeedEffect(ServerPlayerEntity owner) {
        if (!speedLevels.containsKey(owner.getUuid())) return;

        int lvl = speedLevels.get(owner.getUuid());
        if (lvl < 0) lvl = 0;

        owner.removeStatusEffect(StatusEffects.SPEED);
        // speedPlayerVelocityMultiplier is actually the speed level multiplier
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, -1, lvl * (int) plugin.getMainConfig().speedPlayerVelocityMultiplier(), false, false, false));
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "speed")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);
        ParticleManager.spawnEffectCloud(owner, new java.awt.Color(0xD1A44B));

        Vec3d direction = owner.getRotationVector().normalize();
        double multiplier = plugin.getMainConfig().speedDashMultiplier();
        owner.setVelocity(direction.multiply(multiplier));
        owner.velocityDirty = true;

        // Draw line trail
        ParticleManager.drawLine(owner.getServerWorld(), owner.getPos(), owner.getPos().add(direction.multiply(2)), 10, 0xE6DCAA, 1.5f);

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "speed", duration, cooldown);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Speed();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Speed(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_SPEED_NAME : MessageType.SPEED_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_SPEED_LORE : MessageType.SPEED_LORE);
    }

    public static void onAttack(ServerPlayerEntity attacker, LivingEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect speedEffect = InfuseEffect.fromString("speed");
        if (speedEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), speedEffect)) return;

        UUID uuid = attacker.getUuid();
        long currentTime = System.currentTimeMillis();
        long lastHit = lastHitTime.getOrDefault(uuid, 0L);
        
        if (currentTime - lastHit >= 50L) {
            lastHitTime.put(uuid, currentTime);
            speedLevels.put(uuid, speedLevels.getOrDefault(uuid, 0) + 1);
            
            // Halve target's noDamageTicks
            int currentNoDamageTicks = target.timeUntilRegen;
            target.timeUntilRegen = currentNoDamageTicks / 2;
        }
    }
}
