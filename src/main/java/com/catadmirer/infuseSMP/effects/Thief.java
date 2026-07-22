package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import java.util.UUID;

public class Thief extends InfuseEffect {
    private final Infuse plugin;

    public Thief() {
        this(false);
    }

    public Thief(boolean augmented) {
        super("thief", EffectIds.THIEF, augmented, EffectConstants.potionColor(EffectIds.THIEF), EffectConstants.ritualColor(EffectIds.THIEF));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {}

    @Override
    public void unequip(ServerPlayerEntity owner) {}

    @Override
    public void applyPassives(ServerPlayerEntity owner) {}

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "thief")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "thief", duration, cooldown);
        
        for (net.minecraft.entity.Entity entity : owner.getWorld().getOtherEntities(owner, owner.getBoundingBox().expand(10))) {
            if (entity instanceof ServerPlayerEntity victim) {
                if (plugin.getDataManager().isTrusted(victim.getUuid(), owner.getUuid())) continue;
                
                InfuseEffect effect1 = plugin.getDataManager().getEffect(victim.getUuid(), "1");
                InfuseEffect effect2 = plugin.getDataManager().getEffect(victim.getUuid(), "2");
                
                InfuseEffect stolen = effect1 != null ? effect1 : effect2;
                if (stolen != null) {
                    plugin.getDataManager().setEffect(victim.getUuid(), effect1 != null ? "1" : "2", null);
                    plugin.getDataManager().setEffect(owner.getUuid(), "1", stolen); // For now just set to slot 1
                    owner.sendMessage(net.minecraft.text.Text.literal("Stole " + stolen.getKey() + " from " + victim.getName().getString()), true);
                    victim.sendMessage(net.minecraft.text.Text.literal("Your " + stolen.getKey() + " was stolen!"), true);
                    break;
                }
            }
        }
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Thief();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Thief(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_THIEF_NAME : MessageType.THIEF_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_THIEF_LORE : MessageType.THIEF_LORE);
    }

    public static void onPlayerHit(ServerPlayerEntity attacker, ServerPlayerEntity victim) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect thiefEffect = InfuseEffect.fromString("thief");
        if (thiefEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), thiefEffect)) return;

        if (plugin.getDataManager().isTrusted(victim.getUuid(), attacker.getUuid())) return;

        // Hotbar item stealing (slots 0-8)
        java.util.List<Integer> validSlots = new java.util.ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (!victim.getInventory().getStack(i).isEmpty()) {
                validSlots.add(i);
            }
        }
        if (!validSlots.isEmpty()) {
            int chosenSlot = validSlots.get(net.minecraft.util.math.random.Random.create().nextInt(validSlots.size()));
            net.minecraft.item.ItemStack stolenStack = victim.getInventory().getStack(chosenSlot);
            victim.getInventory().setStack(chosenSlot, net.minecraft.item.ItemStack.EMPTY);
            if (!attacker.getInventory().insertStack(stolenStack)) {
                attacker.dropItem(stolenStack, false);
            }
        }

        // Effect stealing when Thief spark is active
        if (CooldownManager.isEffectActive(attacker.getUuid(), "thief")) {
            InfuseEffect effect1 = plugin.getDataManager().getEffect(victim.getUuid(), "1");
            InfuseEffect effect2 = plugin.getDataManager().getEffect(victim.getUuid(), "2");

            InfuseEffect stolen = null;
            String slotStolenFrom = null;
            if (effect1 != null && effect2 != null) {
                if (Math.random() > 0.5) {
                    stolen = effect1;
                    slotStolenFrom = "1";
                } else {
                    stolen = effect2;
                    slotStolenFrom = "2";
                }
            } else if (effect1 != null) {
                stolen = effect1;
                slotStolenFrom = "1";
            } else if (effect2 != null) {
                stolen = effect2;
                slotStolenFrom = "2";
            }

            if (stolen != null) {
                plugin.getDataManager().removeEffect(victim.getUuid(), slotStolenFrom);
                stolen.activateSpark(attacker);
                attacker.sendMessage(net.minecraft.text.Text.literal("Stole and activated " + stolen.getKey() + " spark from " + victim.getName().getString() + "!"), true);
                victim.sendMessage(net.minecraft.text.Text.literal("Your " + stolen.getKey() + " effect was stolen by " + attacker.getName().getString() + "!"), true);
                CooldownManager.setDuration(attacker.getUuid(), "thief", 0);
            }
        }
    }

    public static void onAttack(ServerPlayerEntity attacker, net.minecraft.entity.LivingEntity target) {
        if (target instanceof ServerPlayerEntity victim) {
            onPlayerHit(attacker, victim);
        }
    }
}
