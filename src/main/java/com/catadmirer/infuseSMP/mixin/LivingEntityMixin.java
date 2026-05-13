package com.catadmirer.infuseSMP.mixin;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.Strength;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float modifyDamageAmount(float amount, DamageSource source) {
        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            Infuse plugin = Infuse.getInstance();
            
            // Strength Effect Logic
            if (plugin.getDataManager().hasEffect(attacker, EffectMapping.STRENGTH)) {
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
        return amount;
    }
}
