package com.nxfx21.infabric.inventories;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.managers.RecipeManager;
import com.nxfx21.infabric.util.InventoryUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

public class RecipeGUI extends SimpleGui {

    public RecipeGUI(ServerPlayerEntity player, InfuseEffect effect, SimpleGui parentGui) {
        super(ScreenHandlerType.GENERIC_9X5, player, false);
        this.setTitle(Text.literal(effect.getName().toString() + " Recipe"));

        RecipeManager recipeManager = Infuse.getInstance().getRecipeManager();
        List<String> shape = recipeManager.getRecipeShape(effect);
        Map<Character, Item> ingredients = recipeManager.getRecipeIngredients(effect);

        int[] ingredientSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};

        if (shape.size() == 3) {
            int slotIdx = 0;
            for (int r = 0; r < 3; r++) {
                String row = shape.get(r);
                for (int c = 0; c < 3; c++) {
                    char ch = c < row.length() ? row.charAt(c) : ' ';
                    if (ch != ' ' && ingredients.containsKey(ch)) {
                        Item item = ingredients.get(ch);
                        this.setSlot(ingredientSlots[slotIdx], new ItemStack(item));
                    }
                    slotIdx++;
                }
            }
        }

        // Output item in slot 25
        this.setSlot(25, effect.createItem());

        // Back button in slot 36
        this.setSlot(36, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("§cBack"))
                .setCallback((index, type, action) -> {
                    if (parentGui != null) {
                        parentGui.open();
                    } else {
                        new RecipeListGUI(player).open();
                    }
                }));

        // Fill remaining slots
        InventoryUtils.fillRemainingSlots(this);
    }
}
