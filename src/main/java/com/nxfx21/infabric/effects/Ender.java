package com.nxfx21.infabric.effects;

import com.nxfx21.infabric.EffectConstants;
import com.nxfx21.infabric.EffectIds;
import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.managers.CooldownManager;
import com.nxfx21.infabric.GlobalLoop;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import java.util.UUID;

public class Ender extends InfuseEffect {
    private final Infuse plugin;
    public static final java.util.Set<UUID> cursedPlayers = GlobalLoop.cursedPlayers;

    public Ender() {
        this(false);
    }

    public Ender(boolean augmented) {
        super("ender", EffectIds.ENDER, augmented, EffectConstants.potionColor(EffectIds.ENDER), EffectConstants.ritualColor(EffectIds.ENDER));
        this.plugin = Infuse.getInstance();
    }

    @Override
    public void equip(ServerPlayerEntity owner) {}

    @Override
    public void unequip(ServerPlayerEntity owner) {}

    @Override
    public void applyPassives(ServerPlayerEntity owner) {
        ServerWorld world = owner.getServerWorld();
        double radius = plugin.getMainConfig().enderPassiveRadius();
        for (Entity entity : world.getOtherEntities(owner, owner.getBoundingBox().expand(radius))) {
            if (entity instanceof ServerPlayerEntity nearby) {
                if (plugin.getDataManager().isTrusted(nearby.getUuid(), owner.getUuid())) continue;
                nearby.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 40, 1, false, false));
            }
        }
    }

    @Override
    public void activateSpark(ServerPlayerEntity owner) {
        UUID playerUUID = owner.getUuid();
        if (CooldownManager.isOnCooldown(playerUUID, "ender")) return;

        owner.getWorld().playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.BLOCK_BEACON_POWER_SELECT, SoundCategory.PLAYERS, 1, 1);

        Vec3d startPos = owner.getEyePos();
        Vec3d direction = owner.getRotationVector().normalize();
        int maxDistance = plugin.getMainConfig().enderSparkMaxDistance();

        Vec3d targetPos = null;
        for (int i = 1; i <= maxDistance; i++) {
            Vec3d checkPos = startPos.add(direction.multiply(i));
            if (isSafeTeleportLocation(owner.getServerWorld(), checkPos)) {
                targetPos = checkPos;
            } else {
                break;
            }
        }

        if (targetPos != null) {
            owner.requestTeleport(targetPos.x, targetPos.y, targetPos.z);
        }

        long cooldown = plugin.getMainConfig().cooldown(this);
        long duration = plugin.getMainConfig().duration(this);

        CooldownManager.setTimes(playerUUID, "ender", duration, cooldown);
    }

    public static boolean isSafeTeleportLocation(ServerWorld world, Vec3d pos) {
        if (world == null || pos == null) return false;
        BlockPos blockPos = BlockPos.ofFloored(pos);
        if (blockPos.getY() < world.getBottomY() || blockPos.getY() >= world.getTopYInclusive()) return false;

        BlockPos groundPos = blockPos.down();
        if (groundPos.getY() < world.getBottomY()) return false;

        net.minecraft.block.BlockState groundState = world.getBlockState(groundPos);
        net.minecraft.block.BlockState bodyState = world.getBlockState(blockPos);
        net.minecraft.block.BlockState headState = world.getBlockState(blockPos.up());

        // Prevent void and lava
        if (groundState.isAir() || groundState.isOf(net.minecraft.block.Blocks.VOID_AIR)) return false;
        if (groundState.isOf(net.minecraft.block.Blocks.LAVA) || groundState.getFluidState().isIn(net.minecraft.registry.tag.FluidTags.LAVA)) return false;
        if (bodyState.isOf(net.minecraft.block.Blocks.LAVA) || bodyState.getFluidState().isIn(net.minecraft.registry.tag.FluidTags.LAVA)) return false;
        if (headState.isOf(net.minecraft.block.Blocks.LAVA) || headState.getFluidState().isIn(net.minecraft.registry.tag.FluidTags.LAVA)) return false;

        // Check body and head space
        boolean bodySafe = bodyState.isAir() || (bodyState.getFluidState().isEmpty() && bodyState.getCollisionShape(world, blockPos).isEmpty());
        boolean headSafe = headState.isAir() || (headState.getFluidState().isEmpty() && headState.getCollisionShape(world, blockPos.up()).isEmpty());

        // Check solid ground below target
        boolean groundSolid = (groundState.isOpaque() || groundState.isFullCube(world, groundPos)) && !groundState.isOf(net.minecraft.block.Blocks.LAVA);

        return bodySafe && headSafe && groundSolid;
    }

    public static void onAttack(ServerPlayerEntity attacker, net.minecraft.entity.LivingEntity target) {
        if (attacker == null || target == null) return;
        Infuse plugin = Infuse.getInstance();
        if (plugin == null || plugin.getDataManager() == null || plugin.getMainConfig() == null) return;
        InfuseEffect enderEffect = InfuseEffect.fromString("ender");
        if (enderEffect == null || !plugin.getDataManager().hasEffect(attacker.getUuid(), enderEffect)) return;

        // 1-Hit Kill Endermobs
        if (target instanceof EndermanEntity || target instanceof EndermiteEntity) {
            if (plugin.getMainConfig().enderOnehitMobs()) {
                target.setHealth(0.0f);
                return;
            }
        }

        // Ender Spark 1-Hit Kill non-player mobs
        if (!(target instanceof ServerPlayerEntity) && CooldownManager.isEffectActive(attacker.getUuid(), "ender")) {
            target.setHealth(0.0f);
        }

        // Ender Curse on Hit
        if (target instanceof ServerPlayerEntity victim && plugin.getMainConfig().enderCurseHit()) {
            applyEnderCurse(attacker, victim);
        }
    }

    public static void applyEnderCurse(ServerPlayerEntity attacker, ServerPlayerEntity victim) {
        if (attacker == null || victim == null) return;
        Infuse plugin = Infuse.getInstance();
        if (plugin == null || plugin.getMainConfig() == null || !plugin.getMainConfig().enderCurseHit()) return;

        GlobalLoop.cursedPlayers.add(victim.getUuid());

        if (plugin.getHitTracker() != null) {
            plugin.getHitTracker().scheduleTask(1200L, () -> {
                GlobalLoop.cursedPlayers.remove(victim.getUuid());
            });
        }
    }

    public static void onPlayerDamage(ServerPlayerEntity player, net.minecraft.entity.damage.DamageSource source, float amount) {
        if (player == null || source == null) return;
        if (source.isOf(net.minecraft.entity.damage.DamageTypes.CAMPFIRE)) return;

        if (GlobalLoop.cursedPlayers.contains(player.getUuid())) {
            net.minecraft.entity.damage.DamageSource fakeSource = player.getWorld().getDamageSources().create(net.minecraft.entity.damage.DamageTypes.CAMPFIRE, player);
            for (UUID cursedUuid : GlobalLoop.cursedPlayers) {
                if (cursedUuid.equals(player.getUuid())) continue;
                if (player.getServer() != null) {
                    ServerPlayerEntity other = player.getServer().getPlayerManager().getPlayer(cursedUuid);
                    if (other != null && other.isAlive()) {
                        other.damage(player.getServerWorld(), fakeSource, amount);
                    }
                }
            }
        }
    }

    public static ActionResult onUseDragonBreath(ServerPlayerEntity player, ItemStack stack, Hand hand) {
        if (player == null || stack == null) return ActionResult.PASS;
        Infuse plugin = Infuse.getInstance();
        if (plugin == null || plugin.getDataManager() == null) return ActionResult.PASS;
        InfuseEffect enderEffect = InfuseEffect.fromString("ender");
        if (enderEffect == null || !plugin.getDataManager().hasEffect(player.getUuid(), enderEffect)) return ActionResult.PASS;
        if (!stack.isOf(Items.DRAGON_BREATH)) return ActionResult.PASS;

        if (CooldownManager.isOnCooldown(player.getUuid(), "ender_fireball")) {
            return ActionResult.PASS;
        }

        ServerWorld world = player.getServerWorld();
        if (world == null) return ActionResult.PASS;
        Vec3d look = player.getRotationVector();

        DragonFireballEntity fireball = new DragonFireballEntity(world, player, look);
        fireball.setPosition(player.getX() + look.x * 1.5, player.getEyeY() + look.y * 1.5, player.getZ() + look.z * 1.5);
        fireball.setOwner(player);
        fireball.setVelocity(look.multiply(1.5));
        world.spawnEntity(fireball);

        if (!player.isCreative()) {
            stack.decrement(1);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        CooldownManager.setCooldown(player.getUuid(), "ender_fireball", 30);

        return ActionResult.SUCCESS;
    }

    public static void registerEvents() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) return net.minecraft.util.ActionResult.PASS;
            if (player instanceof ServerPlayerEntity serverPlayer) {
                ItemStack stack = player.getStackInHand(hand);
                if (stack.isOf(Items.DRAGON_BREATH)) {
                    ActionResult result = onUseDragonBreath(serverPlayer, stack, hand);
                    if (result == ActionResult.SUCCESS) {
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    @Override
    public InfuseEffect getRegularVersion() {
        return new Ender();
    }

    @Override
    public InfuseEffect getAugmentedVersion() {
        return new Ender(true);
    }

    @Override
    public Message getName() {
        return new Message(augmented ? MessageType.AUG_ENDER_NAME : MessageType.ENDER_NAME);
    }

    @Override
    public Message getLore() {
        return new Message(augmented ? MessageType.AUG_ENDER_LORE : MessageType.ENDER_LORE);
    }
}
