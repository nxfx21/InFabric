package com.nxfx21.infabric.effects;

import com.nxfx21.infabric.EffectConstants;
import com.nxfx21.infabric.EffectIds;
import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.managers.CooldownManager;
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

public class Apophis extends InfuseEffect {
    public static final Identifier APOPHIS_BOOST_ID = Identifier.of(Infuse.MOD_ID, "apophis_boost");
    public static final Identifier APOPHIS_SPARK_BOOST_ID = Identifier.of(Infuse.MOD_ID, "apophis_spark_boost");

    private final Infuse plugin;

    public Apophis() {
        this(false);
    }

    public Apophis(boolean augmented) {
        super("apophis", EffectIds.APOPHIS, augmented, EffectConstants.potionColor(EffectIds.APOPHIS), EffectConstants.ritualColor(EffectIds.APOPHIS));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {
        EntityAttributeInstance attribute = owner.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null && attribute.getModifier(APOPHIS_BOOST_ID) == null) {
            attribute.addTemporaryModifier(new EntityAttributeModifier(APOPHIS_BOOST_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE));
            owner.heal(10);
        }
    }

    @Override
    public void unequip(ServerPlayerEntity owner) {
        EntityAttributeInstance attribute = owner.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.removeModifier(APOPHIS_BOOST_ID);
        }
    }

    @Override
    public void applyPassives(ServerPlayerEntity owner) {
        EntityAttributeInstance attribute = owner.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null && attribute.getModifier(APOPHIS_BOOST_ID) == null) {
            attribute.addTemporaryModifier(new EntityAttributeModifier(APOPHIS_BOOST_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE));
            owner.heal(10);
        }

        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.LUCK, 40, 9, false, false));
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 40, 2, false, false));
        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 40, 2, false, false));

        net.minecraft.item.ItemStack stack = owner.getMainHandStack();
        if (com.nxfx21.infabric.util.ItemUtil.isSword(stack)) {
            owner.getWorld().getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .flatMap(r -> r.getOptional(net.minecraft.enchantment.Enchantments.LOOTING))
                .ifPresent(entry -> {
                    com.nxfx21.infabric.util.ItemUtil.applySpecialEnchantment(stack, "infuse:apophis_looting", entry, plugin.getMainConfig().apophisLootingLevel());
                });
        }
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "apophis")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        EntityAttributeInstance attribute = owner.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null && attribute.getModifier(APOPHIS_SPARK_BOOST_ID) == null) {
            attribute.addTemporaryModifier(new EntityAttributeModifier(APOPHIS_SPARK_BOOST_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE));
            owner.heal(10);
        }

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "apophis", duration, cooldown);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Apophis();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Apophis(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_APOPHIS_NAME : MessageType.APOPHIS_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_APOPHIS_LORE : MessageType.APOPHIS_LORE);
    }

    public static void onTenHit(ServerPlayerEntity attacker, ServerPlayerEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect apophisEffect = InfuseEffect.fromString("apophis");
        if (apophisEffect == null || !plugin.getDataManager().hasEffect(target.getUuid(), apophisEffect)) return;

        long durationMs = (long) (plugin.getMainConfig().apophisLockDurationSeconds() * 1000L);
        Emerald.lockedPlayers.put(attacker.getUuid(), System.currentTimeMillis() + durationMs);
        attacker.sendMessage(net.minecraft.text.Text.literal("Your food and EXP have been locked!"), true);
    }

    public static void cleanupInventory(net.minecraft.inventory.Inventory inventory, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect apophisEffect = InfuseEffect.fromString("apophis");
        if (apophisEffect == null || !plugin.getDataManager().hasEffect(player.getUuid(), apophisEffect)) return;

        for (int i = 0; i < inventory.size(); i++) {
            net.minecraft.item.ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            player.getWorld().getRegistryManager().getOptional(net.minecraft.registry.RegistryKeys.ENCHANTMENT)
                .flatMap(r -> r.getOptional(net.minecraft.enchantment.Enchantments.LOOTING))
                .ifPresent(entry -> {
                    com.nxfx21.infabric.util.ItemUtil.removeSpecialEnchant(stack, "infuse:apophis_looting", entry);
                });
        }
    }
}
