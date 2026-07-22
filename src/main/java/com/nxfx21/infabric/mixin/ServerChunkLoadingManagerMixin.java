package com.nxfx21.infabric.mixin;

import com.nxfx21.infabric.util.ChunkLoadingManagerHelper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin implements ChunkLoadingManagerHelper {
    @Shadow
    @Final
    private Int2ObjectMap<Object> entityTrackers;

    @Override
    public void forceUpdateTracking(ServerPlayerEntity player) {
        Object tracker = entityTrackers.get(player.getId());
        if (tracker instanceof EntityTrackerAccessor accessor) {
            List<ServerPlayerEntity> players = player.getServerWorld().getPlayers();
            accessor.updateTrackedStatus(players);
        }
    }
}
