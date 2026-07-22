package com.nxfx21.infabric;

import com.nxfx21.infabric.managers.DataManager;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.UUID;

public class PlayerSwapHandItemsListener {
    private final DataManager dataManager;

    public PlayerSwapHandItemsListener() {
        this.dataManager = Infuse.getInstance().getDataManager();
    }

    // Call this from a Mixin in ServerPlayNetworkHandler handling PlayerActionC2SPacket for SWAP_ITEM_WITH_OFFHAND
    public boolean onPlayerSwapHandItems(ServerPlayerEntity player) {
        UUID playerUUID = player.getUuid();
        String data = dataManager.getControlMode(playerUUID);
        if ("offhand".equals(data)) {
            com.nxfx21.infabric.effects.InfuseEffect lEffect = dataManager.getEffect(player.getUuid(), "1");
            com.nxfx21.infabric.effects.InfuseEffect rEffect = dataManager.getEffect(player.getUuid(), "2");

            if (player.isSneaking()) {
                if (rEffect != null) {
                    rEffect.activateSpark(player);
                    return true;
                }
            } else {
                if (lEffect != null) {
                    lEffect.activateSpark(player);
                    return true;
                }
            }
        }
        return false;
    }
}
