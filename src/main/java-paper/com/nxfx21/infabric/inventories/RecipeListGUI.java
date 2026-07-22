package com.nxfx21.infabric.inventories;

import com.nxfx21.infabric.commands.Recipes;
import com.nxfx21.infabric.managers.EffectMapping;
import com.nxfx21.infabric.util.InventoryUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RecipeListGUI implements InventoryHolder {
    private final Inventory inventory;

    public RecipeListGUI() {
        inventory = Bukkit.createInventory(this, 36, Component.text("Potion Crafting"));

        // Loading the potions into the inventory
        int[] customSlots = {0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32};

        int i = 0;
        for (EffectMapping effect : EffectMapping.values()) {
            if (effect.isAugmented()) continue;

            ItemStack potion = Recipes.createPotionWithModifiedLore(effect);
            inventory.setItem(customSlots[i], potion);
            i++;
        }

        InventoryUtils.fillRemainingSlots(inventory);

        // Locking the inventory
        InventoryUtils.lockInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}