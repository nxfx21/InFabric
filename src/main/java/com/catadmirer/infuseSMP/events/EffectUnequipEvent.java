package com.catadmirer.infuseSMP.events;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface EffectUnequipEvent {
    Event<EffectUnequipEvent> EVENT = EventFactory.createArrayBacked(EffectUnequipEvent.class,
            (listeners) -> (player, effect, slot) -> {
                for (EffectUnequipEvent listener : listeners) {
                    if (!listener.onUnequip(player, effect, slot)) {
                        return false;
                    }
                }
                return true;
            });

    boolean onUnequip(ServerPlayerEntity player, InfuseEffect effect, String slot);
}
