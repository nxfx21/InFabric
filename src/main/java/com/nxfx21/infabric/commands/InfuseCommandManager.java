package com.nxfx21.infabric.commands;

import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.managers.CooldownManager;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class InfuseCommandManager {
    private final Infuse plugin;

    public InfuseCommandManager() {
        this.plugin = Infuse.getInstance();
    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerInfuseCommand(dispatcher);
            registerAbilitiesCommand(dispatcher);
            registerClearEffectsCommand(dispatcher);
            registerDrainCommand(dispatcher);
            registerSwapEffectsCommand(dispatcher);
            registerTrustCommand(dispatcher);
            registerUninfuseCommand(dispatcher);
        });
    }

    private void registerInfuseCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("infuse")
            .then(literal("reload")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    plugin.getMainConfig().load();
                    plugin.getRecipeManager().reload();
                    context.getSource().sendMessage(Text.literal("Infuse configs reloaded"));
                    return 1;
                })
            )
            .then(literal("gui")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) return 0;
                    new com.nxfx21.infabric.inventories.EffectChooser(context.getSource().getPlayer()).open();
                    return 1;
                })
            )
            .then(literal("recipes")
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) return 0;
                    new com.nxfx21.infabric.inventories.RecipeListGUI(context.getSource().getPlayer()).open();
                    return 1;
                })
            )
            .then(literal("giveeffect")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("target", EntityArgumentType.player())
                    .then(argument("effect", StringArgumentType.string())
                        .executes(context -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                            String effectKey = StringArgumentType.getString(context, "effect");
                            com.nxfx21.infabric.effects.InfuseEffect mapping = com.nxfx21.infabric.effects.InfuseEffect.fromString(effectKey);
                            if (mapping == null) {
                                context.getSource().sendMessage(Message.toComponent(Message.MessageType.INFUSE_INVALID_PARAM.defaultValue));
                                return 0;
                            }
                            target.getInventory().insertStack(mapping.createItem());
                            context.getSource().sendMessage(Text.literal("Gave effect."));
                            return 1;
                        })
                    )
                )
            )
            .then(literal("cooldown")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("target", EntityArgumentType.player())
                    .executes(context -> {
                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                        CooldownManager.removeAllCooldowns(target.getUuid());
                        context.getSource().sendMessage(Text.literal("Removed cooldowns."));
                        return 1;
                    })
                )
            )
        );
    }

    private void registerAbilitiesCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("abilities")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                    context.getSource().sendMessage(Text.literal("This command must be executed by a player."));
                    return 0;
                }
                ServerPlayerEntity player = context.getSource().getPlayer();
                com.nxfx21.infabric.effects.InfuseEffect effect1 = plugin.getDataManager().getEffect(player.getUuid(), "1");
                com.nxfx21.infabric.effects.InfuseEffect effect2 = plugin.getDataManager().getEffect(player.getUuid(), "2");

                context.getSource().sendMessage(Text.literal("=== Your Infuse Effects & Abilities ==="));
                if (effect1 != null) {
                    long cd1 = CooldownManager.getCooldownTimeLeft(player.getUuid(), effect1.getKey()) / 1000L;
                    context.getSource().sendMessage(Text.literal("Slot 1: " + effect1.getKey() + (cd1 > 0 ? " (Cooldown: " + cd1 + "s)" : " (Ready)")));
                } else {
                    context.getSource().sendMessage(Text.literal("Slot 1: None"));
                }

                if (effect2 != null) {
                    long cd2 = CooldownManager.getCooldownTimeLeft(player.getUuid(), effect2.getKey()) / 1000L;
                    context.getSource().sendMessage(Text.literal("Slot 2: " + effect2.getKey() + (cd2 > 0 ? " (Cooldown: " + cd2 + "s)" : " (Ready)")));
                } else {
                    context.getSource().sendMessage(Text.literal("Slot 2: None"));
                }
                return 1;
            })
            .then(argument("slot", StringArgumentType.word())
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(Text.literal("This command must be executed by a player."));
                        return 0;
                    }
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    String slot = StringArgumentType.getString(context, "slot");
                    if (!slot.equals("1") && !slot.equals("2")) {
                        player.sendMessage(Text.literal("Invalid slot. Use 1 or 2."));
                        return 0;
                    }
                    com.nxfx21.infabric.effects.InfuseEffect effect = plugin.getDataManager().getEffect(player.getUuid(), slot);
                    if (effect != null) {
                        effect.activateSpark(player);
                        return 1;
                    } else {
                        player.sendMessage(Text.literal("No effect equipped in slot " + slot));
                        return 0;
                    }
                })
            )
        );
    }

    private void registerClearEffectsCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("cleareffects")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) return 0;
                ServerPlayerEntity player = context.getSource().getPlayer();
                plugin.getDataManager().removeEffect(player.getUuid(), "1");
                plugin.getDataManager().removeEffect(player.getUuid(), "2");
                player.sendMessage(Text.literal("Cleared your effects."));
                return 1;
            })
            .then(argument("target", EntityArgumentType.player())
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                    plugin.getDataManager().removeEffect(target.getUuid(), "1");
                    plugin.getDataManager().removeEffect(target.getUuid(), "2");
                    context.getSource().sendMessage(Text.literal("Cleared " + target.getName().getString() + "'s effects."));
                    return 1;
                })
            )
        );
    }

    private void registerDrainCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("drain")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                    context.getSource().sendMessage(Text.literal("This command must be executed by a player."));
                    return 0;
                }
                ServerPlayerEntity player = context.getSource().getPlayer();
                boolean drained1 = plugin.getEffectManager().drainEffect(player, "1").type() == com.nxfx21.infabric.managers.EffectManager.EquipResultType.SUCCESS;
                boolean drained2 = plugin.getEffectManager().drainEffect(player, "2").type() == com.nxfx21.infabric.managers.EffectManager.EquipResultType.SUCCESS;
                if (!drained1 && !drained2) {
                    player.sendMessage(Text.literal("You have no equipped effects to drain."));
                }
                return 1;
            })
            .then(argument("slot", StringArgumentType.word())
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(Text.literal("This command must be executed by a player."));
                        return 0;
                    }
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    String slot = StringArgumentType.getString(context, "slot");
                    if (!slot.equals("1") && !slot.equals("2")) {
                        player.sendMessage(Text.literal("Invalid slot. Use 1 or 2."));
                        return 0;
                    }
                    plugin.getEffectManager().drainEffect(player, slot);
                    return 1;
                })
            )
        );
    }

    private void registerSwapEffectsCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("swapeffects")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                    context.getSource().sendMessage(Text.literal("This command must be executed by a player."));
                    return 0;
                }
                ServerPlayerEntity player = context.getSource().getPlayer();
                com.nxfx21.infabric.effects.InfuseEffect effect1 = plugin.getDataManager().getEffect(player.getUuid(), "1");
                com.nxfx21.infabric.effects.InfuseEffect effect2 = plugin.getDataManager().getEffect(player.getUuid(), "2");

                if (effect1 == null && effect2 == null) {
                    player.sendMessage(Text.literal("You have no equipped effects to swap."));
                    return 0;
                }

                plugin.getDataManager().setEffect(player.getUuid(), "1", effect2);
                plugin.getDataManager().setEffect(player.getUuid(), "2", effect1);

                player.sendMessage(Text.literal("Swapped your equipped effects."));
                return 1;
            })
        );
    }

    private void registerTrustCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("trust")
            .then(argument("target", EntityArgumentType.player())
                .executes(context -> {
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    plugin.getDataManager().addTrust(player.getUuid(), target.getUuid());
                    context.getSource().sendMessage(Text.literal("Trusted " + target.getName().getString()));
                    return 1;
                })
            )
        );
    }

    private void registerUninfuseCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("uninfuse")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                    context.getSource().sendMessage(Text.literal("This command must be executed by a player."));
                    return 0;
                }
                ServerPlayerEntity player = context.getSource().getPlayer();
                boolean drained1 = plugin.getEffectManager().drainEffect(player, "1").type() == com.nxfx21.infabric.managers.EffectManager.EquipResultType.SUCCESS;
                boolean drained2 = plugin.getEffectManager().drainEffect(player, "2").type() == com.nxfx21.infabric.managers.EffectManager.EquipResultType.SUCCESS;
                if (!drained1 && !drained2) {
                    player.sendMessage(Text.literal("You have no equipped effects to uninfuse."));
                }
                return 1;
            })
            .then(argument("slot", StringArgumentType.word())
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(Text.literal("This command must be executed by a player."));
                        return 0;
                    }
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    String slot = StringArgumentType.getString(context, "slot");
                    if (!slot.equals("1") && !slot.equals("2")) {
                        player.sendMessage(Text.literal("Invalid slot. Use 1 or 2."));
                        return 0;
                    }
                    plugin.getEffectManager().drainEffect(player, slot);
                    return 1;
                })
            )
            .then(argument("target", EntityArgumentType.player())
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                    boolean drained1 = plugin.getEffectManager().drainEffect(target, "1").type() == com.nxfx21.infabric.managers.EffectManager.EquipResultType.SUCCESS;
                    boolean drained2 = plugin.getEffectManager().drainEffect(target, "2").type() == com.nxfx21.infabric.managers.EffectManager.EquipResultType.SUCCESS;
                    if (!drained1 && !drained2) {
                        context.getSource().sendMessage(Text.literal("Target has no equipped effects to uninfuse."));
                    } else {
                        context.getSource().sendMessage(Text.literal("Uninfused " + target.getName().getString() + "'s effects."));
                    }
                    return 1;
                })
                .then(argument("slot", StringArgumentType.word())
                    .executes(context -> {
                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                        String slot = StringArgumentType.getString(context, "slot");
                        if (!slot.equals("1") && !slot.equals("2")) {
                            context.getSource().sendMessage(Text.literal("Invalid slot. Use 1 or 2."));
                            return 0;
                        }
                        plugin.getEffectManager().drainEffect(target, slot);
                        context.getSource().sendMessage(Text.literal("Uninfused slot " + slot + " for " + target.getName().getString() + "."));
                        return 1;
                    })
                )
            )
        );
    }
}
