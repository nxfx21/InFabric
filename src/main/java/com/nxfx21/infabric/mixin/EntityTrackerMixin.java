package com.nxfx21.infabric.mixin;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.Invis;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(targets = "net.minecraft.server.world.ServerChunkLoadingManager$EntityTracker")
public class EntityTrackerMixin implements EntityTrackerAccessor {
    @Shadow
    @Final
    Entity entity;

    @Shadow
    public void stopTracking(ServerPlayerEntity player) {}

    @Shadow
    public void updateTrackedStatus(List<ServerPlayerEntity> players) {}

    @Inject(method = "updateTrackedStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("HEAD"), cancellable = true)
    private void onUpdateTrackedStatus(ServerPlayerEntity player, CallbackInfo ci) {
        if (this.entity instanceof ServerPlayerEntity vanishedPlayer) {
            if (Invis.activeVanish.contains(vanishedPlayer.getUuid())) {
                if (!vanishedPlayer.equals(player) && !Infuse.getInstance().getDataManager().isTrusted(player.getUuid(), vanishedPlayer.getUuid())) {
                    this.stopTracking(player);
                    ci.cancel();
                }
            }
        }
    }
}
