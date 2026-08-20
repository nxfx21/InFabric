package com.nxfx21.infabric.mixin;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.InfuseEffect;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"), cancellable = true)
    private void onBroadcastDeath(DamageSource damageSource, CallbackInfo ci) {
        Infuse plugin = Infuse.getInstance();
        if (plugin == null || plugin.getMainConfig() == null || plugin.getDataManager() == null) return;
        InfuseEffect invis = InfuseEffect.fromString("invis");
        if (invis == null) return;

        ServerPlayerEntity victim = (ServerPlayerEntity) (Object) this;
        if (plugin.getMainConfig().invisHideDeaths() && plugin.getDataManager().hasEffect(victim.getUuid(), invis)) {
            ci.cancel();
            return;
        }

        if (damageSource.getAttacker() instanceof ServerPlayerEntity killer) {
            if (plugin.getMainConfig().invisHideKills() && plugin.getDataManager().hasEffect(killer.getUuid(), invis)) {
                ci.cancel();
            }
        }
    }
}
