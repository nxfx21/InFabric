package com.nxfx21.infabric.util;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;

public class ItemUtil {
    public static boolean isSword(ItemStack item) {
        return item != null && item.isIn(ItemTags.SWORDS);
    }

    public static boolean isPickaxe(ItemStack item) {
        return item != null && item.isIn(ItemTags.PICKAXES);
    }

    public static boolean isAxe(ItemStack item) {
        return item != null && item.isIn(ItemTags.AXES);
    }

    public static boolean isShovel(ItemStack item) {
        return item != null && item.isIn(ItemTags.SHOVELS);
    }

    public static boolean isHoe(ItemStack item) {
        return item != null && item.isIn(ItemTags.HOES);
    }

    public static void giveOrDropItem(ServerPlayerEntity player, ItemStack... items) {
        for (ItemStack item : items) {
            if (!player.getInventory().insertStack(item)) {
                player.dropItem(item, false);
            }
        }
    }

    public static void applySpecialEnchantment(ItemStack item, String key, RegistryEntry<Enchantment> enchantment, int newLevel) {
        NbtComponent customData = item.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound nbt = customData.copyNbt();
        
        if (nbt.contains(key)) return;
        
        ItemEnchantmentsComponent enchantments = item.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        int oldLevel = enchantments.getLevel(enchantment);
        
        if (oldLevel >= newLevel) return;
        
        nbt.putInt(key, oldLevel);
        item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);
        builder.set(enchantment, newLevel);
        item.set(DataComponentTypes.ENCHANTMENTS, builder.build());
    }

    public static void removeSpecialEnchant(ItemStack item, String key, RegistryEntry<Enchantment> enchantment) {
        NbtComponent customData = item.get(DataComponentTypes.CUSTOM_DATA);
        if (customData == null) return;
        
        NbtCompound nbt = customData.copyNbt();
        if (!nbt.contains(key)) return;
        
        int oldLevel = nbt.getInt(key);
        nbt.remove(key);
        
        if (nbt.isEmpty()) {
            item.remove(DataComponentTypes.CUSTOM_DATA);
        } else {
            item.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }
        
        ItemEnchantmentsComponent enchantments = item.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);
        if (oldLevel > 0) {
            builder.set(enchantment, oldLevel);
        } else {
            builder.remove(e -> e.equals(enchantment));
        }
        item.set(DataComponentTypes.ENCHANTMENTS, builder.build());
    }
}
