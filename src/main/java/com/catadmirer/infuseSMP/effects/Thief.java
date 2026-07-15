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
}
