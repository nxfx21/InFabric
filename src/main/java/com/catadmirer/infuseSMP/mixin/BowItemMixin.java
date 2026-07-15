package com.catadmirer.infuseSMP.mixin;

import com.catadmirer.infuseSMP.Infuse;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BowItem.class)
public class BowItemMixin {
    @org.spongepowered.asm.mixin.injection.ModifyVariable(method = "onStoppedUsing", at = @At("HEAD"), argsOnly = true)
    private int modifyRemainingUseTicks(int remainingUseTicks, ItemStack stack, World world, LivingEntity user) {
        com.catadmirer.infuseSMP.effects.InfuseEffect speedEffect = com.catadmirer.infuseSMP.effects.InfuseEffect.fromString("speed");
        if (user instanceof ServerPlayerEntity player && speedEffect != null && Infuse.getInstance().getDataManager().hasEffect(player.getUuid(), speedEffect)) {
            int maxUseTime = ((BowItem)(Object)this).getMaxUseTime(stack, user);
            int useTime = maxUseTime - remainingUseTicks;
            int acceleratedUseTime = (int) (useTime * 1.8);
            return Math.max(0, maxUseTime - acceleratedUseTime);
        }
        return remainingUseTicks;
    }
}
