package com.nxfx21.infabric.managers;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.effects.InfuseEffect;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class Drop {

    public Drop() {
    }

    public void registerEvents() {
        // Events are registered via Mixins
    }

    public void onPickup(ItemEntity itemEntity, ServerPlayerEntity player) {
        ItemStack item = itemEntity.getStack();
        InfuseEffect effect = InfuseEffect.fromItem(item);
        if (effect == null) return;
        this.playDustEffect(true, effect, itemEntity.getPos(), (ServerWorld) itemEntity.getWorld());
    }

    public void onDrop(ItemEntity droppedItem) {
        ItemStack itemStack = droppedItem.getStack();
        InfuseEffect effect = InfuseEffect.fromItem(itemStack);
        if (effect == null) return;
        this.playDustEffect(false, effect, droppedItem.getPos(), (ServerWorld) droppedItem.getWorld());
        droppedItem.setGlowing(true);
    }

    private void playDustEffect(final boolean bottomToTop, @NotNull InfuseEffect effect, Vec3d pos, ServerWorld world) {
        int color = effect.getPotionColor().getRGB();
        DustParticleEffect dust = new DustParticleEffect(color, 0.7F);
        int points = 16;
        double radius = 0.6;

        // Spawn a cylinder of particles
        for (double yOffset = 0; yOffset <= 2.0; yOffset += 0.15) {
            double ringY = bottomToTop ? yOffset : 2.0 - yOffset;
            for (int i = 0; i < points; ++i) {
                double angle = Math.PI * 2 * i / points;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                world.spawnParticles(dust, pos.getX() + x, pos.getY() + ringY, pos.getZ() + z, 1, 0, 0, 0, 0);
            }
        }
        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_TURTLE_EGG_BREAK, SoundCategory.PLAYERS, 1.3F, 1.2F);
    }
}
