package com.catadmirer.infuseSMP.effects;

import com.catadmirer.infuseSMP.EffectConstants;
import com.catadmirer.infuseSMP.EffectIds;
import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.Message.MessageType;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Frost extends InfuseEffect {
    private static final Map<BlockPos, FrostBlockEntry> CHANGED_BLOCKS = new HashMap<>();

    private final Infuse plugin;

    private record FrostBlockEntry(UUID playerUUID, int radius) {}

    public Frost() {
        this(false);
    }

    public Frost(boolean augmented) {
        super("frost", EffectIds.FROST, augmented, EffectConstants.potionColor(EffectIds.FROST), EffectConstants.ritualColor(EffectIds.FROST));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {
        changeToSnow(owner);
    }

    @Override
    public void unequip(ServerPlayerEntity owner) {}

    @Override
    public void applyPassives(ServerPlayerEntity owner) {
        if (owner.getWorld().getBlockState(owner.getBlockPos().down()).isOf(net.minecraft.block.Blocks.ICE)) {
            owner.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 30, 2, false, false));
        }

        if (!owner.getWorld().getBlockState(owner.getBlockPos()).isOf(Blocks.POWDER_SNOW)) {
            changeToSnow(owner);
        }
        revertBlocks(owner.getServerWorld());
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "frost")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "frost", duration, cooldown);
        
        for (net.minecraft.entity.Entity entity : owner.getWorld().getOtherEntities(owner, owner.getBoundingBox().expand(10))) {
            if (entity instanceof LivingEntity living) {
                if (entity instanceof ServerPlayerEntity nearby && plugin.getDataManager().isTrusted(nearby.getUuid(), owner.getUuid())) continue;
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, (int)(duration * 20), 2, false, false));
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, (int)(duration * 20), 2, false, false));
                living.setFrozenTicks(200);
            }
        }
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Frost();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Frost(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_FROST_NAME : MessageType.FROST_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_FROST_LORE : MessageType.FROST_LORE);
    }

    public static void onPlayerJoin(ServerPlayerEntity player) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect frostEffect = InfuseEffect.fromString("frost");
        if (frostEffect == null || !plugin.getDataManager().hasEffect(player.getUuid(), frostEffect)) return;
        changeToSnow(player);
    }

    public static void changeToSnow(ServerPlayerEntity player) {
        int radius = 3;
        BlockPos center = player.getBlockPos();
        net.minecraft.world.World world = player.getWorld();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    if (!world.getBlockState(pos).isOf(Blocks.POWDER_SNOW)) continue;
                    if (!world.getBlockState(pos.up()).isAir()) continue;

                    world.setBlockState(pos, Blocks.SNOW_BLOCK.getDefaultState());
                    CHANGED_BLOCKS.put(pos, new FrostBlockEntry(player.getUuid(), radius));
                }
            }
        }
    }

    private static void revertBlocks(net.minecraft.server.world.ServerWorld world) {
        CHANGED_BLOCKS.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            FrostBlockEntry blockEntry = entry.getValue();

            if (!world.getBlockState(pos).isOf(Blocks.SNOW_BLOCK)) {
                return true;
            }

            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(blockEntry.playerUUID());
            if (player == null || player.getWorld() != world) {
                world.setBlockState(pos, Blocks.POWDER_SNOW.getDefaultState());
                return true;
            }

            double distance = player.getBlockPos().getManhattanDistance(pos);
            if (distance > blockEntry.radius()) {
                world.setBlockState(pos, Blocks.POWDER_SNOW.getDefaultState());
                return true;
            }

            return false;
        });
    }

    public static void onTenHit(ServerPlayerEntity attacker, ServerPlayerEntity target) {
        Infuse plugin = Infuse.getInstance();
        InfuseEffect frostEffect = InfuseEffect.fromString("frost");
        if (frostEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), frostEffect)) return;

        target.setFrozenTicks(200);
    }
}
