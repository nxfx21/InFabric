package com.catadmirer.infuseSMP.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

public interface EntityTrackerAccessor {
    void updateTrackedStatus(List<ServerPlayerEntity> players);
}
