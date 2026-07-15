package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Regen extends InfuseEffect {
    private final Infuse plugin;

    public Regen() {
        this(false);
    }

    public Regen(boolean augmented) {
        super("regen", EffectIds.REGEN, augmented, EffectConstants.potionColor(EffectIds.REGEN), EffectConstants.ritualColor(EffectIds.REGEN));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, -1, 0, false, false));
    }

    @Override
    public void unequip(ServerPlayerEntity owner) {
        owner.removeStatusEffect(StatusEffects.REGENERATION);
    }

    @Override
    public void applyPassives(ServerPlayerEntity owner) {
        owner.getHungerManager().setFoodLevel(20);
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "regen")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "regen", duration, cooldown);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Regen();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Regen(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_REGEN_NAME : MessageType.REGEN_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_REGEN_LORE : MessageType.REGEN_LORE);
    }

    public static void onAttack(ServerPlayerEntity attacker, LivingEntity target, float damage) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect regenEffect = InfuseEffect.fromString("regen");
        if (regenEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), regenEffect)) return;

        attacker.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 60, 1, false, false));
        
        if (CooldownManager.isEffectActive(attacker.getUuid(), "regen")) {
            for (net.minecraft.entity.Entity entity : attacker.getWorld().getOtherEntities(attacker, attacker.getBoundingBox().expand(5))) {
                if (entity instanceof ServerPlayerEntity nearby && plugin.getDataManager().isTrusted(attacker.getUuid(), nearby.getUuid())) {
                    nearby.heal(damage / 2.0f);
                }
            }
        }
    }

    public static void onTenHit(ServerPlayerEntity attacker, ServerPlayerEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect regenEffect = InfuseEffect.fromString("regen");
        if (regenEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), regenEffect)) return;

        target.getHungerManager().setFoodLevel(Math.max(0, target.getHungerManager().getFoodLevel() - 2));
    }

    public static void onConsume(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect regenEffect = InfuseEffect.fromString("regen");
        if (regenEffect == null || !plugin.getDataManager().hasEffect(player.getUuid(), regenEffect)) return;

        player.getHungerManager().setSaturationLevel(player.getHungerManager().getSaturationLevel() + 6.0f);
    }
}
