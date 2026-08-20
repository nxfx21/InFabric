package com.nxfx21.infabric.managers;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.effects.InfuseEffect;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EffectCraftManager {

    private final Infuse plugin;
    private static ServerBossBar ritualBossBar;
    private static EndCrystalEntity ritualBeam;
    private static final Set<BlockPos> lockedBrewingStands = Collections.synchronizedSet(new HashSet<>());
    private static ScheduledExecutorService ritualScheduler;

    public EffectCraftManager() {
        this.plugin = Infuse.getInstance();
    }

    public void registerEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;
            BlockPos pos = hitResult.getBlockPos();
            if (!world.getBlockState(pos).isOf(Blocks.BREWING_STAND)) {
                return ActionResult.PASS;
            }

            if (lockedBrewingStands.contains(pos)) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.sendMessage(Text.literal("This brewing stand is currently locked in an infusion ritual!"), true);
                }
                return ActionResult.FAIL;
            }

            if (player instanceof ServerPlayerEntity serverPlayer) {
                if (plugin != null && plugin.getMainConfig() != null && plugin.getMainConfig().brewingGui()) {
                    openStationSelectionGui(serverPlayer, pos);
                } else {
                    openEffectCraftingMenu(serverPlayer, pos);
                }
                return ActionResult.SUCCESS;
            }

            return ActionResult.PASS;
        });

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (state.isOf(Blocks.BREWING_STAND) && lockedBrewingStands.contains(pos)) {
                if (player instanceof ServerPlayerEntity serverPlayer) {
                    serverPlayer.sendMessage(Text.literal("Cannot break a brewing stand performing a ritual!"), true);
                }
                return false;
            }
            return true;
        });
    }

    public void openStationSelectionGui(ServerPlayerEntity player, BlockPos pos) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X3, player, false);
        gui.setTitle(Text.literal("Brewing Stand Mode"));

        gui.setSlot(11, new GuiElementBuilder(Items.CRAFTING_TABLE)
                .setName(Text.literal("§aEffect Crafting"))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openEffectCraftingMenu(player, pos);
                }));

        gui.setSlot(15, new GuiElementBuilder(Items.BREWING_STAND)
                .setName(Text.literal("§eVanilla Brewing"))
                .setCallback((index, type, action) -> {
                    gui.close();
                    openStandardBrewingStand(player, pos);
                }));

        gui.open();
    }

    public void openEffectCraftingMenu(ServerPlayerEntity player, BlockPos pos) {
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, inv, p) -> new CraftingScreenHandler(syncId, inv, ScreenHandlerContext.create(p.getWorld(), pos)),
                Text.literal("Effect Crafting")
        ));
    }

    public void openStandardBrewingStand(ServerPlayerEntity player, BlockPos pos) {
        if (player.getWorld().getBlockEntity(pos) instanceof net.minecraft.block.entity.BrewingStandBlockEntity brewingStand) {
            player.openHandledScreen(brewingStand);
        }
    }

    public static boolean handleCraftEffect(ServerPlayerEntity player, InfuseEffect effect, BlockPos brewerPos) {
        if (player == null || effect == null) return false;
        Infuse pluginInstance = Infuse.getInstance();
        if (pluginInstance == null || pluginInstance.getMainConfig() == null || pluginInstance.getDataManager() == null) return false;

        int craftLimit = pluginInstance.getMainConfig().getCraftLimit(effect);
        int numCrafted = pluginInstance.getDataManager().getExistingCount(effect);
        if (numCrafted >= craftLimit) {
            player.sendMessage(Text.literal("The maximum limit for " + effect.getName().toString() + " has been reached!"), false);
            return false;
        }

        pluginInstance.getDataManager().setExistingCount(effect, numCrafted + 1);

        if (!effect.isAugmented()) {
            if (pluginInstance.getMainConfig().regularBroadcast()) {
                Message msg = new Message(MessageType.REGULAR_BROADCAST);
                msg.applyPlaceholder("player", player.getName().getString());
                msg.applyPlaceholder("item", effect.getName().toString());
                if (brewerPos != null) {
                    msg.applyPlaceholder("x", brewerPos.getX());
                    msg.applyPlaceholder("y", brewerPos.getY());
                    msg.applyPlaceholder("z", brewerPos.getZ());
                }
                player.getServer().getPlayerManager().broadcast(msg.toComponent(), false);
            }
            return true;
        }

        if (ritualBossBar != null) {
            player.sendMessage(new Message(MessageType.ERROR_RITUAL_ACTIVE).toComponent(), false);
            return false;
        }

        startRitual(player, effect, brewerPos);
        return true;
    }

    private static void startRitual(ServerPlayerEntity player, InfuseEffect effect, BlockPos brewerPos) {
        Infuse pluginInstance = Infuse.getInstance();
        ServerWorld world = player.getServerWorld();

        ritualBossBar = new ServerBossBar(
                Text.literal("🧪 " + effect.getName().toString() + " 🧪"),
                BossBar.Color.PURPLE,
                BossBar.Style.PROGRESS
        );

        for (ServerPlayerEntity p : player.getServer().getPlayerManager().getPlayerList()) {
            ritualBossBar.addPlayer(p);
        }

        int ritualDuration = effect.getKey().equals("ender") ? 
                pluginInstance.getMainConfig().ritualDurationEnder() : pluginInstance.getMainConfig().ritualDuration();

        if (brewerPos != null && pluginInstance.getMainConfig().ritualBeacon()) {
            lockedBrewingStands.add(brewerPos);
            try {
                ritualBeam = new EndCrystalEntity(EntityType.END_CRYSTAL, world);
                ritualBeam.setPosition(brewerPos.getX() + 0.5, brewerPos.getY(), brewerPos.getZ() + 0.5);
                ritualBeam.setShowBottom(false);
                ritualBeam.setInvulnerable(true);
                ritualBeam.setInvisible(true);
                ritualBeam.setBeamTarget(new BlockPos(brewerPos.getX(), 500, brewerPos.getZ()));
                world.spawnEntity(ritualBeam);
            } catch (Throwable t) {
                Infuse.LOGGER.error("Failed to spawn ritual beam crystal", t);
            }
        }

        if (pluginInstance.getRecipeManager() != null) {
            pluginInstance.getRecipeManager().updateEnderRecipe();
        }

        Message msg = new Message(MessageType.EFFECT_BROADCAST);
        msg.applyPlaceholder("player", player.getName().getString());
        msg.applyPlaceholder("item", effect.getName().toString());
        if (brewerPos != null) {
            msg.applyPlaceholder("x", brewerPos.getX());
            msg.applyPlaceholder("y", brewerPos.getY());
            msg.applyPlaceholder("z", brewerPos.getZ());
        }
        player.getServer().getPlayerManager().broadcast(msg.toComponent(), false);

        if (ritualScheduler == null || ritualScheduler.isShutdown()) {
            ritualScheduler = Executors.newSingleThreadScheduledExecutor();
        }

        final int totalTicks = Math.max(1, ritualDuration * 20);
        final long startTime = System.currentTimeMillis();
        final long durationMs = totalTicks * 50L;

        ritualScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - startTime;
                float progress = Math.max(0.0f, 1.0f - ((float) elapsed / durationMs));
                if (ritualBossBar != null) {
                    ritualBossBar.setPercent(progress);
                }

                if (elapsed >= durationMs) {
                    finishRitual(player, effect, brewerPos);
                    throw new RuntimeException("Ritual Complete"); // End scheduled task
                }
            }
        }, 0, 50, TimeUnit.MILLISECONDS);
    }

    private static void finishRitual(ServerPlayerEntity player, InfuseEffect effect, BlockPos brewerPos) {
        if (ritualBossBar != null) {
            ritualBossBar.clearPlayers();
            ritualBossBar = null;
        }

        if (brewerPos != null) {
            lockedBrewingStands.remove(brewerPos);
        }

        removeBeam();

        if (player != null && player.getServer() != null) {
            Message msg = new Message(MessageType.EFFECT_FINISHED);
            msg.applyPlaceholder("item", effect.getName().toString());
            player.getServer().getPlayerManager().broadcast(msg.toComponent(), false);

            if (brewerPos != null && player.getServerWorld() != null) {
                ItemStack item = effect.createItem();
                net.minecraft.entity.ItemEntity entity = new net.minecraft.entity.ItemEntity(
                        player.getServerWorld(),
                        brewerPos.getX() + 0.5, brewerPos.getY() + 1.0, brewerPos.getZ() + 0.5,
                        item
                );
                player.getServerWorld().spawnEntity(entity);
            }
        }
    }

    public static void removeBeam() {
        if (ritualBeam != null) {
            try {
                ritualBeam.discard();
            } catch (Throwable ignored) {}
            ritualBeam = null;
        }
    }
}
