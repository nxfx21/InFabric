package com.nxfx21.infabric.effects;

import com.nxfx21.infabric.EffectConstants;
import com.nxfx21.infabric.EffectIds;
import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.managers.CooldownManager;
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
        return player != null && shouldAutoCrit(player.getUuid());
    }

    public static boolean shouldAutoCrit(UUID uuid) {
        return CooldownManager.isEffectActive(uuid, "strength");
    }

    public static float applySparkAutoCrit(ServerPlayerEntity player, float damage) {
        return player != null ? applySparkAutoCrit(player.getUuid(), damage) : damage;
    }

    public static float applySparkAutoCrit(UUID uuid, float damage) {
        if (shouldAutoCrit(uuid)) {
            damage *= 1.35f;
        }
        return damage;
    }

    public static float modifyAttackDamage(ServerPlayerEntity attacker, net.minecraft.entity.LivingEntity target, float damage) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect strengthEffect = InfuseEffect.fromString("strength");
        if (strengthEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), strengthEffect)) return damage;

        // Health-based damage boost: scales with how much HP the attacker is missing
        float maxHealth = (float) attacker.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.MAX_HEALTH);
        damage += (maxHealth - attacker.getHealth()) * 0.3f;

        // Double damage against non-player entities
        if (!(target instanceof ServerPlayerEntity)) {
            damage *= 2.0f;
        }

        // Shield stun when attacker uses an axe against a blocking player
        if (target instanceof ServerPlayerEntity playerTarget && playerTarget.isBlocking()) {
            if (com.nxfx21.infabric.util.ItemUtil.isAxe(attacker.getMainHandStack())) {
                playerTarget.getWorld().playSound(null, playerTarget.getX(), playerTarget.getY(), playerTarget.getZ(),
                    net.minecraft.sound.SoundEvents.ITEM_SHIELD_BREAK, net.minecraft.sound.SoundCategory.PLAYERS, 1, 1);
                playerTarget.getItemCooldownManager().set(net.minecraft.item.Items.SHIELD.getDefaultStack(), 200);
                // Halve the damage if the hit connected on a blocking player
                damage /= 2.0f;
            }
        }

        return damage;
    }

    /** Called from the bow/arrow mixin — caps pierce level at 1. */
    public static int getArrowPierceLevel(ServerPlayerEntity attacker, int currentLevel) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect strengthEffect = InfuseEffect.fromString("strength");
        if (strengthEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), strengthEffect)) return currentLevel;
        return Math.max(currentLevel, 1);
    }
}
