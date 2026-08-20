package com.nxfx21.infabric;

import com.nxfx21.infabric.events.TenHitEvent;
import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.effects.Thunder;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HitTracker {
    private final Infuse plugin;
    private final Map<UUID, Integer> hitTracker = new HashMap<>();
    private final Queue<Runnable> decayQueue = new ConcurrentLinkedQueue<>();

    private static class ScheduledTask {
        long runAtTick;
        Runnable task;
        ScheduledTask(long runAtTick, Runnable task) {
            this.runAtTick = runAtTick;
            this.task = task;
        }
    }
    private final List<ScheduledTask> scheduledTasks = new ArrayList<>();
    private long currentTick = 0;

    public HitTracker() {
        this.plugin = Infuse.getInstance();
    }

    public void registerHit(UUID playerUuid) {
        if (playerUuid == null) return;
        int hits = hitTracker.getOrDefault(playerUuid, 0) + 1;
        hitTracker.put(playerUuid, hits);
    }

    public void registerHit(ServerPlayerEntity player) {
        if (player != null) {
            registerHit(player.getUuid());
        }
    }

    public int getHits(UUID playerUuid) {
        if (playerUuid == null) return 0;
        return hitTracker.getOrDefault(playerUuid, 0);
    }

    public int getHits(ServerPlayerEntity player) {
        return player != null ? getHits(player.getUuid()) : 0;
    }

    public void resetHits(UUID playerUuid) {
        if (playerUuid == null) return;
        hitTracker.put(playerUuid, 0);
    }

    public void resetHits(ServerPlayerEntity player) {
        if (player != null) {
            resetHits(player.getUuid());
        }
    }

    public void registerEvents() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) return ActionResult.PASS;

            if (!(player instanceof ServerPlayerEntity attacker) || !(entity instanceof ServerPlayerEntity target)) {
                return ActionResult.PASS;
            }

            // Skipping the hit if the attacker trusts the target
            if (plugin.getDataManager().isTrusted(attacker.getUuid(), target.getUuid())) {
                return ActionResult.PASS;
            }

            // Vanilla attack cooldown needs to be at 84.8% to be a normal hit.
            float cooldownProgress = attacker.getAttackCooldownProgress(0.5f);
            if (cooldownProgress < 0.85f) {
                return ActionResult.PASS;
            }

            if (entity instanceof net.minecraft.entity.LivingEntity living) {
                com.nxfx21.infabric.effects.Speed.onAttack(attacker, living);
                com.nxfx21.infabric.effects.Regen.onAttack(attacker, living, 0f);
            }

            int hits = hitTracker.getOrDefault(attacker.getUuid(), 0) + 1;
            
            // Incrementing by 2 if the thunder effect is registered, the attacker has it, and if they are in the rain.
            InfuseEffect thunder = InfuseEffect.fromString("thunder");
            if (thunder != null && plugin.getDataManager().hasEffect(attacker.getUuid(), thunder) && attacker.getWorld().hasRain(attacker.getBlockPos())) {
                hits += 1;
            }
            
            if (hits >= 10) {
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

            // Schedule the decay using actual server ticks
            scheduledTasks.add(new ScheduledTask(currentTick + hitCounterDecaySeconds * 20L, () -> {
                Runnable decayTask = decayQueue.peek();
                if (decayTask != null) {
                    decayQueue.remove();
                    decayTask.run();
                }
            }));

            return ActionResult.PASS;
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            hitTracker.remove(handler.player.getUuid());
        });
    }

    public void scheduleTask(long delayTicks, Runnable task) {
        scheduledTasks.add(new ScheduledTask(currentTick + delayTicks, task));
    }

    public void tick() {
        currentTick++;
        scheduledTasks.removeIf(task -> {
            if (currentTick >= task.runAtTick) {
                task.task.run();
                return true;
            }
            return false;
        });
    }
}
