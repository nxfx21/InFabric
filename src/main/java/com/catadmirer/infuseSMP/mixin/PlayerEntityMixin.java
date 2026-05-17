package com.catadmirer.infuseSMP.mixin;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "canConsume", at = @At("HEAD"), cancellable = true)
    private void onCanConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (Infuse.getInstance().getDataManager().hasEffect(player.getUuid(), EffectMapping.REGEN)) {
            cir.setReturnValue(true);
        }
    }

    @ModifyVariable(method = "addExperience", at = @At("HEAD"), argsOnly = true)
    private int modifyExperience(int experience) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        Infuse plugin = Infuse.getInstance();
        
        if (plugin.getDataManager().hasEffect(player.getUuid(), EffectMapping.EMERALD)) {
            double multiplier = 2.0;
            if (com.catadmirer.infuseSMP.managers.CooldownManager.isEffectActive(player.getUuid(), "emerald")) {
                multiplier = 4.0;
            }
            experience = (int) Math.round(experience * multiplier);
        }
        return experience;
    }
}
