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

public class Emerald extends InfuseEffect {
    private final Infuse plugin;

    public Emerald() {
        this(false);
    }

    public Emerald(boolean augmented) {
        super("emerald", EffectIds.EMERALD, augmented, EffectConstants.potionColor(EffectIds.EMERALD), EffectConstants.ritualColor(EffectIds.EMERALD));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {}

    @Override
    public void unequip(ServerPlayerEntity owner) {}

    @Override
    public void applyPassives(ServerPlayerEntity owner) {
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 40, 2, false, false));
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 40, 9, false, false));
        
        net.minecraft.item.ItemStack stack = owner.getMainHandStack();
        if (com.catadmirer.infuseSMP.util.ItemUtil.isSword(stack)) {
            owner.getWorld().getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .flatMap(r -> r.getOptional(net.minecraft.enchantment.Enchantments.LOOTING))
                .ifPresent(entry -> {
                    com.catadmirer.infuseSMP.util.ItemUtil.applySpecialEnchantment(stack, "infuse:emerald_looting", entry, plugin.getMainConfig().emeraldLootingLevel());
                });
        }
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "emerald")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, (int) duration * 20, 4));
        CooldownManager.setTimes(playerUUID, "emerald", duration, cooldown);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Emerald();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Emerald(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_EMERALD_NAME : MessageType.EMERALD_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_EMERALD_LORE : MessageType.EMERALD_LORE);
    }

    public static final java.util.Map<UUID, Long> lockedPlayers = new java.util.concurrent.ConcurrentHashMap<>();

    public static boolean isLocked(UUID uuid) {
        Long until = lockedPlayers.get(uuid);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            lockedPlayers.remove(uuid);
            return false;
        }
        return true;
    }

    public static void onTenHit(ServerPlayerEntity attacker, ServerPlayerEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect emeraldEffect = InfuseEffect.fromString("emerald");
        if (emeraldEffect == null || !plugin.getDataManager().hasEffect(target.getUuid(), emeraldEffect)) return;

        long durationMs = (long) (plugin.getMainConfig().emeraldLockDurationSeconds() * 1000L);
        lockedPlayers.put(attacker.getUuid(), System.currentTimeMillis() + durationMs);
        attacker.sendMessage(net.minecraft.text.Text.literal("Your food and EXP have been locked!"), true);
    }

    public static void cleanupInventory(net.minecraft.inventory.Inventory inventory, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect emeraldEffect = InfuseEffect.fromString("emerald");
        if (emeraldEffect == null || !plugin.getDataManager().hasEffect(player.getUuid(), emeraldEffect)) return;

        for (int i = 0; i < inventory.size(); i++) {
            net.minecraft.item.ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            player.getWorld().getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .flatMap(r -> r.getOptional(net.minecraft.enchantment.Enchantments.LOOTING))
                .ifPresent(entry -> {
                    com.catadmirer.infuseSMP.util.ItemUtil.removeSpecialEnchant(stack, "infuse:emerald_looting", entry);
                });
        }
    }
}
