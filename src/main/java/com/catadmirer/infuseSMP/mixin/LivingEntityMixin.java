package com.catadmirer.infuseSMP.mixin;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.Strength;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

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
            if (strengthEffect != null && plugin.getDataManager().hasEffect(attacker, strengthEffect)) {
                // Double damage to mobs (not players)
                if (!((Object) this instanceof PlayerEntity)) {
                    amount *= 2.0f;
                }
                
                // Adrenaline bonus
                amount = Strength.getExtraDamage(attacker, amount);
                
                // Auto-crit spark
                amount = Strength.applySparkAutoCrit(attacker, amount);
            }
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

    @org.spongepowered.asm.mixin.injection.Inject(method = "finishUsing", at = @At("HEAD"))
    private void onFinishUsing(World world, net.minecraft.item.ItemStack stack, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.item.ItemStack> cir) {
        if ((Object) this instanceof ServerPlayerEntity player) {
            Infuse.getInstance().getEffectManager().onDrinkEffect(player, stack);
        }
    }
}
