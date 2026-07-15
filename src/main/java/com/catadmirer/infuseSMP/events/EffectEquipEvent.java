package com.catadmirer.infuseSMP.events;

import com.catadmirer.infuseSMP.effects.InfuseEffect;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface EffectEquipEvent {
    Event<EffectEquipEvent> EVENT = EventFactory.createArrayBacked(EffectEquipEvent.class,
            (listeners) -> (player, effect, slot) -> {
                for (EffectEquipEvent listener : listeners) {
                    if (!listener.onEquip(player, effect, slot)) {
                        return false;
                    }
                }
                return true;
            });

    boolean onEquip(ServerPlayerEntity player, InfuseEffect effect, String slot);
}
