package com.catadmirer.infuseSMP.managers;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;

public class Drop {

    public Drop() {
    }

    public void registerEvents() {
        // TODO: Register item pickup event (using Mixin)
    }

    public void onDrop(ItemEntity droppedItem) {
        ItemStack itemStack = droppedItem.getStack();
        EffectMapping mapping = EffectMapping.fromItem(itemStack);
        if (mapping == null) return;
        playDustEffect(false, mapping, droppedItem);
        droppedItem.setGlowing(true);
    }

    private void playDustEffect(boolean bottomToTop, EffectMapping effect, ItemEntity locationEntity) {
        // Spawning dust particles. Ported logic here...
        // TODO: Port BukkitRunnable circle particle effect.
    }
}
