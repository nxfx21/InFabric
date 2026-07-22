package com.nxfx21.infabric.inventories;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.InfuseEffect;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class RecipeListGUI extends SimpleGui {

    public RecipeListGUI(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.setTitle(Text.literal("Recipes"));
        
        int slot = 0;
        for (InfuseEffect effect : InfuseEffect.getRegisteredEffects().values()) {
            if (effect.isAugmented()) continue;
            
            if (Infuse.getInstance().getRecipeManager().isRecipeEnabled(effect)) {
                this.setSlot(slot++, effect.createItem());
            }
            
            if (slot >= 54) break;
        }
    }
}
