package com.nxfx21.infabric;

import net.minecraft.entity.boss.BossBar;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import java.awt.Color;

public class EffectConstants {
    /**
     * Gets the Item to use in the background of this effect's menu.
     * 
     * @param effectId The id of the infuse effect.
     * 
     * @return the Item for the effect.
     */
    public static Item menuBackgroundColor(int effectId) {
        return switch (effectId) {
            case EffectIds.EMERALD -> Items.LIME_STAINED_GLASS_PANE;
            case EffectIds.ENDER -> Items.PURPLE_STAINED_GLASS_PANE;
            case EffectIds.FEATHER -> Items.WHITE_STAINED_GLASS_PANE;
            case EffectIds.FIRE -> Items.ORANGE_STAINED_GLASS_PANE;
            case EffectIds.FROST -> Items.LIGHT_BLUE_STAINED_GLASS_PANE;
            case EffectIds.HASTE -> Items.ORANGE_STAINED_GLASS_PANE;
            case EffectIds.HEART -> Items.RED_STAINED_GLASS_PANE;
            case EffectIds.INVIS -> Items.LIGHT_GRAY_STAINED_GLASS_PANE;
            case EffectIds.OCEAN -> Items.BLUE_STAINED_GLASS_PANE;
            case EffectIds.REGEN -> Items.RED_STAINED_GLASS_PANE;
            case EffectIds.SPEED -> Items.LIGHT_BLUE_STAINED_GLASS_PANE;
            case EffectIds.STRENGTH -> Items.RED_STAINED_GLASS_PANE;
            case EffectIds.THUNDER -> Items.YELLOW_STAINED_GLASS_PANE;
            case EffectIds.APOPHIS -> Items.MAGENTA_STAINED_GLASS_PANE;
            case EffectIds.THIEF -> Items.RED_STAINED_GLASS_PANE;
            default -> Items.AIR;
        };
    }

    /**
     * Gets the Color for this effect's potion and related text.
     * 
     * @param effectId The id of the infuse effect.
     * 
     * @return the Color for the effect.
     */
    public static Color potionColor(int effectId) {
        return switch (effectId) {
            case EffectIds.EMERALD -> Color.GREEN;
            case EffectIds.ENDER -> new Color(0x800080);
            case EffectIds.FEATHER -> new Color(0xBEA3CA);
            case EffectIds.FIRE -> new Color(0xEE5522);
            case EffectIds.FROST -> new Color(0x55FFFF);
            case EffectIds.HASTE -> new Color(0xFFCC33);
            case EffectIds.HEART -> Color.RED;
            case EffectIds.INVIS -> new Color(0xAA00AA);
            case EffectIds.OCEAN -> new Color(0x0066FF);
            case EffectIds.REGEN -> new Color(0xFF5555);
            case EffectIds.SPEED -> new Color(0xEEBB77);
            case EffectIds.STRENGTH -> new Color(0x800000);
            case EffectIds.THUNDER -> Color.YELLOW;
            case EffectIds.APOPHIS -> new Color(0x440044);
            case EffectIds.THIEF -> new Color(0xAA0000);
            default -> null;
        };
    }

    /**
     * Gets the BossBar.Color for this effect's ritual.
     * 
     * @param effectId The id of the infuse effect.
     * 
     * @return the BossBar.Color for the effect.
     */
    public static BossBar.Color ritualColor(int effectId) {
        return switch (effectId) {
            case EffectIds.EMERALD -> BossBar.Color.GREEN;
            case EffectIds.ENDER -> BossBar.Color.PURPLE;
            case EffectIds.FEATHER -> BossBar.Color.WHITE;
            case EffectIds.FIRE -> BossBar.Color.RED;
            case EffectIds.FROST -> BossBar.Color.BLUE;
            case EffectIds.HASTE -> BossBar.Color.YELLOW;
            case EffectIds.HEART -> BossBar.Color.RED;
            case EffectIds.INVIS -> BossBar.Color.PURPLE;
            case EffectIds.OCEAN -> BossBar.Color.BLUE;
            case EffectIds.REGEN -> BossBar.Color.PINK;
            case EffectIds.SPEED -> BossBar.Color.YELLOW;
            case EffectIds.STRENGTH -> BossBar.Color.RED;
            case EffectIds.THUNDER -> BossBar.Color.RED;
            case EffectIds.APOPHIS -> BossBar.Color.PURPLE;
            case EffectIds.THIEF -> BossBar.Color.YELLOW;
            default -> BossBar.Color.WHITE;
        };
    }
}
