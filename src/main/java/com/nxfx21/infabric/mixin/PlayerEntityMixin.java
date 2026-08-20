package com.nxfx21.infabric.mixin;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.InfuseEffect;
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
        if (com.nxfx21.infabric.effects.Emerald.isLocked(player.getUuid()) || com.nxfx21.infabric.effects.Apophis.isLocked(player.getUuid())) {
            cir.setReturnValue(false);
            return;
        }

        Infuse plugin = Infuse.getInstance();
        if (plugin != null && plugin.getDataManager() != null && plugin.getMainConfig() != null && plugin.getMainConfig().regenCanAlwaysEat()) {
            InfuseEffect regenEffect = InfuseEffect.fromString("regen");
            if (regenEffect != null && plugin.getDataManager().hasEffect(player.getUuid(), regenEffect)) {
                cir.setReturnValue(true);
            }
        }
    }

    @ModifyVariable(method = "addExperience", at = @At("HEAD"), argsOnly = true)
    private int modifyExperience(int experience) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (com.nxfx21.infabric.effects.Emerald.isLocked(player.getUuid()) || com.nxfx21.infabric.effects.Apophis.isLocked(player.getUuid())) {
            return 0;
        }

        Infuse plugin = Infuse.getInstance();
        InfuseEffect emeraldEffect = InfuseEffect.fromString("emerald");
        
        if (experience > 0 && emeraldEffect != null && plugin.getDataManager().hasEffect(player.getUuid(), emeraldEffect)) {
            double multiplier = plugin.getMainConfig().emeraldMultiplierStandard();
            if (com.nxfx21.infabric.managers.CooldownManager.isEffectActive(player.getUuid(), "emerald")) {
                multiplier = plugin.getMainConfig().emeraldMultiplierUseEffect();
            }
            experience = (int) Math.round(experience * multiplier);
        }
        return experience;
    }

    @Inject(method = "addExperienceLevels", at = @At("HEAD"), cancellable = true)
    private void onAddExperienceLevels(int levels, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (com.nxfx21.infabric.effects.Emerald.isLocked(player.getUuid()) || com.nxfx21.infabric.effects.Apophis.isLocked(player.getUuid())) {
            ci.cancel();
        }
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("RETURN"))
    private void onDropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity entity = cir.getReturnValue();
        if (entity != null && !entity.getWorld().isClient()) {
            Infuse.getInstance().getDropManager().onDrop(entity);
        }
    }

}
