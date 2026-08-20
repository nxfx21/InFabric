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
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class Apophis extends InfuseEffect {
    public static final Identifier APOPHIS_BOOST_ID = Identifier.of(Infuse.MOD_ID, "apophis_boost");
    public static final Identifier APOPHIS_SPARK_BOOST_ID = Identifier.of(Infuse.MOD_ID, "apophis_spark_boost");

    private static final Property APOPHIS_SKIN = new Property(
            "textures",
            "ewogICJ0aW1lc3RhbXAiIDogMTcxNzg4NTA2MDQwNywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVEZXZKYWRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MwOTBmY2NjMjBmMWM3ZWMyMDBkNGVkMDUxMjQwNjM3ZmRmNjE5ZDg1Nzg0NWZhNWRmNWJkMzM1MWJiMjBkOCIKICAgIH0KICB9Cn0=",
            "mBgGwS28lqNz7rJCysD9SElJpA5q+34uTZK68JFXIFzuoN31KQg2VHjVDz+/nAr0yXdRwOrgL5rnRb2NbKBPyKSWdcB8A1nVHeNMpoJ5c5CzEERyOROUiTRxge/MIhYL7Fkj67fkh7Sc/l7BwDAf7/7OIgiAIleUTLZ9COnIN15gylTBldOo3JOka8TTNrI1i4QmnMsbgT0luQZzrUMRtZxIHNwx+26IevzCE+hpNdwiYqnDVZdayDLPVy1vv+i3C7AJGd9b7/2/qv0YmWxvT3uKrPR8+9fbSWltGx9ikrdXO17FrGc5u0gqmPWAaSSWw/NJmMhPenILh7/MvXA8mO2m7JeuhnM/EYzdOMB3qzvkUEVddFIngPl6LNE8XG1R+APFBsbpnpybB7dQphSud5DNfuZijqLDd735kykYlRMzw5VVGf7fONheLzSV42XRsIU+5IazHvmAZ4pxr72+r9bbS9vRW38ZgQIy6p8r4tLv9jfmqmcS9lEn1CAgDLAqZWGzIWeIgOdDsrWH4ia/1gj6oZVefRCr2dAS84NsOQUdoJDbS8G0+ArN+CWgnlcwOJCS6MB5kBmQl2FPvwLcSnnRcS66XKfH28Bu2/J3Hu5zRWbONuOLQTbYFxwftUtvS1IORKBCfWvlJTx5G/mz1KOGW89iOCpW8jdx8EmzpRI="
    );

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
        disguise(owner);
    }

    @Override
    public void unequip(ServerPlayerEntity owner) {
        EntityAttributeInstance attribute = owner.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute != null) {
            attribute.removeModifier(APOPHIS_BOOST_ID);
        }
        removeDisguise(owner);
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

    public static void onTenHit(ServerPlayerEntity attacker, ServerPlayerEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect apophisEffect = InfuseEffect.fromString("apophis");
        if (apophisEffect == null || !plugin.getDataManager().hasEffect(target.getUuid(), apophisEffect)) return;

        long durationMs = (long) (plugin.getMainConfig().apophisLockDurationSeconds() * 1000L);
        long until = System.currentTimeMillis() + durationMs;
        lockedPlayers.put(attacker.getUuid(), until);
        Emerald.lockedPlayers.put(attacker.getUuid(), until);
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

    public void initDisguise(ServerPlayerEntity owner) {
        UUID uuid = owner.getUuid();

        File disguiseFile = new File(plugin.getDataFolder(), "data/ApophisPlayers/" + uuid + ".txt");
        disguiseFile.getParentFile().mkdirs();

        if (disguiseFile.exists()) return;
        
        try {
            FileWriter writer = new FileWriter(disguiseFile);
            GameProfile profile = owner.getGameProfile();
            Optional<Property> textures = profile.getProperties().get("textures").stream().findFirst();

            writer.write(owner.getName().getString() + "\n");
            if (textures.isEmpty()) {
                writer.write("null\nnull");
            } else {
                writer.write(textures.get().value() + "\n");
                writer.write(String.valueOf(textures.get().signature()));
            }

            writer.flush();
            writer.close();
        } catch (IOException err) {
            Infuse.LOGGER.error("Failed to write to {}.", disguiseFile.getPath());
        }
    }

    public void disguise(ServerPlayerEntity owner) {
        initDisguise(owner);

        GameProfile profile = owner.getGameProfile();
        profile.getProperties().removeAll("textures");
        profile.getProperties().put("textures", APOPHIS_SKIN);
        
        owner.setCustomNameVisible(false);
        if (owner.getServer() != null) {
            net.minecraft.scoreboard.ServerScoreboard scoreboard = owner.getServer().getScoreboard();
            if (scoreboard != null) {
                net.minecraft.scoreboard.Team team = scoreboard.getTeam("apophis_hidden");
                if (team == null) {
                    team = scoreboard.addTeam("apophis_hidden");
                    team.setNameTagVisibilityRule(net.minecraft.scoreboard.AbstractTeam.VisibilityRule.NEVER);
                }
                scoreboard.addScoreHolderToTeam(owner.getNameForScoreboard(), team);
            }
        }
    }

    public void removeDisguise(ServerPlayerEntity owner) {
        UUID uuid = owner.getUuid();

        owner.setCustomNameVisible(true);
        if (owner.getServer() != null) {
            net.minecraft.scoreboard.ServerScoreboard scoreboard = owner.getServer().getScoreboard();
            if (scoreboard != null) {
                net.minecraft.scoreboard.Team team = scoreboard.getTeam("apophis_hidden");
                if (team != null) {
                    scoreboard.removeScoreHolderFromTeam(owner.getNameForScoreboard(), team);
                }
            }
        }

        File disguiseFile = new File(plugin.getDataFolder(), "data/ApophisPlayers/" + uuid + ".txt");

        try (Scanner scanner = new Scanner(disguiseFile)) {
            GameProfile profile = owner.getGameProfile();
            String value = "";
            String signature = null;
            if (scanner.hasNextLine()) {
                scanner.nextLine(); // name
            }

            if (scanner.hasNextLine()) {
                value = scanner.nextLine();
            }

            if (scanner.hasNextLine()) {
                signature = scanner.nextLine();
                if (signature.equals("null")) {
                    signature = null;
                }
            }

            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", value, signature));

        } catch (FileNotFoundException err) {}

        if (disguiseFile.exists()) {
            disguiseFile.delete();
        }
    }
}
