package com.nxfx21.infabric.mixin;

import com.nxfx21.infabric.effects.Emerald;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentScreenHandler.class)
public class EnchantmentScreenHandlerMixin {
    @Shadow @Final public int[] enchantmentPower;
    @Shadow @Final public int[] enchantmentLevel;

    @Unique
    private PlayerEntity infabric$player;

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("RETURN"))
    private void onInit(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        this.infabric$player = playerInventory.player;
    }

    @Inject(method = "onContentChanged", at = @At("RETURN"))
    private void onContentChangedInject(Inventory inventory, CallbackInfo ci) {
        if (this.infabric$player instanceof ServerPlayerEntity serverPlayer) {
            Emerald.applyEnchantmentBonus(serverPlayer, this.enchantmentPower, this.enchantmentLevel);
        }
    }
}
