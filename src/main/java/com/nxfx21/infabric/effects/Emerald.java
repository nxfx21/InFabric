package com.nxfx21.infabric.effects;

import com.nxfx21.infabric.EffectConstants;
import com.nxfx21.infabric.EffectIds;
import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.managers.CooldownManager;
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
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 40, 0));
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 40, 9, false, false));
        
        net.minecraft.item.ItemStack stack = owner.getMainHandStack();
        if (com.nxfx21.infabric.util.ItemUtil.isSword(stack)) {
            owner.getWorld().getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .flatMap(r -> r.getOptional(net.minecraft.enchantment.Enchantments.LOOTING))
                .ifPresent(entry -> {
                    com.nxfx21.infabric.util.ItemUtil.applySpecialEnchantment(stack, "infuse:emerald_looting", entry, plugin.getMainConfig().emeraldLootingLevel());
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
        if (uuid == null) return false;
        Long until = lockedPlayers.get(uuid);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            lockedPlayers.remove(uuid);
            return false;
        }
        return true;
    }

    public static void onAttack(ServerPlayerEntity attacker, net.minecraft.entity.LivingEntity target) {
        if (attacker == null || target == null) return;
        Infuse plugin = Infuse.getInstance();
        if (plugin == null || plugin.getDataManager() == null || plugin.getMainConfig() == null) return;
        InfuseEffect emeraldEffect = InfuseEffect.fromString("emerald");
        if (emeraldEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), emeraldEffect)) return;

        if (target instanceof ServerPlayerEntity damaged) {
            int expPerHit = plugin.getMainConfig().emeraldExpPerHit();
            if (expPerHit <= 0) return;

            int currentXp = Math.max(0, damaged.totalExperience);
            int actualStolen = Math.min(expPerHit, currentXp);
            if (actualStolen > 0) {
                damaged.addExperience(-actualStolen);
                if (damaged.totalExperience < 0) {
                    damaged.totalExperience = 0;
                }
                float percent = plugin.getMainConfig().emeraldExpPercent();
                int toGain = (int) (actualStolen * percent);
                if (toGain > 0) {
                    attacker.addExperience(toGain);
                }
            }
        }
    }

    public static void onConsume(ServerPlayerEntity player, net.minecraft.item.ItemStack stack) {
        if (player == null || stack == null) return;
        Infuse plugin = Infuse.getInstance();
        if (plugin == null || plugin.getMainConfig() == null || !plugin.getMainConfig().emeraldPreserveConsumables()) return;
        if (plugin.getDataManager() == null) return;
        InfuseEffect emeraldEffect = InfuseEffect.fromString("emerald");
        if (emeraldEffect == null || !plugin.getDataManager().hasEffect(player.getUuid(), emeraldEffect)) return;

        if (stack.isOf(net.minecraft.item.Items.POTION)) return;

        double chance = 0.5;
        if (CooldownManager.isEffectActive(player.getUuid(), "emerald")) chance = 0.75;

        if (Math.random() <= chance) {
            stack.increment(1);
            if (player.getWorld() != null) {
                player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
                if (player.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                    serverWorld.spawnParticles(net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1, player.getZ(), 3, 0.5, 0.5, 0.5, 0.01);
                }
            }
        }
    }

    public static void applyEnchantmentBonus(ServerPlayerEntity player, int[] enchantmentPower, int[] enchantmentLevel) {
        if (player == null || enchantmentPower == null || enchantmentLevel == null) return;
        Infuse plugin = Infuse.getInstance();
        if (plugin == null || plugin.getMainConfig() == null || !plugin.getMainConfig().emeraldEnchantBonus()) return;
        if (plugin.getDataManager() == null) return;
        InfuseEffect emeraldEffect = InfuseEffect.fromString("emerald");
        if (emeraldEffect == null || !plugin.getDataManager().hasEffect(player.getUuid(), emeraldEffect)) return;

        for (int i = 0; i < 3; i++) {
            if (i < enchantmentPower.length && enchantmentPower[i] > 0) {
                enchantmentPower[i] = Math.min(30, enchantmentPower[i] + 5);
            }
            if (i < enchantmentLevel.length && enchantmentLevel[i] > 0) {
                enchantmentLevel[i] = enchantmentLevel[i] + 1;
            }
        }
    }

    public static void onTenHit(ServerPlayerEntity attacker, ServerPlayerEntity target) {
        if (attacker == null || target == null) return;
        Infuse plugin = Infuse.getInstance();
        if (plugin == null || plugin.getDataManager() == null || plugin.getMainConfig() == null) return;
        InfuseEffect emeraldEffect = InfuseEffect.fromString("emerald");
        if (emeraldEffect == null || !plugin.getDataManager().hasEffect(target.getUuid(), emeraldEffect)) return;

        long durationMs = (long) (plugin.getMainConfig().emeraldLockDurationSeconds() * 1000L);
        lockedPlayers.put(attacker.getUuid(), System.currentTimeMillis() + durationMs);
        attacker.sendMessage(net.minecraft.text.Text.literal("Your food and EXP have been locked!"), true);
    }

    public static void cleanupInventory(net.minecraft.inventory.Inventory inventory, ServerPlayerEntity player) {
        if (inventory == null || player == null) return;
        Infuse plugin = Infuse.getInstance();
        if (plugin == null || plugin.getDataManager() == null) return;
        InfuseEffect emeraldEffect = InfuseEffect.fromString("emerald");
        if (emeraldEffect == null || !plugin.getDataManager().hasEffect(player.getUuid(), emeraldEffect)) return;

        for (int i = 0; i < inventory.size(); i++) {
            net.minecraft.item.ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            player.getWorld().getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .flatMap(r -> r.getOptional(net.minecraft.enchantment.Enchantments.LOOTING))
                .ifPresent(entry -> {
                    com.nxfx21.infabric.util.ItemUtil.removeSpecialEnchant(stack, "infuse:emerald_looting", entry);
                });
        }
    }
}
