package com.nxfx21.infabric.inventories;

import com.nxfx21.infabric.managers.EffectMapping;
import com.nxfx21.infabric.EffectConstants;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.util.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class AugOrRegChooser implements InventoryHolder {
    private final Inventory inventory;

    public AugOrRegChooser(EffectMapping effect) {
        inventory = Bukkit.createInventory(this, 27, Message.toComponent("<yellow>Choose"));
        
        // Filling the inventory with a filler item.
        InventoryUtils.fillInventory(inventory, InventoryUtils.createNoName(EffectConstants.menuBackgroundColor(effect.getId())));

        // Adding the effects to the inventory
        inventory.setItem(11, effect.regular().createItem());
        inventory.setItem(15, effect.augmented().createItem());

        // Locking the inventory
        InventoryUtils.lockInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}