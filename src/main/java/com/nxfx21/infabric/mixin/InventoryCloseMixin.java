package com.nxfx21.infabric.mixin;

import com.nxfx21.infabric.effects.Emerald;
import com.nxfx21.infabric.effects.Haste;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.entity.player.PlayerEntity.class)
public class InventoryCloseMixin {
    @Inject(method = "onHandledScreenClosed", at = @At("HEAD"))
    private void onInventoryClose(CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayerEntity player)) return;
        ScreenHandler handler = player.currentScreenHandler;
        if (handler != null && handler.slots.size() > 0) {
            net.minecraft.inventory.Inventory topInventory = handler.getSlot(0).inventory;
            if (topInventory != player.getInventory()) {
                Emerald.cleanupInventory(topInventory, player);
                Haste.cleanupInventory(topInventory, player);
            }
        }
    }
}
