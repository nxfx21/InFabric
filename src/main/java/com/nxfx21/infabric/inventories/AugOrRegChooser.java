package com.nxfx21.infabric.inventories;

import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.util.InventoryUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class AugOrRegChooser extends SimpleGui {

    public AugOrRegChooser(ServerPlayerEntity player, InfuseEffect effect) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);
        this.setTitle(Text.literal("Choose Effect Type"));

        // Fill background
        InventoryUtils.fillRemainingSlots(this);

        InfuseEffect regular = effect.getRegularVersion();
        InfuseEffect augmented = effect.getAugmentedVersion();

        // Regular effect in slot 11
        if (regular != null) {
            this.setSlot(11, new GuiElementBuilder(regular.createItem())
                    .setCallback((index, type, action) -> {
                        new RecipeGUI(player, regular, this).open();
                    }));
        }

        // Augmented effect in slot 15
        if (augmented != null) {
            this.setSlot(15, new GuiElementBuilder(augmented.createItem())
                    .setCallback((index, type, action) -> {
                        new RecipeGUI(player, augmented, this).open();
                    }));
        }

        // Back button in slot 22
        this.setSlot(22, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("§cBack to Recipes"))
                .setCallback((index, type, action) -> {
                    new RecipeListGUI(player).open();
                }));
    }
}
