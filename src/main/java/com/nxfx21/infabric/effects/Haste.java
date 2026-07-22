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

public class Haste extends InfuseEffect {
    private final Infuse plugin;

    public Haste() {
        this(false);
    }

    public Haste(boolean augmented) {
        super("haste", EffectIds.HASTE, augmented, EffectConstants.potionColor(EffectIds.HASTE), EffectConstants.ritualColor(EffectIds.HASTE));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {}

    @Override
    public void unequip(ServerPlayerEntity owner) {}

    @Override
    public void applyPassives(ServerPlayerEntity owner) {
        net.minecraft.item.ItemStack stack = owner.getMainHandStack();
        if (com.nxfx21.infabric.util.ItemUtil.isPickaxe(stack)) {
            var registry = owner.getWorld().getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.FORTUNE).ifPresent(entry -> 
                com.nxfx21.infabric.util.ItemUtil.applySpecialEnchantment(stack, "infuse:haste_fortune", entry, plugin.getMainConfig().hasteFortuneLevel()));
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.EFFICIENCY).ifPresent(entry -> 
                com.nxfx21.infabric.util.ItemUtil.applySpecialEnchantment(stack, "infuse:haste_efficiency", entry, plugin.getMainConfig().hasteEfficiencyLevel()));
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.UNBREAKING).ifPresent(entry -> 
                com.nxfx21.infabric.util.ItemUtil.applySpecialEnchantment(stack, "infuse:haste_unbreaking", entry, plugin.getMainConfig().hasteUnbreakingLevel()));
        }
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "haste")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        owner.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, (int) duration * 20, 3));
        CooldownManager.setTimes(playerUUID, "haste", duration, cooldown);
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Haste();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Haste(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_HASTE_NAME : MessageType.HASTE_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_HASTE_LORE : MessageType.HASTE_LORE);
    }

    public static void cleanupInventory(net.minecraft.inventory.Inventory inventory, ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect hasteEffect = InfuseEffect.fromString("haste");
        if (hasteEffect == null || !plugin.getDataManager().hasEffect(player.getUuid(), hasteEffect)) return;

        for (int i = 0; i < inventory.size(); i++) {
            net.minecraft.item.ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            var registry = player.getWorld().getRegistryManager().getOrThrow(net.minecraft.registry.RegistryKeys.ENCHANTMENT);
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.FORTUNE).ifPresent(entry -> 
                com.nxfx21.infabric.util.ItemUtil.removeSpecialEnchant(stack, "infuse:haste_fortune", entry));
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.EFFICIENCY).ifPresent(entry -> 
                com.nxfx21.infabric.util.ItemUtil.removeSpecialEnchant(stack, "infuse:haste_efficiency", entry));
            
            registry.getOptional(net.minecraft.enchantment.Enchantments.UNBREAKING).ifPresent(entry -> 
                com.nxfx21.infabric.util.ItemUtil.removeSpecialEnchant(stack, "infuse:haste_unbreaking", entry));
        }
    }
}
