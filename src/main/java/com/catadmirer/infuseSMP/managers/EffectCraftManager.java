package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Infuse;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.util.ActionResult;

public class EffectCraftManager {

    public EffectCraftManager() {
    }

    public void registerEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient() && world.getBlockState(hitResult.getBlockPos()).isOf(Blocks.BREWING_STAND)) {
                // TODO: Open custom brewing stand GUI using SGUI
                Infuse.LOGGER.info("Player clicked brewing stand, should open SGUI crafting menu.");
                return ActionResult.SUCCESS; // Prevent normal brewing stand opening
            }
            return ActionResult.PASS;
        });

        // TODO: Handle CraftItemEvent (when an item is successfully crafted)
        // Fabric doesn't have an exact CraftItemEvent, so we may need a Mixin into CraftingScreenHandler
        
        // TODO: Handle ritual bossbar and ender crystal spawning when Augmented effect is crafted
    }


    public static void removeBeam() {
        // Remove ender crystal beam
    }
}
