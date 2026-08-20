package com.nxfx21.infabric;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerDeathListener {
    public static void register() {
        ServerLivingEntityEvents.AFTER_DEATH.register(PlayerDeathListener::onDeath);
    }

    private static void onDeath(LivingEntity entity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayerEntity player)) return;

        Infuse plugin = Infuse.getInstance();
        if (plugin.getMainConfig().playerHeadDrops()) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.set(DataComponentTypes.PROFILE, new ProfileComponent(player.getGameProfile()));
            player.getWorld().spawnEntity(new net.minecraft.entity.ItemEntity(player.getWorld(), player.getX(), player.getY(), player.getZ(), head));
        }

        plugin.getEffectManager().handleDeath(player);

        if (damageSource.getAttacker() instanceof ServerPlayerEntity killer) {
            com.nxfx21.infabric.effects.InfuseEffect thiefEffect = com.nxfx21.infabric.effects.InfuseEffect.fromString("thief");
            if (thiefEffect != null && plugin.getDataManager().hasEffect(killer.getUuid(), thiefEffect)) {
                com.nxfx21.infabric.effects.Thief.disguiseAs(killer, player);
            }
        }
    }
}
