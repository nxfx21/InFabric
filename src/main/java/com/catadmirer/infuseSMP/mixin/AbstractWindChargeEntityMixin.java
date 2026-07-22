package com.catadmirer.infuseSMP.mixin;

import com.catadmirer.infuseSMP.effects.Frost;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractWindChargeEntity.class)
public abstract class AbstractWindChargeEntityMixin {
    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void onWindChargeEntityHit(EntityHitResult entityHitResult, CallbackInfo ci) {
        AbstractWindChargeEntity windCharge = (AbstractWindChargeEntity) (Object) this;
        if (windCharge.getOwner() instanceof ServerPlayerEntity player) {
            Frost.onWindChargeHit(player, entityHitResult.getEntity());
        }
    }

    @Inject(method = "onCollision", at = @At("HEAD"))
    private void onWindChargeCollision(HitResult hitResult, CallbackInfo ci) {
        AbstractWindChargeEntity windCharge = (AbstractWindChargeEntity) (Object) this;
        if (windCharge.getOwner() instanceof ServerPlayerEntity player) {
            Frost.onWindChargeExplode(player, windCharge.getPos(), 4.0);
        }
    }
}
