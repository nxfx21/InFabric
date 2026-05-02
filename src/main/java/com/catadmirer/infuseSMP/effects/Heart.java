package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import java.util.UUID;

public class Heart {
    public static final Identifier HEART_BOOST_ID = Identifier.of(Infuse.MOD_ID, "heart_boost");
    public static final Identifier HEART_SPARK_BOOST_ID = Identifier.of(Infuse.MOD_ID, "heart_spark_boost");

    public static void applyPassiveEffects(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        if (!plugin.getDataManager().hasEffect(player, EffectMapping.HEART)) return;

        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null && attribute.getModifier(HEART_BOOST_ID) == null) {
            attribute.addTemporaryModifier(new EntityAttributeModifier(HEART_BOOST_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE));
            player.heal(10);
        }
    }

    public static void activateSpark(boolean isAugmented, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        UUID playerUUID = player.getUuid();

        if (CooldownManager.isOnCooldown(playerUUID, "heart")) return;

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null && attribute.getModifier(HEART_SPARK_BOOST_ID) == null) {
            attribute.addTemporaryModifier(new EntityAttributeModifier(HEART_SPARK_BOOST_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE));
            player.heal(10);
        }

        long cooldown = plugin.getMainConfig().cooldown(isAugmented ? EffectMapping.AUG_HEART : EffectMapping.HEART);
        long duration = plugin.getMainConfig().duration(isAugmented ? EffectMapping.AUG_HEART : EffectMapping.HEART);

        CooldownManager.setTimes(playerUUID, "heart", duration, cooldown);
        
        // TODO: Schedule modifier removal
    }
}
