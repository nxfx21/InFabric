package com.nxfx21.infabric.effects;

import com.nxfx21.infabric.EffectConstants;
import com.nxfx21.infabric.EffectIds;
import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.managers.CooldownManager;
import net.minecraft.entity.LivingEntity;
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

public class Heart extends InfuseEffect {
    public static final Identifier HEART_BOOST_ID = Identifier.of(Infuse.MOD_ID, "heart_boost");
    public static final Identifier HEART_SPARK_BOOST_ID = Identifier.of(Infuse.MOD_ID, "heart_spark_boost");

    private final Infuse plugin;

    public Heart() {
        this(false);
    }

    public Heart(boolean augmented) {
        super("heart", EffectIds.HEART, augmented, EffectConstants.potionColor(EffectIds.HEART), EffectConstants.ritualColor(EffectIds.HEART));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {
        EntityAttributeInstance attribute = owner.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null && attribute.getModifier(HEART_BOOST_ID) == null) {
            attribute.addTemporaryModifier(new EntityAttributeModifier(HEART_BOOST_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE));
            owner.heal(10);
        }
    }

    @Override
    public void unequip(ServerPlayerEntity owner) {
        EntityAttributeInstance attribute = owner.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.removeModifier(HEART_BOOST_ID);
        }
    }

    @Override
    public void applyPassives(ServerPlayerEntity owner) {
        EntityAttributeInstance attribute = owner.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null && attribute.getModifier(HEART_BOOST_ID) == null) {
            attribute.addTemporaryModifier(new EntityAttributeModifier(HEART_BOOST_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE));
            owner.heal(10);
        }
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "heart")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        EntityAttributeInstance attribute = owner.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null && attribute.getModifier(HEART_SPARK_BOOST_ID) == null) {
            attribute.addTemporaryModifier(new EntityAttributeModifier(HEART_SPARK_BOOST_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE));
            owner.heal(10);
        }

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "heart", duration, cooldown);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Heart();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Heart(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_HEART_NAME : MessageType.HEART_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_HEART_LORE : MessageType.HEART_LORE);
    }

    public static void onTenHit(ServerPlayerEntity attacker, ServerPlayerEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect heartEffect = InfuseEffect.fromString("heart");
        if (heartEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), heartEffect)) return;

        showHealthAboveEntity(target);
    }

    private static final java.util.concurrent.ScheduledExecutorService DISPLAY_SCHEDULER =
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Infuse-HeartDisplayCleaner");
            t.setDaemon(true);
            return t;
        });

    private static void showHealthAboveEntity(LivingEntity entity) {
        if (entity == null || entity.getWorld().isClient()) return;
        net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity display = net.minecraft.entity.EntityType.TEXT_DISPLAY.create(entity.getWorld(), net.minecraft.entity.SpawnReason.COMMAND);
        if (display == null) return;
        
        display.setPos(entity.getX(), entity.getY() + 2.5, entity.getZ());
        display.setInvisible(false);
        display.setText(net.minecraft.text.Text.literal(String.format("§c❤ %.1f", entity.getHealth())));
        
        entity.getWorld().spawnEntity(display);
        display.startRiding(entity);
        
        DISPLAY_SCHEDULER.schedule(() -> {
            if (entity.getWorld() != null && entity.getWorld().getServer() != null) {
                entity.getWorld().getServer().execute(display::discard);
            }
        }, 2500, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public static void onConsume(ServerPlayerEntity player, net.minecraft.item.ItemStack stack) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect heartEffect = InfuseEffect.fromString("heart");
        if (heartEffect == null || !plugin.getDataManager().hasEffect(player.getUuid(), heartEffect)) return;
        
        int duration = 600;
        int amplifier = 0;
        if (stack.isOf(net.minecraft.item.Items.ENCHANTED_GOLDEN_APPLE)) {
            duration = 2400;
            amplifier = 4;
        }
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, duration, amplifier));
    }
}
