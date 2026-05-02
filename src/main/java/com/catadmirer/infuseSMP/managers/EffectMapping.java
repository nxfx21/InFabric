package com.catadmirer.infuseSMP.managers;

import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum EffectMapping {
    EMERALD  ("emerald",   1, Color.GREEN,         com.catadmirer.infuseSMP.effects.Emerald::applyPassiveEffects,      com.catadmirer.infuseSMP.effects.Emerald::activateSpark),
    ENDER    ("ender",     2, new Color(0x800080), com.catadmirer.infuseSMP.effects.Ender::applyPassiveEffects,        com.catadmirer.infuseSMP.effects.Ender::activateSpark),
    FEATHER  ("feather",   3, new Color(0xBEA3CA), com.catadmirer.infuseSMP.effects.Feather::applyPassiveEffects,      com.catadmirer.infuseSMP.effects.Feather::activateSpark),
    FIRE     ("fire",      4, new Color(0xEE5522), com.catadmirer.infuseSMP.effects.Fire::applyPassiveEffects,         com.catadmirer.infuseSMP.effects.Fire::activateSpark),
    FROST    ("frost",     5, new Color(0x55FFFF), com.catadmirer.infuseSMP.effects.Frost::applyPassiveEffects,        com.catadmirer.infuseSMP.effects.Frost::activateSpark),
    HASTE    ("haste",     6, new Color(0xFFCC33), com.catadmirer.infuseSMP.effects.Haste::applyPassiveEffects,        com.catadmirer.infuseSMP.effects.Haste::activateSpark),
    HEART    ("heart",     7, Color.RED,           com.catadmirer.infuseSMP.effects.Heart::applyPassiveEffects,        com.catadmirer.infuseSMP.effects.Heart::activateSpark),
    INVIS    ("invis",     8, new Color(0xAA00AA), com.catadmirer.infuseSMP.effects.Invisibility::applyPassiveEffects, com.catadmirer.infuseSMP.effects.Invisibility::activateSpark),
    OCEAN    ("ocean",     9, new Color(0x0066FF), com.catadmirer.infuseSMP.effects.Ocean::applyPassiveEffects,        com.catadmirer.infuseSMP.effects.Ocean::activateSpark),
    REGEN    ("regen",    10, new Color(0xFF5555), com.catadmirer.infuseSMP.effects.Regen::applyPassiveEffects,        com.catadmirer.infuseSMP.effects.Regen::activateSpark),
    SPEED    ("speed",    11, new Color(0xEEBB77), com.catadmirer.infuseSMP.effects.Speed::applyPassiveEffects,        com.catadmirer.infuseSMP.effects.Speed::activateSpark),
    STRENGTH ("strength", 12, new Color(0x800000), com.catadmirer.infuseSMP.effects.Strength::applyPassiveEffects,     com.catadmirer.infuseSMP.effects.Strength::activateSpark),
    THUNDER  ("thunder",  13, Color.YELLOW,        com.catadmirer.infuseSMP.effects.Thunder::applyPassiveEffects,      com.catadmirer.infuseSMP.effects.Thunder::activateSpark),
    APOPHIS  ("apophis",  14, new Color(0x440044), com.catadmirer.infuseSMP.effects.Apophis::applyPassiveEffects,      com.catadmirer.infuseSMP.effects.Apophis::activateSpark),
    THIEF    ("thief",    15, new Color(0xAA0000), com.catadmirer.infuseSMP.effects.Thief::applyPassiveEffects,        com.catadmirer.infuseSMP.effects.Thief::activateSpark),

    AUG_EMERALD(EMERALD),
    AUG_ENDER(ENDER),
    AUG_FEATHER(FEATHER),
    AUG_FIRE(FIRE),
    AUG_FROST(FROST),
    AUG_HASTE(HASTE),
    AUG_HEART(HEART),
    AUG_INVIS(INVIS),
    AUG_OCEAN(OCEAN),
    AUG_REGEN(REGEN),
    AUG_SPEED(SPEED),
    AUG_STRENGTH(STRENGTH),
    AUG_THUNDER(THUNDER),
    AUG_APOPHIS(APOPHIS),
    AUG_THIEF(THIEF);

    public static final Identifier AUG_MODEL = Identifier.of("infuse", "aug");

    private final String key;
    private final int id;
    private final Color color;
    private final String icon;
    private final String activeIcon;
    private final Consumer<ServerPlayerEntity> passiveFunction;
    private final BiConsumer<Boolean,ServerPlayerEntity> sparkFunction;

    private EffectMapping regular;
    private EffectMapping augmented;

    private EffectMapping(String key, int id, Color color, Consumer<ServerPlayerEntity> passiveFunction, BiConsumer<Boolean,ServerPlayerEntity> sparkFunction) {
        this(key, id, color, "\ue901", "\ue902", passiveFunction, sparkFunction);
    }

    private EffectMapping(String key, int id, Color color, String icon, String activeIcon, Consumer<ServerPlayerEntity> passiveFunction, BiConsumer<Boolean,ServerPlayerEntity> sparkFunction) {
        this.key = key;
        this.id = id;
        this.color = color;
        this.icon = icon;
        this.activeIcon = activeIcon;
        this.passiveFunction = passiveFunction;
        this.sparkFunction = sparkFunction;

        regular = this;
    }

    private EffectMapping(EffectMapping base) {
        this.key = "aug_" + base.key;
        this.id = base.id;
        this.color = base.color;
        this.icon = base.icon;
        this.activeIcon = base.activeIcon;
        this.passiveFunction = base.passiveFunction;
        this.sparkFunction = base.sparkFunction;

        regular = base;
        augmented = this;
        base.augmented = this;
    }

    public String getKey() {
        return key;
    }

    public int getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }

    public String getIcon() {
        return icon;
    }

    public String getActiveIcon() {
        return activeIcon;
    }

    @NotNull
    public Message getName() {
        return switch (this) {
            case EMERALD -> new Message(MessageType.EMERALD_NAME);
            case ENDER -> new Message(MessageType.ENDER_NAME);
            case FEATHER -> new Message(MessageType.FEATHER_NAME);
            case FIRE -> new Message(MessageType.FIRE_NAME);
            case FROST -> new Message(MessageType.FROST_NAME);
            case HASTE -> new Message(MessageType.HASTE_NAME);
            case HEART -> new Message(MessageType.HEART_NAME);
            case INVIS -> new Message(MessageType.INVIS_NAME);
            case OCEAN -> new Message(MessageType.OCEAN_NAME);
            case REGEN -> new Message(MessageType.REGEN_NAME);
            case SPEED -> new Message(MessageType.SPEED_NAME);
            case STRENGTH -> new Message(MessageType.STRENGTH_NAME);
            case THUNDER -> new Message(MessageType.THUNDER_NAME);
            case APOPHIS -> new Message(MessageType.APOPHIS_NAME);
            case THIEF -> new Message(MessageType.THIEF_NAME);
            case AUG_EMERALD -> new Message(MessageType.AUG_EMERALD_NAME);
            case AUG_ENDER -> new Message(MessageType.AUG_ENDER_NAME);
            case AUG_FEATHER -> new Message(MessageType.AUG_FEATHER_NAME);
            case AUG_FIRE -> new Message(MessageType.AUG_FIRE_NAME);
            case AUG_FROST -> new Message(MessageType.AUG_FROST_NAME);
            case AUG_HASTE -> new Message(MessageType.AUG_HASTE_NAME);
            case AUG_HEART -> new Message(MessageType.AUG_HEART_NAME);
            case AUG_INVIS -> new Message(MessageType.AUG_INVIS_NAME);
            case AUG_OCEAN -> new Message(MessageType.AUG_OCEAN_NAME);
            case AUG_REGEN -> new Message(MessageType.AUG_REGEN_NAME);
            case AUG_SPEED -> new Message(MessageType.AUG_SPEED_NAME);
            case AUG_STRENGTH -> new Message(MessageType.AUG_STRENGTH_NAME);
            case AUG_THUNDER -> new Message(MessageType.AUG_THUNDER_NAME);
            case AUG_APOPHIS -> new Message(MessageType.AUG_APOPHIS_NAME);
            case AUG_THIEF -> new Message(MessageType.AUG_THIEF_NAME);
        };
    }

    public Message getLore() {
        return switch (this) {
            case EMERALD -> new Message(MessageType.EMERALD_LORE);
            case ENDER -> new Message(MessageType.ENDER_LORE);
            case FEATHER -> new Message(MessageType.FEATHER_LORE);
            case FIRE -> new Message(MessageType.FIRE_LORE);
            case FROST -> new Message(MessageType.FROST_LORE);
            case HASTE -> new Message(MessageType.HASTE_LORE);
            case HEART -> new Message(MessageType.HEART_LORE);
            case INVIS -> new Message(MessageType.INVIS_LORE);
            case OCEAN -> new Message(MessageType.OCEAN_LORE);
            case REGEN -> new Message(MessageType.REGEN_LORE);
            case SPEED -> new Message(MessageType.SPEED_LORE);
            case STRENGTH -> new Message(MessageType.STRENGTH_LORE);
            case THUNDER -> new Message(MessageType.THUNDER_LORE);
            case APOPHIS -> new Message(MessageType.APOPHIS_LORE);
            case THIEF -> new Message(MessageType.THIEF_LORE);
            case AUG_EMERALD -> new Message(MessageType.AUG_EMERALD_LORE);
            case AUG_ENDER -> new Message(MessageType.AUG_ENDER_LORE);
            case AUG_FEATHER -> new Message(MessageType.AUG_FEATHER_LORE);
            case AUG_FIRE -> new Message(MessageType.AUG_FIRE_LORE);
            case AUG_FROST -> new Message(MessageType.AUG_FROST_LORE);
            case AUG_HASTE -> new Message(MessageType.AUG_HASTE_LORE);
            case AUG_HEART -> new Message(MessageType.AUG_HEART_LORE);
            case AUG_INVIS -> new Message(MessageType.AUG_INVIS_LORE);
            case AUG_OCEAN -> new Message(MessageType.AUG_OCEAN_LORE);
            case AUG_REGEN -> new Message(MessageType.AUG_REGEN_LORE);
            case AUG_SPEED -> new Message(MessageType.AUG_SPEED_LORE);
            case AUG_STRENGTH -> new Message(MessageType.AUG_STRENGTH_LORE);
            case AUG_THUNDER -> new Message(MessageType.AUG_THUNDER_LORE);
            case AUG_APOPHIS -> new Message(MessageType.AUG_APOPHIS_LORE);
            case AUG_THIEF -> new Message(MessageType.AUG_THIEF_LORE);
        };
    }

    public EffectMapping regular() { return regular; }
    public EffectMapping augmented() { return augmented; }
    public boolean isAugmented() { return this == augmented; }

    @NotNull
    public ItemStack createItem() {
        ItemStack item = new ItemStack(Items.POTION);
        item.set(DataComponentTypes.ITEM_NAME, getName().toComponent());
        item.set(DataComponentTypes.LORE, new LoreComponent(getLore().toComponentList()));

        NbtCompound nbt = new NbtCompound();
        nbt.putString("infuse:effect_key", key);
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        return item;
    }

    public void applyPassiveEffects(ServerPlayerEntity player) {
        passiveFunction.accept(player);
    }

    public void activateSpark(ServerPlayerEntity player) {
        sparkFunction.accept(isAugmented(), player);
    }

    public boolean isEffect(@Nullable ItemStack item) {
        if (item == null || item.isEmpty() || !item.isOf(Items.POTION)) return false;
        NbtComponent data = item.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return false;
        NbtCompound nbt = data.copyNbt();
        return key.equals(nbt.getString("infuse:effect_key"));
    }

    @Nullable
    public static EffectMapping fromItem(@Nullable ItemStack item) {
        if (item == null || item.isEmpty() || !item.isOf(Items.POTION)) return null;
        NbtComponent data = item.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) return null;
        NbtCompound nbt = data.copyNbt();
        String key = nbt.getString("infuse:effect_key");
        if (key == null || key.isEmpty()) return null;
        return fromEffectKey(key);
    }

    @Nullable
    public static EffectMapping fromEffectKey(@Nullable String key) {
        for (EffectMapping mapping : values()) {
            if (mapping.getKey().equalsIgnoreCase(key)) return mapping;
        }
        return null;
    }
}
