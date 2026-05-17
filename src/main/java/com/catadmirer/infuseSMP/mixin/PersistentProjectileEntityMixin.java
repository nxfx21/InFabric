package com.catadmirer.infuseSMP.mixin;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin extends ProjectileEntity {
    public PersistentProjectileEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Invoker("setPierceLevel")
    public abstract void invokeSetPierceLevel(byte level);

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void onSetOwner(Entity owner, CallbackInfo ci) {
        if (owner instanceof ServerPlayerEntity player) {
            Infuse plugin = Infuse.getInstance();

            // Strength: Piercing 100
            if (plugin.getDataManager().hasEffect(player, EffectMapping.STRENGTH)) {
                invokeSetPierceLevel((byte) 100);
            }

            // Fire: Set on fire
            if (plugin.getDataManager().hasEffect(player, EffectMapping.FIRE)) {
                ((PersistentProjectileEntity) (Object) this).setOnFireFor(100);
            }
            
            // Speed: Velocity multiplier (This is better handled in BowItem)
        }
    }
}
