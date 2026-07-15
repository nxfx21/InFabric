package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class Feather extends InfuseEffect {
    private final Infuse plugin;

    public Feather() {
        this(false);
    }

    public Feather(boolean augmented) {
        super("feather", EffectIds.FEATHER, augmented, EffectConstants.potionColor(EffectIds.FEATHER), EffectConstants.ritualColor(EffectIds.FEATHER));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {}

    @Override
    public void unequip(ServerPlayerEntity owner) {}

    @Override
    public void applyPassives(ServerPlayerEntity owner) {}

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "feather")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        owner.setVelocity(0, 1, 0);
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 20, 10));
        owner.velocityDirty = true;

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "feather", duration, cooldown);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Feather();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Feather(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_FEATHER_NAME : MessageType.FEATHER_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_FEATHER_LORE : MessageType.FEATHER_LORE);
    }

    public static void onFallDamage(ServerPlayerEntity player, net.minecraft.entity.damage.DamageSource source, float amount, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect featherEffect = InfuseEffect.fromString("feather");
        if (featherEffect != null && plugin.getDataManager().hasEffect(player.getUuid(), featherEffect)) {
            if (source.isOf(net.minecraft.entity.damage.DamageTypes.FALL)) {
                cir.setReturnValue(false);
            }
        }
    }

    public static void onAttack(ServerPlayerEntity attacker, LivingEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect featherEffect = InfuseEffect.fromString("feather");
        if (featherEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), featherEffect)) return;

        if (attacker.fallDistance >= 7.0f) {
            attacker.getWorld().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.ITEM_MACE_SMASH_AIR, SoundCategory.PLAYERS, 1.0f, 1.0f);
            attacker.getServerWorld().spawnParticles(net.minecraft.particle.ParticleTypes.GUST_EMITTER_SMALL, target.getX(), target.getY(), target.getZ(), 1, 0, 0, 0, 0);
            attacker.setVelocity(attacker.getVelocity().x, 1.8, attacker.getVelocity().z);
            attacker.velocityModified = true;
        }
    }

    public static void onTenHit(ServerPlayerEntity attacker, ServerPlayerEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect featherEffect = InfuseEffect.fromString("feather");
        if (featherEffect == null || !plugin.getDataManager().hasEffect(target.getUuid(), featherEffect)) return;

        attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOW_FALLING, 100, 2));
        
        WindChargeEntity windCharge = new WindChargeEntity(target, target.getWorld(), target.getX(), target.getY() + 1.0, target.getZ());
        windCharge.setVelocity(new Vec3d(0, -1.0, 0));
        target.getWorld().spawnEntity(windCharge);

        target.setVelocity(new Vec3d(0, 0.5, 0));
        target.velocityDirty = true;
    }
}
