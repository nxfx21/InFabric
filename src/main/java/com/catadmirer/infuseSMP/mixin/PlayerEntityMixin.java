package com.catadmirer.infuseSMP.mixin;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.effects.InfuseEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
    private void onCanConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        InfuseEffect regenEffect = InfuseEffect.fromString("regen");
        if (regenEffect != null && Infuse.getInstance().getDataManager().hasEffect(player.getUuid(), regenEffect)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "addExperience", at = @At("HEAD"), argsOnly = true)
    private int modifyExperience(int experience) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (com.catadmirer.infuseSMP.effects.Emerald.isLocked(player.getUuid())) {
            return 0;
        }

        Infuse plugin = Infuse.getInstance();
        InfuseEffect emeraldEffect = InfuseEffect.fromString("emerald");
        
        if (emeraldEffect != null && plugin.getDataManager().hasEffect(player.getUuid(), emeraldEffect)) {
            double multiplier = plugin.getMainConfig().emeraldMultiplierStandard();
            if (com.catadmirer.infuseSMP.managers.CooldownManager.isEffectActive(player.getUuid(), "emerald")) {
                multiplier = plugin.getMainConfig().emeraldMultiplierUseEffect();
            }
            experience = (int) Math.round(experience * multiplier);
        }
        return experience;
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("RETURN"))
    private void onDropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity entity = cir.getReturnValue();
        if (entity != null && !entity.getWorld().isClient()) {
            Infuse.getInstance().getDropManager().onDrop(entity);
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
}
