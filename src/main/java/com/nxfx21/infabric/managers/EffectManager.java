package com.nxfx21.infabric.managers;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.events.EffectEquipEvent;
import com.nxfx21.infabric.events.EffectUnequipEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EffectManager {
    private final Infuse plugin;

    public EffectManager(Infuse plugin) {
        this.plugin = plugin;
    }

    public EquipResult setEffect(ServerPlayerEntity player, InfuseEffect effect, String slot) {
        return equipEffect(player, effect, slot, true);
    }

    public EquipResult equipEffect(ServerPlayerEntity player, InfuseEffect effect, String slot, boolean override) {
        // Calling an EffectEquipEvent and stopping if it is canceled.
        boolean allowed = EffectEquipEvent.EVENT.invoker().onEquip(player, effect, slot);
        if (!allowed) return new EquipResult(EquipResultType.CANCELLED, effect);

        InfuseEffect equipped = plugin.getDataManager().getEffect(player.getUuid(), slot);
        if (equipped != null && !override) return new EquipResult(EquipResultType.FAIL);

        // Unequipping the old effect
        if (equipped != null) {
            EquipResult res = unequipEffect(player, slot);
            if (res.type != EquipResultType.SUCCESS) return new EquipResult(res.type, effect);
        }

        // Equipping the effect and updating the player data
        effect.equip(player);
        plugin.getDataManager().setEffect(player.getUuid(), slot, effect);

        return new EquipResult(EquipResultType.SUCCESS, effect);
    }

    public EquipResult drainEffect(ServerPlayerEntity player, String slot) {
        // Unequipping the effect
        EquipResult result = unequipEffect(player, slot);

        // Checking if an effect was removed
        if (result.type == EquipResultType.FAIL) {
            Message msg = new Message(MessageType.EFFECT_NONE_EQUIPPED);
            msg.applyPlaceholder("slot", slot);
            player.sendMessage(msg.toComponent());
            return result;
        }

        // Skipping if the unequip event was canceled
        if (result.type == EquipResultType.CANCELLED) {
            if (result.effect == null) {
                throw new IllegalStateException("Cancelled unequip events should still return their related effect");
            }
            // If the DRAIN_CANCELLED message is missing from config, we can handle it or use a default
            player.sendMessage(Message.toComponent("<red>Drain cancelled."));
            return result;
        }

        if (result.effect == null) {
            throw new IllegalStateException("Successful unequip events need to return their related effect.");
        }

        // Making sure the player has inventory space
        if (player.getInventory().getEmptySlot() == -1) {
            player.sendMessage(new Message(MessageType.ERROR_INV_FULL).toComponent());
            return result;
        }

        // Giving the player the item
        player.getInventory().insertStack(result.effect.createItem());

        // Sending the success message
        Message msg = new Message(MessageType.DRAIN_SUCCESS);
        msg.applyPlaceholder("effect_name", result.effect.getName().toString());
        player.sendMessage(msg.toComponent());

        return result;
    }

    public EquipResult dropEffect(ServerPlayerEntity player, String slot) {
        EquipResult result = unequipEffect(player, slot);

        if (result.type == EquipResultType.FAIL) return result;

        if (result.type == EquipResultType.CANCELLED) {
            if (result.effect == null) {
                throw new IllegalStateException("Cancelled unequip events should still return their related effect");
            }
            return result;
        }

        if (result.effect == null) {
            throw new IllegalStateException("Successful unequip events need to return their related effect.");
        }

        // Dropping the item
        player.getWorld().spawnEntity(new net.minecraft.entity.ItemEntity(
            player.getWorld(), player.getX(), player.getY(), player.getZ(), result.effect.createItem()
        ));

        return result;
    }

    public EquipResult unequipEffect(ServerPlayerEntity player, String slot) {
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUuid(), slot);
        if (effect == null) return new EquipResult(EquipResultType.FAIL);

        // Calling an EffectUnequipEvent
        boolean allowed = EffectUnequipEvent.EVENT.invoker().onUnequip(player, effect, slot);
        if (!allowed) return new EquipResult(EquipResultType.CANCELLED, effect);

        // Unequipping the effect and updating the player data
        effect.unequip(player);
        plugin.getDataManager().removeEffect(player.getUuid(), slot);

        return new EquipResult(EquipResultType.SUCCESS, effect);
    }

    public void onDrinkEffect(ServerPlayerEntity player, ItemStack item) {
        // Getting the effect from the item
        InfuseEffect effect = InfuseEffect.fromItem(item);
        if (effect == null) return;

        // Equipping the effect
        EquipResult result = this.equipEffect(player, effect, "1", false);

        // Equipping the slot in the players other slot
        if (result.type == EquipResultType.FAIL) {
            if (plugin.getDataManager().getEffect(player.getUuid(), "2") != null) {
                result = this.drainEffect(player, "2");
                if (result.type == EquipResultType.CANCELLED || result.type == EquipResultType.FAIL) return;
            }
            result = this.equipEffect(player, effect, "2", false);
        }

        if (result.type == EquipResultType.CANCELLED) return;

        // Notifying the player
        Message msg = new Message(MessageType.EFFECT_EQUIPPED);
        msg.applyPlaceholder("effect_name", effect.getName().toString());
        player.sendMessage(msg.toComponent());

        // Decrement item stack size
        item.decrement(1);
    }

    public void handleDeath(ServerPlayerEntity player) {
        EquipResult result;
        String dropMode = plugin.getMainConfig().effectDrops();
        switch (dropMode.toLowerCase()) {
            case "random" -> {
                String slot = (Math.random() > 0.5) ? "1" : "2";
                result = dropEffect(player, slot);
                if (result.type == EquipResultType.FAIL) {
                    dropEffect(player, slot.equals("1") ? "2" : "1");
                }
            }
            case "prefer_1" -> {
                result = dropEffect(player, "1");
                if (result.type == EquipResultType.FAIL) {
                    dropEffect(player, "2");
                }
            }
            case "prefer_2" -> {
                result = dropEffect(player, "2");
                if (result.type == EquipResultType.FAIL) {
                    dropEffect(player, "1");
                }
            }
            case "only_1" -> dropEffect(player, "1");
            case "only_2" -> dropEffect(player, "2");
            case "both" -> {
                dropEffect(player, "1");
                dropEffect(player, "2");
            }
            case "none" -> {}
        }
    }

    public void handleJoin(ServerPlayerEntity player) {
        // Giving the player their starting effects if they haven't joined before
        // Since Fabric doesn't have hasPlayedBefore() directly on ServerPlayerEntity, we check if they have any saved data or we can default it.
        // Let's check if their UUID exists in playerdata config or not.
        boolean firstJoin = !plugin.getDataManager().hasAnyData(player.getUuid());

        if (plugin.getMainConfig().joinEffectsEnabled() && firstJoin) {
            List<InfuseEffect> effects = plugin.getMainConfig().joinEffects();
            if (!effects.isEmpty()) {
                InfuseEffect effect = effects.get((int) (Math.random() * effects.size()));
                equipEffect(player, effect, "1", false);
                return;
            }
        }

        // Enabling each effect
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUuid(), "1");
        if (effect != null) effect.equip(player);

        effect = plugin.getDataManager().getEffect(player.getUuid(), "2");
        if (effect != null) effect.equip(player);
    }

    public void handleQuit(ServerPlayerEntity player) {
        // Deactivating the player's effects
        InfuseEffect effect = plugin.getDataManager().getEffect(player.getUuid(), "1");
        if (effect != null) effect.unequip(player);

        effect = plugin.getDataManager().getEffect(player.getUuid(), "2");
        if (effect != null) effect.unequip(player);
    }

    public record EquipResult(EquipResultType type, @Nullable InfuseEffect effect) {
        public EquipResult(EquipResultType type) {
            this(type, null);
        }
    }

    public enum EquipResultType {
        FAIL,
        CANCELLED,
        SUCCESS
    }
}
