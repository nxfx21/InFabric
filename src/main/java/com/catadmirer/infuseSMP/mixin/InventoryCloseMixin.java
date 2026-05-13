package com.catadmirer.infuseSMP.mixin;

import com.catadmirer.infuseSMP.effects.Emerald;
import com.catadmirer.infuseSMP.effects.Haste;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.entity.player.PlayerEntity.class)
public class InventoryCloseMixin {
    @Inject(method = "onScreenHandlerClosed", at = @At("HEAD"))
    private void onInventoryClose(ScreenHandler handler, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayerEntity player)) return;
        
        // Cleanup top inventory (the one being closed)
        // Note: handler.getSlot(0).inventory is usually the top inventory in common containers
        if (handler.slots.size() > 0) {
            net.minecraft.inventory.Inventory topInventory = handler.getSlot(0).inventory;
            // Ensure we're not cleaning the player's own inventory twice (though Emerald/Haste check it anyway)
            if (topInventory != player.getInventory()) {
                Emerald.cleanupInventory(topInventory, player);
                Haste.cleanupInventory(topInventory, player);
            }
        }
    }
}
