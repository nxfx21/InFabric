package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Strength extends InfuseEffect {
    private final Infuse plugin;

    public Strength() {
        this(false);
    }

    public Strength(boolean augmented) {
        super("strength", EffectIds.STRENGTH, augmented, EffectConstants.potionColor(EffectIds.STRENGTH), EffectConstants.ritualColor(EffectIds.STRENGTH));
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
        if (CooldownManager.isOnCooldown(playerUUID, "strength")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);
        
        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "strength", duration, cooldown);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Strength();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Strength(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_STRENGTH_NAME : MessageType.STRENGTH_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_STRENGTH_LORE : MessageType.STRENGTH_LORE);
    }

    public static float getExtraDamage(ServerPlayerEntity attacker, float damage) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect strengthEffect = InfuseEffect.fromString("strength");
        if (strengthEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), strengthEffect)) return damage;

        float health = attacker.getHealth();
        if (health < 2f) {
            damage += 3f;
        } else if (health < 4f) {
            damage += 2f;
        } else if (health < 6f) {
            damage += 1f;
        }
        return damage;
    }

    public static boolean shouldAutoCrit(ServerPlayerEntity player) {
        return CooldownManager.isEffectActive(player.getUuid(), "strength");
    }

    public static float applySparkAutoCrit(ServerPlayerEntity player, float damage) {
        if (shouldAutoCrit(player)) {
            damage *= 1.35f;
        }
        return damage;
    }
}
