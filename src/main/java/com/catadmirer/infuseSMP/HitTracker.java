package com.catadmirer.infuseSMP;

import com.catadmirer.infuseSMP.events.TenHitEvent;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HitTracker {
    private final Infuse plugin;
    private final Map<UUID, Integer> hitTracker = new HashMap<>();
    private final Queue<Runnable> decayQueue = new ConcurrentLinkedQueue<>();

    public HitTracker() {
        this.plugin = Infuse.getInstance();
    }

    public void registerEvents() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;

            if (!(player instanceof ServerPlayerEntity attacker) || !(entity instanceof ServerPlayerEntity target)) {
                return ActionResult.PASS;
            }

            // In Fabric, getAttackCooldownProgress takes a tick delta (e.g. 0.5f)
            float cooldownProgress = attacker.getAttackCooldownProgress(0.5f);
            if (cooldownProgress < 0.85f) {
                return ActionResult.PASS;
            }

            if (entity instanceof net.minecraft.entity.LivingEntity living) {
                com.catadmirer.infuseSMP.effects.Speed.onAttack(attacker, living);
                com.catadmirer.infuseSMP.effects.Regen.onAttack(attacker, living);
            }

            int hits = hitTracker.getOrDefault(attacker.getUuid(), 0) + 1;
            
            if (hits == 10) {
                TenHitEvent.EVENT.invoker().onTenHits(attacker, target);
                hitTracker.put(attacker.getUuid(), 0);

                for (int i = 0; i < 10; i++) {
                    if (!decayQueue.isEmpty()) decayQueue.remove();
                }
                return ActionResult.PASS;
            }

            hitTracker.put(attacker.getUuid(), hits);

            int hitCounterDecaySeconds = plugin.getMainConfig().hitCounterDecaySeconds();
            if (hitCounterDecaySeconds < 1) return ActionResult.PASS;

            decayQueue.add(() -> {
                if (attacker.isDisconnected()) return;
                int curHits = hitTracker.getOrDefault(attacker.getUuid(), 0);
                if (curHits > 0) {
                    hitTracker.put(attacker.getUuid(), curHits - 1);
                }
            });

            // Schedule the decay using a custom scheduler or MinecraftServer timer (simplified here)
            // TODO: Use actual server ticks to schedule `decayQueue.peek().run()` later.
            // (We'll integrate this with GlobalLoop or a Scheduler class)

            return ActionResult.PASS;
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            hitTracker.remove(handler.player.getUuid());
        });
    }

    public void tick() {
        // This is a simplified decay logic. In a real scenario, you'd want to track the time for each hit separately.
        // But for this port, we'll just run one decay task every few ticks if the queue is not empty.
        Runnable decayTask = decayQueue.poll();
        if (decayTask != null) {
            decayTask.run();
        }
    }
}
