package com.catadmirer.infuseSMP.events;

import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface EffectEquipEvent {
    Event<EffectEquipEvent> EVENT = EventFactory.createArrayBacked(EffectEquipEvent.class,
            (listeners) -> (player, effect, slot) -> {
                for (EffectEquipEvent listener : listeners) {
                    listener.onEquip(player, effect, slot);
                }
            });

    void onEquip(ServerPlayerEntity player, EffectMapping effect, String slot);
}
