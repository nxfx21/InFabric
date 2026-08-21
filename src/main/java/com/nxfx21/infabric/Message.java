package com.nxfx21.infabric;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Message {
    private static final MessageTranslator TRANSLATOR = new MessageTranslator();

    private String message;
    private final List<String> placeholders;

    public Message(MessageType messageType) {
        String translated = TRANSLATOR.translate(messageType.name().toLowerCase());
        if (translated != null) {
            this.message = translated;
        } else {
            this.message = messageType.defaultValue;
        }
        this.placeholders = new ArrayList<>(messageType.placeholders);
    }

    public Message applyPlaceholder(String placeholder, Object value) {
        placeholders.remove(placeholder);
        if (value instanceof Message m) {
            this.message = message.replace("%" + placeholder + "%", m.toString());
        } else if (value instanceof Text t) {
            this.message = message.replace("%" + placeholder + "%", t.getString());
        } else {
            this.message = message.replace("%" + placeholder + "%", String.valueOf(value));
        }
        return this;
    }

    public Message applyPlaceholders(Map<String, Object> placeholders) {
        placeholders.forEach(this::applyPlaceholder);
        return this;
    }

    @Override
    public String toString() {
        return message;
    }

    public List<String> toStringList() {
        return List.of(message.split("\n"));
    }

    public List<Text> toComponentList() {
        return toStringList().stream()
                .map(m -> TagParser.DEFAULT.parseText(m, ParserContext.of()))
                .toList();
    }

    public Text toComponent() {
        return TagParser.DEFAULT.parseText(message, ParserContext.of());
    }

    public static Text toComponent(String message) {
        return TagParser.DEFAULT.parseText(message, ParserContext.of());
    }

    public static MessageTranslator getTranslator() {
        return TRANSLATOR;
    }

    public enum MessageType {
        EFFECT_BROADCAST(List.of("player", "item", "x", "y", "z", "dimension"), "🧪 %player% is cooking up the %item% at %x%, %y%, %z%... %dimension%"),
        DISCORD_BROADCAST(List.of("player", "item", "x", "y", "z", "dimension"), "%player% is cooking up the %item% at %x%, %y%, %z% in %dimension% @everyone"),
        EFFECT_FINISHED(List.of("item"), "%item% has been brewed!"),
        REGULAR_BROADCAST(List.of("item", "x", "y", "z", "dimension"), "🧪 A %item% has been crafted at <#90D5FF><b>%x%, %y%, %z%... %dimension%"),
        SLOT_EMPTY(List.of("slot"), "<red>You don't have any effect equipped in slot %slot%."),
        EFFECT_NONE_EQUIPPED(List.of("slot"), "<red>You don't have an Effect equipped in slot %slot%."),
        WITHDRAW_INVALID("<red>Invalid usage. Use /ldrain or /rdrain"),
        TRUST_CONSOLE_USAGE("<red>Only players can use this command."),
        TRUST_INCORRECT_USAGE(List.of("label"), "<red>Usage: /%label% <player>"),
        TRUST_NO_PLAYER("<red>Player not found."),
        TRUST_SELF("<red>You always trust yourself. Surely..."),
        TRUST_ADDED(List.of("target"), "<green>You now trust %target%."),
        TRUST_ALREADY_TRUSTED(List.of("target"), "<green>You already trust %target%."),
        TRUST_REMOVED(List.of("target"), "<green>You no longer trust %target%."),
        TRUST_NOT_TRUSTED(List.of("target"), "<green>You already didn't trust %target%."),
        EFFECT_NO_BREWING("<red>You need to craft this in a brewing stand!"),
        DEATH_MESSAGE(List.of("victim", "killer"), "%victim% was slain by %killer%"),
        CONTROLS_USAGE("<red>Usage: /controls <offhand|command>"),
        CONTROLS_INVALID_PARAM("<red>Invalid option. Use \"offhand\" or \"command\"."),
        INFUSE_INVALID_PARAM("<red>Please use the tab completions as a reference."),
        INFUSE_INVALID_SLOT(List.of("slot"), "<red>Invalid Argument! Could not identify slot %slot%. Please use \"1\" or \"2\"."),
        INFUSE_HELP("<gold>--- Infuse Help ---\n<yellow>/infuse gui <gray>- Open infuse GUI\n<yellow>/infuse reload <gray>- Reload config\n<yellow>/infuse recipes <gray>- View recipes\n<yellow>/infuse help <gray>- Show this help message"),
        INFUSE_CONTROLS_USAGE("<red>Usage: /infuse controls <offhand|command>"),
        INFUSE_CONTROLS_SUCCESS(List.of("control_mode"), "<dark_red>Your controls are now %control_mode%"),
        INFUSE_SETEFFECT_USAGE("<red>Invalid Argument! Please use /infuse setEffect <slot> <player> <effect>"),
        INFUSE_SETEFFECT_SUCCESS(List.of("slot", "player_name", "effect_name"), "<green>Successfully set the effect in slot %slot% of player %player_name% to %effect_name%."),
        INFUSE_GIVEEFFECT_USAGE("<red>Invalid Argument! Please use /infuse giveEffect <player> <effect>"),
        INFUSE_GIVEEFFECT_SUCCESS(List.of("effect_color", "effect_name"), "%effect_color%You received the %effect_name%"),
        INFUSE_CLEAREFFECTS_USAGE("<red>Invalid Argument! Please use /infuse clearEffects <player>"),
        INFUSE_CLEAREFFECTS_SUCCESS(List.of("player_name"), "<green>Cleared %player_name%'s effects"),
        INFUSE_COOLDOWN_USAGE("<red>Invalid Argument! Please use /infuse cooldown <player>"),
        INFUSE_COOLDOWN_SUCCESS(List.of("player_name"), "<green>Removed %player_name%'s cooldown"),
        CLEAREFFECTS_USAGE("<red>Usage: /infuse clearEffects <player>"),
        CONTROL_MODE_NOTIFY(List.of("control_mode"), "<gray>Your ability mode is set to: %control_mode%"),
        JOIN_ABILITY_NOTIFY(List.of("control_mode"), "<gray>Your ability mode is set to: %control_mode%"),
        DRAIN_SUCCESS(List.of("effect_name"), "<green>You have drained your: %effect_name%"),
        DRAIN_CANCELLED("<red>Drain was cancelled."),
        EFFECT_EQUIPPED(List.of("effect_name"), "<green>You have equipped the %effect_name%"),
        SWAP_NO_EFFECTS("<red>You do not have any effects equipped to swap."),
        SWAP_SUCCESS("<green>Your Effects have been swapped."),
        THIEF_STEAL(List.of("victim", "effect_name"), "<yellow>You stole %victim%'s %effect_name% Effect"),
        RECIPE_NOT_FOUND("<red>No recipe found for this potion."),
        RECIPE_DISABLED("<red>Recipe is disabled/broken"),
        ERROR_INV_FULL("<red>Your inventory is full! Make space before unequipping."),
        ERROR_NOT_PLAYER("<red>Only players can use this command."),
        ERROR_NOT_OP("<red>You must be OP to run this command."),
        ERROR_INVALID_COMMAND("<red>Invalid command."),
        ERROR_RITUAL_ACTIVE("<red>A ritual is already in progress!"),
        ERROR_TARGET_NOT_FOUND("<red>Player not found or not online."),

        // Effect messages
        EMERALD_NAME("<#009420>Emerald Effect"),
        EMERALD_LORE("<#009420><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#009420>$ <dark_gray>Looting 5\n<#009420>$ <dark_gray>Luck 10\n<#009420>$ <dark_gray>1.5x EXP\n<#009420>$ <dark_gray>Consumables have a 15% chance of not being consumed\n<#009420>$ <dark_gray>Enchanting table always on level 30\n<dark_gray>\n<#009420><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<#009420>$ <dark_gray>Hero of the village 255\n<#009420>$ <dark_gray>Consumables have a 25% chance of not being consumed\n<#009420>$ <dark_gray>3x EXP\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_EMERALD_NAME("<#009420>Augmented Emerald Effect"),
        AUG_EMERALD_LORE("<#009420><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#009420>$ <dark_gray>Looting 5\n<#009420>$ <dark_gray>Luck 10\n<#009420>$ <dark_gray>1.5x EXP\n<#009420>$ <dark_gray>Consumables have a 15% chance of not being consumed\n<#009420>$ <dark_gray>Enchanting table always on level 30\n<dark_gray>\n<#009420><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<#009420>$ <dark_gray>Hero of the village 255\n<#009420>$ <dark_gray>Consumables have a 25% chance of not being consumed\n<#009420>$ <dark_gray>3x EXP\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

        ENDER_NAME("<dark_purple>Ender Effect"),
        ENDER_LORE("<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_purple>⭐ <dark_gray>All nearby untrusted players have glowing\n<dark_purple>⭐ <dark_gray>Use dragon's breath to shoot powerful fireballs that curse players\n<dark_purple>⭐ <dark_gray>Curse untrusted players on hit which shares damage with all\n<dark_purple>⭐ <dark_gray>cursed players\n<dark_gray>\n<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_purple>⭐ <dark_gray>Teleport to the cursor position within a 15 block radius\n<dark_purple>⭐ <dark_gray>Instantly kills any mob and curses players\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 10s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),
        AUG_ENDER_NAME("<dark_purple>Augmented Ender Effect"),
        AUG_ENDER_LORE("<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_purple>⭐ <dark_gray>All nearby untrusted players have glowing\n<dark_purple>⭐ <dark_gray>Use dragon's breath to shoot powerful fireballs that curse players\n<dark_purple>⭐ <dark_gray>Curse untrusted players on hit which shares damage with all\n<dark_purple>⭐ <dark_gray>cursed players\n<dark_gray>\n<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_purple>⭐ <dark_gray>Teleport to the cursor position within a 15 block radius\n<dark_purple>⭐ <dark_gray>Instantly kills any mob and curses players\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

        FEATHER_NAME("<#BEA3CA>Feather Effect"),
        FEATHER_LORE("<#BEA3CA><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#BEA3CA>\ud83e\udeb6 <dark_gray>No fall damage\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Attacking from 7+ block fall does a mace hit\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Auto windcharge counter after being attacked 10 times\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 0.5x cooldown\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 2x velocity\n<dark_gray>\n<#BEA3CA><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Launches the player upward\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Slams the player back down\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 2s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_FEATHER_NAME("<#BEA3CA>Augmented Feather Effect"),
        AUG_FEATHER_LORE("<#BEA3CA><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#BEA3CA>\ud83e\udeb6 <dark_gray>No fall damage\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Attacking from 7+ block fall does a mace hit\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Auto windcharge counter after being attacked 10 times\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 0.5x cooldown\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Windcharges have 2x velocity\n<dark_gray>\n<#BEA3CA><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Launches the player upward\n<#BEA3CA>\ud83e\udeb6 <dark_gray>Slams the player back down\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 2s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

        FIRE_NAME("<#E85720>Fire Effect"),
        FIRE_LORE("<#E85720><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#E85720>\ud83d\udd25 <dark_gray>Fire Resistance\n<#E85720>\ud83d\udd25 <dark_gray>Full charged bow shots set arrows on fire\n<#E85720>\ud83d\udd25 <dark_gray>In lava, no fall damage\n<#E85720>\ud83d\udd25 <dark_gray>Every 10 hits sets target on fire for 5s\n<dark_gray>\n<#E85720><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:\n<#E85720>\ud83d\udd25 <dark_gray>Set surrounding enemies on fire (5 block radius)\n<dark_gray>\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_FIRE_NAME("<#E85720>Augmented Fire Effect"),
        AUG_FIRE_LORE("<#E85720><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#E85720>\ud83d\udd25 <dark_gray>Fire Resistance\n<#E85720>\ud83d\udd25 <dark_gray>Full charged bow shots set arrows on fire\n<#E85720>\ud83d\udd25 <dark_gray>In lava, no fall damage\n<#E85720>\ud83d\udd25 <dark_gray>Every 10 hits sets target on fire for 5s\n<dark_gray>\n<#E85720><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:\n<#E85720>\ud83d\udd25 <dark_gray>Set surrounding enemies on fire (5 block radius)\n<dark_gray>\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

        FROST_NAME("<aqua>Frost Effect"),
        FROST_LORE("<aqua><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<aqua>❄ <dark_gray>Speed 3 on ice and snow\n<aqua>❄ <dark_gray>Freeze player every 10 hits\n<aqua>❄ <dark_gray>Frozen enemies can't use windcharges\n<dark_gray>\n<aqua><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<aqua>❄ <dark_gray>Reduce enemies jump strength and freeze them every hit\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 90s"),
        AUG_FROST_NAME("<aqua>Augmented Frost Effect"),
        AUG_FROST_LORE("<aqua><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<aqua>❄ <dark_gray>Speed 3 on ice and snow\n<aqua>❄ <dark_gray>Freeze player every 10 hits\n<aqua>❄ <dark_gray>Frozen enemies can't use windcharges\n<dark_gray>\n<aqua><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<aqua>❄ <dark_gray>Reduce enemies jump strength and freeze them every hit\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),

        HASTE_NAME("<#BD934F>Haste Effect"),
        HASTE_LORE("<#BD934F><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#BD934F>⛏ <dark_gray>Fortune 5 + Efficiency 10 + Unbreaking 5 on pickaxes\n<#BD934F>⛏ <dark_gray>Halved shield cooldown when stunned\n<dark_gray>\n<#BD934F><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<#BD934F>⛏ <dark_gray>Attack faster\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_HASTE_NAME("<#BD934F>Augmented Haste Effect"),
        AUG_HASTE_LORE("<#BD934F><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#BD934F>⛏ <dark_gray>Fortune 5 + Efficiency 10 + Unbreaking 5 on pickaxes\n<#BD934F>⛏ <dark_gray>Halved shield cooldown when stunned\n<dark_gray>\n<#BD934F><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<#BD934F>⛏ <dark_gray>Attack faster\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

        HEART_NAME("<red>Heart Effect"),
        HEART_LORE("<red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<red>❤ <dark_gray>+5 Hearts\n<red>❤ <dark_gray>All food gives absorption\n<red>❤ <dark_gray>Egaps gives +10 absorption hearts\n<red>❤ <dark_gray>See player's health every 10 hits\n<dark_gray>\n<red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<red>❤ <dark_gray>Heal players to 20 hearts instantly\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 60s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 120s"),
        AUG_HEART_NAME("<red>Augmented Heart Effect"),
        AUG_HEART_LORE("<red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<red>❤ <dark_gray>+5 Hearts\n<red>❤ <dark_gray>All food gives absorption\n<red>❤ <dark_gray>Egaps gives +10 absorption hearts\n<red>❤ <dark_gray>See player's health every 10 hits\n<dark_gray>\n<red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<red>❤ <dark_gray>Heal players to 20 hearts instantly\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 60s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),

        INVIS_NAME("<#2B0078>Invis Effect"),
        INVIS_LORE("<#2B0078><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#2B0078>\ud83d\udc41 <dark_gray>Permanent Invisibility\n<#2B0078>\ud83d\udc41 <dark_gray>Full bow shot blinds the target for 5s and gives blindness for 2s\n<#2B0078>\ud83d\udc41 <dark_gray>Mobs cannot target you\n<dark_gray>\n<#2B0078><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<#2B0078>\ud83d\udc41 <dark_gray>Creates a 5×5 hollow circle of black dust particles\n<#2B0078>\ud83d\udc41 <dark_gray>Inside: allies become fully invisible; enemies get blindness\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 90s"),
        AUG_INVIS_NAME("<#2B0078>Augmented Invis Effect"),
        AUG_INVIS_LORE("<#2B0078><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#2B0078>\ud83d\udc41 <dark_gray>Permanent Invisibility\n<#2B0078>\ud83d\udc41 <dark_gray>Full bow shot blinds the target for 5s and gives blindness for 2s\n<#2B0078>\ud83d\udc41 <dark_gray>Mobs cannot target you\n<dark_gray>\n<#2B0078><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<#2B0078>\ud83d\udc41 <dark_gray>Creates a 5×5 hollow circle of black dust particles\n<#2B0078>\ud83d\udc41 <dark_gray>Inside: allies become fully invisible; enemies get blindness\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 45s"),

        OCEAN_NAME("<blue>Ocean Effect"),
        OCEAN_LORE("<blue><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<blue>🫧 <dark_gray>Swim faster\n<blue>🫧 <dark_gray>Breathe underwater\n<blue>🫧 <dark_gray>Make everyone around you start drowning when in water\n<blue>🫧 <dark_gray>Tridents pull players\n<dark_gray>\n<blue><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:\n<blue>🫧 <dark_gray>Creates a Whirlhole\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_OCEAN_NAME("<blue>Augmented Ocean Effect"),
        AUG_OCEAN_LORE("<blue><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<blue>🫧 <dark_gray>Swim faster\n<blue>🫧 <dark_gray>Breathe underwater\n<blue>🫧 <dark_gray>Make everyone around you start drowning when in water\n<blue>🫧 <dark_gray>Tridents pull players\n<dark_gray>\n<blue><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:\n<blue>🫧 <dark_gray>Creates a Whirlhole\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 15s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

        REGEN_NAME("<#FC00DD>Regen Effect"),
        REGEN_LORE("<#B0009A><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#FC00DD>+ <dark_gray>No hunger loss\n<#FC00DD>+ <dark_gray>Permanent Regeneration\n<#FC00DD>+ <dark_gray>Every hit gives Regeneration 2 for 3.0 seconds\n<#FC00DD>+ <dark_gray>All food gives +3.0 saturation bars\n<#FC00DD>+ <dark_gray>Eat anytime\n<#FC00DD>+ <dark_gray>10th hit takes away 1.0 hunger bar from your target\n<dark_gray>\n<#B0009A>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:\n<#FC00DD>+ <dark_gray>Damage dealt heals you and nearby teammates\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_REGEN_NAME("<#FC00DD>Augmented Regen Effect"),
        AUG_REGEN_LORE("<#B0009A><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#FC00DD>+ <dark_gray>No hunger loss\n<#FC00DD>+ <dark_gray>Permanent Regeneration\n<#FC00DD>+ <dark_gray>Every hit gives Regeneration 2 for 3.0 seconds\n<#FC00DD>+ <dark_gray>All food gives +3.0 saturation bars\n<#FC00DD>+ <dark_gray>Eat anytime\n<#FC00DD>+ <dark_gray>10th hit takes away 1.0 hunger bar from your target\n<dark_gray>\n<#B0009A>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛ:\n<#FC00DD>+ <dark_gray>Damage dealt heals you and nearby teammates\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

        SPEED_NAME("<#E8BD74>Speed Effect"),
        SPEED_LORE("<#E8BD74><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#E8BD74>⋘ <dark_gray>Speed 1\n<#E8BD74>⋘ <dark_gray>Increase speed level by 1 after each hit\n<#E8BD74>⋘ <dark_gray>Speed resets after 1 second of no activity\n<#E8BD74>⋘ <dark_gray>Ranged weapons charge 1.5x faster\n<#E8BD74>⋘ <dark_gray>Enemy invincibility frames are halved\n<dark_gray>\n<#E8BD74><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<#E8BD74>⋘ <dark_gray>Speed Dash\n<dark_gray>\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 20s"),
        AUG_SPEED_NAME("<#E8BD74>Augmented Speed Effect"),
        AUG_SPEED_LORE("<#E8BD74><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<#E8BD74>⋘ <dark_gray>Speed 1\n<#E8BD74>⋘ <dark_gray>Increase speed level by 1 after each hit\n<#E8BD74>⋘ <dark_gray>Speed resets after 1 second of no activity\n<#E8BD74>⋘ <dark_gray>Ranged weapons charge 1.5x faster\n<#E8BD74>⋘ <dark_gray>Enemy invincibility frames are halved\n<dark_gray>\n<#E8BD74><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<#E8BD74>⋘ <dark_gray>Speed Dash\n<dark_gray>\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 10s"),

        STRENGTH_NAME("<dark_red>Strength Effect"),
        STRENGTH_LORE("<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_red>\ud83d\udee1 <dark_gray>Double Damage to all mobs\n<dark_red>\ud83d\udee1 <dark_gray>Disable shields for 10 seconds\n<dark_red>\ud83d\udee1 <dark_gray>Ranged weapons pierce shields\n<dark_red>\ud83d\udee1 <dark_gray>+1 Damage when under 6 hearts\n<dark_red>\ud83d\udee1 <dark_gray>+2 Damage when under 4 hearts\n<dark_red>\ud83d\udee1 <dark_gray>+3 Damage when under 2 hearts\n<dark_gray>\n<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_red>\ud83d\udee1 <dark_gray>All attacks are critical for 15 seconds\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_STRENGTH_NAME("<dark_red>Augmented Strength Effect"),
        AUG_STRENGTH_LORE("<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_red>\ud83d\udee1 <dark_gray>Double Damage to all mobs\n<dark_red>\ud83d\udee1 <dark_gray>Disable shields for 10 seconds\n<dark_red>\ud83d\udee1 <dark_gray>Ranged weapons pierce shields\n<dark_red>\ud83d\udee1 <dark_gray>+1 Damage when under 6 hearts\n<dark_red>\ud83d\udee1 <dark_gray>+2 Damage when under 4 hearts\n<dark_red>\ud83d\udee1 <dark_gray>+3 Damage when under 2 hearts\n<dark_gray>\n<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_red>\ud83d\udee1 <dark_gray>All attacks are critical for 15 seconds\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

        THUNDER_NAME("<yellow>Thunder Effect"),
        THUNDER_LORE("<yellow><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<yellow>⚡ <dark_gray>Chain lightning\n<yellow>⚡ <dark_gray>Tridents Strikes Lightning\n<dark_gray>\n<yellow><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<yellow>⚡ <dark_gray>Strike enemies with lightning and make a thunderstorm\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 60s"),
        AUG_THUNDER_NAME("<yellow>Augmented Thunder Effect"),
        AUG_THUNDER_LORE("<yellow><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<yellow>⚡ <dark_gray>Chain lightning\n<yellow>⚡ <dark_gray>Tridents Strikes Lightning\n<dark_gray>\n<yellow><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<yellow>⚡ <dark_gray>Strike enemies with lightning and make a thunderstorm\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 30s"),

        // Extra effect messages
        APOPHIS_NAME("<dark_purple>Apophis Effect"),
        APOPHIS_LORE("<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_purple>🍼 <dark_gray>Combine Fire, Emerald and Heart's effects\n<dark_purple>🍼 <dark_gray>Have a custom skin and nametag\n<dark_gray>\n<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_purple>🍼 <dark_gray>Activate Fire, Emerald and Heart's sparks\n<dark_purple>🍼 <dark_gray>Upon hitting a player blind their screen\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 20s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 2m"),
        AUG_APOPHIS_NAME("<dark_purple>Augmented Apophis Effect"),
        AUG_APOPHIS_LORE("<dark_purple><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_purple>🍼 <dark_gray>Combine Fire, Emerald and Heart's effects\n<dark_purple>🍼 <dark_gray>Have a custom skin and nametag\n<dark_gray>\n<dark_purple><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_purple>🍼 <dark_gray>Activate Fire, Emerald and Heart's sparks\n<dark_purple>🍼 <dark_gray>Upon hitting a player blind their screen\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: 30s\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: 1m 30s"),

        THIEF_NAME("<dark_red>Thief Effect"),
        THIEF_LORE("<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_red>🥷 <dark_gray>You're not shown on tablist\n<dark_red>🥷 <dark_gray>Your footsteps don't make noise\n<dark_red>🥷 <dark_gray>Kill a player to disguise yourself as them\n<dark_gray>\n<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_red>🥷 <dark_gray>Temporarily steal your opponents effect\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: Unknown\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: Unknown"),
        AUG_THIEF_NAME("<dark_red>Augmented Thief Effect"),
        AUG_THIEF_LORE("<dark_red><b>ᴘᴀꜱꜱɪᴠᴇ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_red>🥷 <dark_gray>You're not shown on tablist\n<dark_red>🥷 <dark_gray>Your footsteps don't make noise\n<dark_red>🥷 <dark_gray>Kill a player to disguise yourself as them\n<dark_gray>\n<dark_red><b>ꜱᴘᴀʀᴋ ᴇꜰꜰᴇᴄᴛꜱ:\n<dark_red>🥷 <dark_gray>Temporarily steal your opponents effect\n<dark_gray>\n<dark_aqua>ᴅᴜʀᴀᴛɪᴏɴ: Unknown\n<dark_aqua>ᴄᴏᴏʟᴅᴏᴡɴ: Unknown");

        public final List<String> placeholders;
        public final String defaultValue;

        MessageType(String defaultValue) {
            this(List.of(), defaultValue);
        }

        MessageType(List<String> placeholders, String defaultValue) {
            this.placeholders = placeholders;
            this.defaultValue = defaultValue;
        }

        // Aliases for compatibility
        public static final MessageType TRUST_CONSOLEUSAGE = TRUST_CONSOLE_USAGE;
        public static final MessageType TRUST_INCORRECTUSAGE = TRUST_INCORRECT_USAGE;
        public static final MessageType TRUST_NOPLAYER = TRUST_NO_PLAYER;
        public static final MessageType TRUST_ALREADYTRUSTED = TRUST_ALREADY_TRUSTED;
        public static final MessageType TRUST_NOTTRUSTED = TRUST_NOT_TRUSTED;
        public static final MessageType EFFECT_NOBREWING = EFFECT_NO_BREWING;
    }
}
