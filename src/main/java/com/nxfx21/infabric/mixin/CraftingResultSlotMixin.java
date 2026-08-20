package com.nxfx21.infabric.mixin;

import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.managers.EffectCraftManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CraftingResultSlot.class)
public class CraftingResultSlotMixin {
    @Inject(method = "onTakeItem", at = @At("HEAD"))
    private void onTake(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            InfuseEffect effect = InfuseEffect.fromItem(stack);
            if (effect != null) {
                EffectCraftManager.handleCraftEffect(serverPlayer, effect, player.getBlockPos());
            }
        }
    }
}
