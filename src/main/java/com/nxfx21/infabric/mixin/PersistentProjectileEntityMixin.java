package com.nxfx21.infabric.mixin;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.Fire;
import com.nxfx21.infabric.effects.InfuseEffect;
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

            InfuseEffect strengthEffect = InfuseEffect.fromString("strength");
            if (strengthEffect != null && plugin.getDataManager().hasEffect(player.getUuid(), strengthEffect)) {
                invokeSetPierceLevel((byte) 100);
            }

            Fire.onEntityShootBow(player, (PersistentProjectileEntity) (Object) this);
        }
    }
}
