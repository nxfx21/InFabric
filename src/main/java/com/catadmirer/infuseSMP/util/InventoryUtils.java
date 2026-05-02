package com.catadmirer.infuseSMP.util;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

public class InventoryUtils {
    public static ItemStack createNoName(Item item) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponentTypes.ITEM_NAME, Text.empty());
        return stack;
    }

    public static void fillInventory(SimpleGui gui, ItemStack item) {
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setSlot(i, item.copy());
        }
    }

    public static void setItems(SimpleGui gui, int[] slots, ItemStack item) {
        for (int slot : slots) {
            gui.setSlot(slot, item.copy());
        }
    }

    public static void fillRemainingSlots(SimpleGui gui) {
        ItemStack pane = createNoName(Items.RED_STAINED_GLASS_PANE);
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getSlot(i) == null || gui.getSlot(i).getItemStack().isEmpty()) {
                gui.setSlot(i, pane.copy());
            }
        }
    }
}