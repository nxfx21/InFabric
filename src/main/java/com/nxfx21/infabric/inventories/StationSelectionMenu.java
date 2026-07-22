package com.nxfx21.infabric.inventories;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class StationSelectionMenu extends SimpleGui {
    public StationSelectionMenu(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.setTitle(Text.literal("Station Selection"));
        
        net.minecraft.item.ItemStack brewingStand = new net.minecraft.item.ItemStack(net.minecraft.item.Items.BREWING_STAND);
        brewingStand.set(net.minecraft.component.DataComponentTypes.ITEM_NAME, Text.literal("Brewing Stand"));
        this.setSlot(11, brewingStand, (index, type, action, gui) -> {
            // player.openHandledScreen(...) - This is complex for a non-block-based GUI.
            // For now, we'll just close and tell them to use a real brewing stand or implementation-specific logic.
            player.sendMessage(Text.literal("Use a real brewing stand for now."), true);
            gui.close();
        });

        net.minecraft.item.ItemStack craftingTable = new net.minecraft.item.ItemStack(net.minecraft.item.Items.CRAFTING_TABLE);
        craftingTable.set(net.minecraft.component.DataComponentTypes.ITEM_NAME, Text.literal("Effect Crafting"));
        this.setSlot(15, craftingTable, (index, type, action, gui) -> {
            new EffectChooser(player).open();
        });
    }
}
