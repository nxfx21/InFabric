package com.nxfx21.infabric.effects;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

public abstract class InfuseEffect {
    private static final Map<Integer, InfuseEffect> REGISTERED_EFFECTS = new HashMap<>();

    public static final Identifier EFFECT_KEY = Identifier.of("infuse", "effect_key");
    public static final Identifier AUG_MODEL = Identifier.of("infuse", "aug");

    protected final String key;
    protected final int id;
    protected final boolean augmented;
    protected final Color potionColor;
    protected final BossBar.Color ritualColor;

    public InfuseEffect(String key, int id, boolean augmented, Color potionColor, BossBar.Color ritualColor) {
        this.key = key;
        this.id = id;
        this.augmented = augmented;
        this.potionColor = potionColor;
        this.ritualColor = ritualColor;
    }

    public static boolean isRegistered(InfuseEffect effect) {
        return REGISTERED_EFFECTS.containsKey(effect.id);
    }

    public static boolean register(InfuseEffect effect) {
        if (effect.id > 100) {
            Infuse.LOGGER.warn("Effect id {} for {} is invalid. Effect ids cannot be > 100.", effect.id, effect.key);
            return false;
        }

        InfuseEffect existing = REGISTERED_EFFECTS.get(effect.id);
        if (existing != null) {
            Infuse.LOGGER.warn("Effect id {} has already been taken by {}. Cannot assign it to {}.", effect.id, existing.key, effect.key);
            return false;
        }

        REGISTERED_EFFECTS.put(effect.id, effect);
        return true;
    }

    @NotNull
    public static Map<Integer, InfuseEffect> getRegisteredEffects() {
        return Map.copyOf(REGISTERED_EFFECTS);
    }

    @NotNull
    public static java.util.Collection<InfuseEffect> getAllEffects() {
        return REGISTERED_EFFECTS.values();
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public boolean isAugmented() {
        return augmented;
    }

    public Color getPotionColor() {
        return potionColor;
    }

    public BossBar.Color getRitualColor() {
        return ritualColor;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof InfuseEffect effect)) return false;
        return effect.augmented == this.augmented && effect.id == this.id;
    }

    @Override
    public String toString() {
        return (augmented ? "aug_" : "") + key;
    }

    public abstract void equip(ServerPlayerEntity owner);
    public abstract void unequip(ServerPlayerEntity owner);

    public void applyPassives(ServerPlayerEntity owner) {}
    public abstract void activateSpark(ServerPlayerEntity owner);

    public abstract InfuseEffect getRegularVersion();
    public abstract InfuseEffect getAugmentedVersion();

    public abstract Message getName();
    public abstract Message getLore();

    public char getIcon() {
        return (char) Integer.parseInt("E" + (augmented ? 2 : 0) + String.format("%02d", id), 16);
    }

    public char getActiveIcon() {
        return (char) Integer.parseInt("E" + (augmented ? 3 : 1) + String.format("%02d", id), 16);
    }

    public static InfuseEffect fromString(@Nullable String key) {
        if (key == null) return null;

        boolean augmented = key.startsWith("aug_");
        if (augmented) {
            key = key.substring(4);
        }

        for (InfuseEffect effect : REGISTERED_EFFECTS.values()) {
            if (!effect.getKey().equals(key)) continue;
            return augmented ? effect.getAugmentedVersion() : effect.getRegularVersion();
        }

        Infuse.LOGGER.warn("No effect found for string '{}'.", key);
        return null;
    }

    public ItemStack createItem() {
        ItemStack item = new ItemStack(Items.POTION);
        item.set(DataComponentTypes.ITEM_NAME, getName().toComponent());
        item.set(DataComponentTypes.LORE, new LoreComponent(getLore().toComponentList()));

        PotionContentsComponent contents = new PotionContentsComponent(
            Optional.empty(),
            Optional.of(potionColor.getRGB()),
            List.of(),
            Optional.empty()
        );
        item.set(DataComponentTypes.POTION_CONTENTS, contents);

        NbtCompound nbt = new NbtCompound();
        nbt.putString("infuse:effect_key", toString());
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        if (augmented) {
            item.set(DataComponentTypes.ITEM_MODEL, AUG_MODEL);
        }

        return item;
    }

    public boolean itemMatches(@Nullable ItemStack item) {
        if (item == null || item.isEmpty() || !item.isOf(Items.POTION)) return false;
        NbtComponent data = item.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return false;
        NbtCompound nbt = data.copyNbt();
        return toString().equals(nbt.getString("infuse:effect_key"));
    }

    public static InfuseEffect fromItem(ItemStack item) {
        if (item == null || item.isEmpty() || !item.isOf(Items.POTION)) return null;
        NbtComponent data = item.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return null;
        NbtCompound nbt = data.copyNbt();
        String key = nbt.getString("infuse:effect_key");
        if (key == null) return null;
        return fromString(key);
    }

    public int serialize() {
        return (augmented ? 100 : 0) + id;
    }

    public static InfuseEffect deserialize(int serialized) {
        if (!REGISTERED_EFFECTS.containsKey(serialized % 100)) {
            Infuse.LOGGER.warn("Could not find an effect registered to id {}", serialized % 100);
            return null;
        }

        boolean augmented = serialized > 99;
        int id = serialized % 100;
        InfuseEffect effect = REGISTERED_EFFECTS.get(id);

        return augmented ? effect.getAugmentedVersion() : effect.getRegularVersion();
    }
}
