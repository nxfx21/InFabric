package com.catadmirer.infuseSMP.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface TenHitEvent {
    Event<TenHitEvent> EVENT = EventFactory.createArrayBacked(TenHitEvent.class,
            (listeners) -> (attacker, target) -> {
                for (TenHitEvent listener : listeners) {
                    listener.onTenHits(attacker, target);
                }
            });

    void onTenHits(ServerPlayerEntity attacker, ServerPlayerEntity target);
}
