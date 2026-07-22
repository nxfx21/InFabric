package com.catadmirer.infuseSMP.mixin;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.Strength;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float modifyDamageAmount(float amount, DamageSource source) {
        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            Infuse plugin = Infuse.getInstance();
            com.catadmirer.infuseSMP.effects.InfuseEffect strengthEffect = com.catadmirer.infuseSMP.effects.InfuseEffect.fromString("strength");
            
            // Strength Effect Logic
            if (strengthEffect != null && plugin.getDataManager().hasEffect(attacker.getUuid(), strengthEffect)) {
                amount = Strength.getExtraDamage(attacker, amount);
                amount = Strength.applySparkAutoCrit(attacker, amount);
                amount = Strength.modifyAttackDamage(attacker, (LivingEntity) (Object) this, amount);
            }

            com.catadmirer.infuseSMP.effects.Emerald.onAttack(attacker, (LivingEntity) (Object) this);
            com.catadmirer.infuseSMP.effects.Ender.onAttack(attacker, (LivingEntity) (Object) this);
            com.catadmirer.infuseSMP.effects.Thief.onAttack(attacker, (LivingEntity) (Object) this);
        }

        if (source.getSource() instanceof net.minecraft.entity.projectile.PersistentProjectileEntity projectile) {
            if (projectile.getOwner() instanceof ServerPlayerEntity shooter) {
                com.catadmirer.infuseSMP.effects.InfuseEffect invisEffect = com.catadmirer.infuseSMP.effects.InfuseEffect.fromString("invis");
                if (invisEffect != null && Infuse.getInstance().getDataManager().hasEffect(shooter.getUuid(), invisEffect)) {
                    if ((Object) this instanceof ServerPlayerEntity target) {
                        target.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.BLINDNESS, 80, 0, false, false));
                        com.catadmirer.infuseSMP.effects.Invis.spawnBlackParticles(target, 4);
                    }
                }
            }
        }

        return amount;
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamageHead(DamageSource source, float amount, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            com.catadmirer.infuseSMP.effects.Feather.onFallDamage(player, source, amount, cir);
        }
    }

    @Inject(method = "sendPickup", at = @At("HEAD"))
    private void onSendPickup(Entity entity, int count, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            if (entity instanceof ItemEntity itemEntity) {
                Infuse.getInstance().getDropManager().onPickup(itemEntity, player);
            }
        }
    }

    @Inject(method = "consumeItem", at = @At("HEAD"))
    private void onConsumeItem(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            net.minecraft.item.ItemStack stack = ((LivingEntity) (Object) this).getActiveItem();
            Infuse.getInstance().getEffectManager().onDrinkEffect(player, stack);
            com.catadmirer.infuseSMP.effects.Emerald.onConsume(player, stack);
        }
    }
}
