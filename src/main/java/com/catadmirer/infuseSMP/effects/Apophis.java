package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import java.util.UUID;

public class Apophis {
    public static final Identifier APOPHIS_BOOST_ID = Identifier.of(Infuse.MOD_ID, "apophis_boost");
    public static final Identifier APOPHIS_SPARK_BOOST_ID = Identifier.of(Infuse.MOD_ID, "apophis_spark_boost");

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.APOPHIS)) return;

        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null && attribute.getModifier(APOPHIS_BOOST_ID) == null) {
            attribute.addTemporaryModifier(new EntityAttributeModifier(APOPHIS_BOOST_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE));
            player.heal(10);
        }

        player.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 40, 9, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 40, 2, false, false));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 2, false, false));
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "apophis")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null && attribute.getModifier(APOPHIS_SPARK_BOOST_ID) == null) {
            attribute.addTemporaryModifier(new EntityAttributeModifier(APOPHIS_SPARK_BOOST_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE));
            player.heal(10);
        }

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_APOPHIS : EffectMapping.APOPHIS);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_APOPHIS : EffectMapping.APOPHIS);

        CooldownManager.setTimes(playerUUID, "apophis", duration, cooldown);
    }
}
