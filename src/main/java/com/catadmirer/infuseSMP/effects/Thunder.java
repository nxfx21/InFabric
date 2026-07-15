package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Thunder extends InfuseEffect {
    private final Infuse plugin;

    public Thunder() {
        this(false);
    }

    public Thunder(boolean augmented) {
        super("thunder", EffectIds.THUNDER, augmented, EffectConstants.potionColor(EffectIds.THUNDER), EffectConstants.ritualColor(EffectIds.THUNDER));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {}

    @Override
    public void unequip(ServerPlayerEntity owner) {}

    @Override
    public void applyPassives(ServerPlayerEntity owner) {}

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "thunder")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "thunder", duration, cooldown);
        
        long durationTicks = duration * 20;
        scheduleStormTick(owner, owner.getServerWorld(), 0, durationTicks);
    }

    private void scheduleStormTick(ServerPlayerEntity owner, ServerWorld world, int ticksElapsed, long durationTicks) {
        if (ticksElapsed >= durationTicks) return;

        double baseRadius = 10;
        double radiusBoostPerPlayer = 0.3;
        double radius = baseRadius;

        while (true) {
            final double currentRadius = radius;
            long nearbyPlayersCount = world.getPlayers(p -> p.getPos().distanceTo(owner.getPos()) <= currentRadius).size();
            double tmp = baseRadius + radiusBoostPerPlayer * nearbyPlayersCount;
            if (tmp == radius) break;
            radius = tmp;
        }

        double finalRadius = radius;
        for (ServerPlayerEntity target : world.getPlayers()) {
            if (target == owner) continue;
            if (target.getPos().distanceTo(owner.getPos()) > finalRadius) continue;
            if (plugin.getDataManager().isTrusted(target.getUuid(), owner.getUuid())) continue;

            strikeLighting(target, owner);
        }

        int nextTick = ticksElapsed + 20;
        plugin.getHitTracker().scheduleTask(20L, () -> scheduleStormTick(owner, world, nextTick, durationTicks));
    }

    public static void strikeLighting(LivingEntity target, LivingEntity attacker) {
        ServerWorld world = (ServerWorld) target.getWorld();
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world, net.minecraft.entity.SpawnReason.EVENT);
        if (lightning != null) {
            lightning.refreshPositionAfterTeleport(target.getPos());
            lightning.setCosmetic(true);
            world.spawnEntity(lightning);
        }
        target.damage(world, world.getDamageSources().lightningBolt(), 2.0f);
    }

    public static void onTenHit(ServerPlayerEntity attacker, ServerPlayerEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect thunderEffect = InfuseEffect.fromString("thunder");
        if (thunderEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), thunderEffect)) return;

        strikeLighting(target, attacker);

        List<ServerPlayerEntity> targets = new ArrayList<>(List.of(attacker, target));
        plugin.getHitTracker().scheduleTask(20L, () -> chainLightning(targets));
    }

    private static void chainLightning(List<ServerPlayerEntity> targets) {
        if (targets.size() >= 11) return;

        Infuse plugin = Infuse.getInstance();
        ServerPlayerEntity attacker = targets.get(0);
        ServerPlayerEntity lastHit = targets.get(targets.size() - 1);
        double radius = 3.0;

        for (ServerPlayerEntity potentialTarget : lastHit.getServerWorld().getPlayers()) {
            if (plugin.getDataManager().isTrusted(attacker.getUuid(), potentialTarget.getUuid())) continue;
            if (targets.contains(potentialTarget)) continue;
            if (potentialTarget.getPos().distanceTo(lastHit.getPos()) > radius) continue;

            strikeLighting(potentialTarget, attacker);
            targets.add(potentialTarget);
            plugin.getHitTracker().scheduleTask(20L, () -> chainLightning(targets));
            break;
        }
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Thunder();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Thunder(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_THUNDER_NAME : MessageType.THUNDER_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_THUNDER_LORE : MessageType.THUNDER_LORE);
    }
}
