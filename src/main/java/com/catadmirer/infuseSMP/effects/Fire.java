package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Fire extends InfuseEffect {
    private final Infuse plugin;

    public Fire() {
        this(false);
    }

    public Fire(boolean augmented) {
        super("fire", EffectIds.FIRE, augmented, EffectConstants.potionColor(EffectIds.FIRE), EffectConstants.ritualColor(EffectIds.FIRE));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {}

    @Override
    public void unequip(ServerPlayerEntity owner) {}

    @Override
    public void applyPassives(ServerPlayerEntity owner) {
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 0, false, false));
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "fire")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "fire", duration, cooldown);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Fire();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Fire(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_FIRE_NAME : MessageType.FIRE_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_FIRE_LORE : MessageType.FIRE_LORE);
    }

    public static void onTenHit(ServerPlayerEntity attacker, ServerPlayerEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect fireEffect = InfuseEffect.fromString("fire");
        if (fireEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), fireEffect)) return;

        target.setOnFireFor(5);
    }
}
