package com.nxfx21.infabric.mixin;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.InfuseEffect;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @Shadow
    private int foodLevel;
    @Shadow
    private float saturationLevel;
    @Shadow
    private float exhaustion;

    @Inject(method = "update", at = @At("HEAD"), cancellable = true)
    private void onUpdate(net.minecraft.server.network.ServerPlayerEntity player, CallbackInfo ci) {
        if (com.nxfx21.infabric.effects.Emerald.isLocked(player.getUuid())) {
            ci.cancel();
            return;
        }

        Infuse plugin = Infuse.getInstance();
        InfuseEffect regenEffect = InfuseEffect.fromString("regen");
        if (plugin != null && plugin.getDataManager() != null && regenEffect != null) {
            if (plugin.getDataManager().hasEffect(player.getUuid(), regenEffect)) {
                this.foodLevel = 20;
                this.saturationLevel = 20.0f;
                this.exhaustion = 0.0f;
            }
        }
    }
}
