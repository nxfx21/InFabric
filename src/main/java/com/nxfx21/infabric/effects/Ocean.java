package com.nxfx21.infabric.effects;

import com.nxfx21.infabric.EffectConstants;
import com.nxfx21.infabric.EffectIds;
import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.managers.CooldownManager;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Ocean extends InfuseEffect {
    private final Infuse plugin;

    public Ocean() {
        this(false);
    }

    public Ocean(boolean augmented) {
        super("ocean", EffectIds.OCEAN, augmented, EffectConstants.potionColor(EffectIds.OCEAN), EffectConstants.ritualColor(EffectIds.OCEAN));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {}

    @Override
    public void unequip(ServerPlayerEntity owner) {}

    @Override
    public void applyPassives(ServerPlayerEntity owner) {
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, 40, 0, false, false));
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.DOLPHINS_GRACE, 40, 0, false, false));
        
        int drownStrength = plugin.getMainConfig().oceanPassiveDrownStrength();
        int drownDamage = plugin.getMainConfig().oceanPassiveDrownDamage();
        if (CooldownManager.isEffectActive(owner.getUuid(), "ocean")) {
            drownStrength = plugin.getMainConfig().oceanSparkDrownStrength();
            drownDamage = plugin.getMainConfig().oceanSparkDrownDamage();
        }

        for (ServerPlayerEntity nearby : owner.getServerWorld().getPlayers()) {
            if (nearby == owner) continue;
            if (plugin.getDataManager().isTrusted(nearby.getUuid(), owner.getUuid())) continue;
            if (nearby.getPos().distanceTo(owner.getPos()) <= 5) {
                nearby.setAir(Math.max(-20, nearby.getAir() - drownStrength));
                if (nearby.getAir() <= 0) {
                    nearby.damage(owner.getServerWorld(), owner.getServerWorld().getDamageSources().drown(), (float) drownDamage);
                }
            }
        }

        if (CooldownManager.isEffectActive(owner.getUuid(), "ocean")) {
            applyPullEffect(owner);
            spawnSparkParticles(owner);
        }
    }

    private void applyPullEffect(ServerPlayerEntity caster) {
        double radius = plugin.getMainConfig().oceanPullRadius();
        double strength = plugin.getMainConfig().oceanPullStrength();

        for (ServerPlayerEntity p : caster.getServerWorld().getPlayers()) {
            if (p == caster) continue;
            if (plugin.getDataManager().isTrusted(caster.getUuid(), p.getUuid())) continue;
            if (p.getPos().distanceTo(caster.getPos()) > radius) continue;

            net.minecraft.util.math.Vec3d direction = caster.getPos().subtract(p.getPos()).normalize();
            p.setVelocity(p.getVelocity().add(direction.multiply(strength)));
            p.velocityModified = true;
        }
    }

    private void spawnSparkParticles(ServerPlayerEntity player) {
        double radius = 5;
        net.minecraft.server.world.ServerWorld world = player.getServerWorld();
        for (int angle = 0; angle < 360; angle += 20) {
            double rad = Math.toRadians(angle);
            double x = player.getX() + radius * Math.cos(rad);
            double z = player.getZ() + radius * Math.sin(rad);
            world.spawnParticles(net.minecraft.particle.ParticleTypes.FALLING_WATER, x, player.getY() + 1, z, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "ocean")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "ocean", duration, cooldown);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Ocean();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Ocean(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_OCEAN_NAME : MessageType.OCEAN_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_OCEAN_LORE : MessageType.OCEAN_LORE);
    }
}
