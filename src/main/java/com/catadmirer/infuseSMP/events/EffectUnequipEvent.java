package com.catadmirer.infuseSMP.events;

import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface EffectUnequipEvent {
    Event<EffectUnequipEvent> EVENT = EventFactory.createArrayBacked(EffectUnequipEvent.class,
            (listeners) -> (player, effect, slot) -> {
                for (EffectUnequipEvent listener : listeners) {
                    listener.onUnequip(player, effect, slot);
                }
            });

    void onUnequip(ServerPlayerEntity player, EffectMapping effect, String slot);
}
