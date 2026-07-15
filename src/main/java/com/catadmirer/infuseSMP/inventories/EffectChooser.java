package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class EffectChooser extends SimpleGui {

    public EffectChooser(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.setTitle(Text.literal("Effect Chooser"));
        
        int slot = 0;
        for (InfuseEffect effect : InfuseEffect.getRegisteredEffects().values()) {
            if (effect.isAugmented()) continue; // Handled below or handled individually
            
            this.setSlot(slot++, effect.createItem(), (index, type, action, gui) -> {
                player.getInventory().insertStack(effect.createItem());
                player.sendMessage(Text.literal("Gave you " + effect.getKey()), true);
            });
            
            InfuseEffect augmented = effect.getAugmentedVersion();
            if (augmented != null) {
                this.setSlot(slot++, augmented.createItem(), (index, type, action, gui) -> {
                    player.getInventory().insertStack(augmented.createItem());
                    player.sendMessage(Text.literal("Gave you " + augmented.getKey()), true);
                });
            }
            
            if (slot >= 54) break;
        }
    }
}
