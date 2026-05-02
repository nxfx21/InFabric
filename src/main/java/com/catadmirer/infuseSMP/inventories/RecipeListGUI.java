package com.catadmirer.infuseSMP.inventories;

import com.catadmirer.infuseSMP.Infuse;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class RecipeListGUI extends SimpleGui {

    public RecipeListGUI(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.setTitle(Text.literal("Recipes"));
        
        int slot = 0;
        for (com.catadmirer.infuseSMP.managers.EffectMapping mapping : com.catadmirer.infuseSMP.managers.EffectMapping.values()) {
            if (mapping.name().startsWith("AUG_")) continue;
            
            if (Infuse.getInstance().getRecipeManager().isRecipeEnabled(mapping)) {
                this.setSlot(slot++, mapping.createItem());
            }
            
            if (slot >= 54) break;
        }
    }
}
