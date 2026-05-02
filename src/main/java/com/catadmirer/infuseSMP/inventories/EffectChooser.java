package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.managers.EffectMapping;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class EffectChooser extends SimpleGui {

    public EffectChooser(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.setTitle(Text.literal("Effect Chooser"));
        
        int slot = 0;
        for (EffectMapping mapping : EffectMapping.values()) {
            if (mapping.name().startsWith("AUG_")) continue; // Only regular ones or both?
            
            this.setSlot(slot++, mapping.createItem(), (index, type, action, gui) -> {
                player.getInventory().insertStack(mapping.createItem());
                player.sendMessage(Text.literal("Gave you " + mapping.getKey()), true);
            });
            
            if (mapping.augmented() != null) {
                this.setSlot(slot++, mapping.augmented().createItem(), (index, type, action, gui) -> {
                    player.getInventory().insertStack(mapping.augmented().createItem());
                    player.sendMessage(Text.literal("Gave you " + mapping.augmented().getKey()), true);
                });
            }
            
            if (slot >= 54) break;
        }
    }
}
