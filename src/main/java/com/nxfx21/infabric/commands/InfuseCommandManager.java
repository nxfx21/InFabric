package com.nxfx21.infabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.nxfx21.infabric.Infuse;
import com.nxfx21.infabric.Message;
import com.nxfx21.infabric.Message.MessageType;
import com.nxfx21.infabric.effects.InfuseEffect;
import com.nxfx21.infabric.inventories.AugOrRegChooser;
import com.nxfx21.infabric.inventories.EffectChooser;
import com.nxfx21.infabric.inventories.RecipeGUI;
import com.nxfx21.infabric.inventories.RecipeListGUI;
import com.nxfx21.infabric.managers.CooldownManager;
import com.nxfx21.infabric.managers.EffectManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class InfuseCommandManager {
    private final Infuse plugin;

    private static final SuggestionProvider<ServerCommandSource> EFFECT_SUGGESTIONS = (context, builder) -> {
        List<String> effectKeys = new ArrayList<>();
        for (InfuseEffect effect : InfuseEffect.getRegisteredEffects().values()) {
            effectKeys.add(effect.getKey());
        }
        return CommandSource.suggestMatching(effectKeys, builder);
    };

    private static final SuggestionProvider<ServerCommandSource> REGULAR_EFFECT_SUGGESTIONS = (context, builder) -> {
        List<String> effectKeys = new ArrayList<>();
        for (InfuseEffect effect : InfuseEffect.getRegisteredEffects().values()) {
            if (!effect.isAugmented()) {
                effectKeys.add(effect.getKey());
            }
        }
        return CommandSource.suggestMatching(effectKeys, builder);
    };

    private static final SuggestionProvider<ServerCommandSource> CONTROL_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(List.of("offhand", "command"), builder);

    private static final SuggestionProvider<ServerCommandSource> SLOT_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(List.of("1", "2"), builder);

    public InfuseCommandManager() {
        this.plugin = Infuse.getInstance();
    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerInfuseCommand(dispatcher);
            registerAbilitiesCommand(dispatcher);
            registerClearEffectsCommand(dispatcher);
            registerDrainCommand(dispatcher);
            registerDrawCommand(dispatcher);
            registerSwapEffectsCommand(dispatcher);
            registerTrustCommand(dispatcher);
            registerUntrustCommand(dispatcher);
            registerUninfuseCommand(dispatcher);
            registerControlsCommand(dispatcher);
            registerRecipesCommand(dispatcher);
        });
    }

    private void registerInfuseCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("infuse")
            .then(literal("reload")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    plugin.getMainConfig().load();
                    plugin.getRecipeManager().reload();
                    Message.getTranslator().loadAll();
                    context.getSource().sendMessage(Text.literal("Infuse configs and translations reloaded."));
                    return 1;
                })
            )
            .then(literal("gui")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                        return 0;
                    }
                    new EffectChooser(context.getSource().getPlayer()).open();
                    return 1;
                })
            )
            .then(literal("recipes")
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                        return 0;
                    }
                    new RecipeListGUI(context.getSource().getPlayer()).open();
                    return 1;
                })
            )
            .then(literal("controls")
                .then(argument("mode", StringArgumentType.word())
                    .suggests(CONTROL_SUGGESTIONS)
                    .executes(context -> {
                        if (!context.getSource().isExecutedByPlayer()) {
                            context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                            return 0;
                        }
                        String mode = StringArgumentType.getString(context, "mode").toLowerCase();
                        if (!mode.equals("offhand") && !mode.equals("command")) {
                            context.getSource().sendMessage(new Message(MessageType.CONTROLS_INVALID_PARAM).toComponent());
                            return 0;
                        }
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        plugin.getDataManager().setControlMode(player.getUuid(), mode);
                        Message msg = new Message(MessageType.INFUSE_CONTROLS_SUCCESS);
                        msg.applyPlaceholder("control_mode", mode);
                        player.sendMessage(msg.toComponent());
                        return 1;
                    })
                )
            )
            .then(literal("giveeffect")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("target", EntityArgumentType.player())
                    .then(argument("effect", StringArgumentType.string())
                        .suggests(EFFECT_SUGGESTIONS)
                        .executes(context -> {
                            ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                            String effectKey = StringArgumentType.getString(context, "effect");
                            InfuseEffect mapping = InfuseEffect.fromString(effectKey);
                            if (mapping == null) {
                                context.getSource().sendMessage(new Message(MessageType.INFUSE_INVALID_PARAM).toComponent());
                                return 0;
                            }
                            target.getInventory().insertStack(mapping.createItem());
                            Message msg = new Message(MessageType.INFUSE_GIVEEFFECT_SUCCESS);
                            msg.applyPlaceholder("effect_color", "");
                            msg.applyPlaceholder("effect_name", mapping.getName().toString());
                            target.sendMessage(msg.toComponent());
                            context.getSource().sendMessage(Text.literal("Gave effect " + mapping.getKey() + " to " + target.getName().getString()));
                            return 1;
                        })
                    )
                )
            )
            .then(literal("seteffect")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("slot", StringArgumentType.word())
                    .suggests(SLOT_SUGGESTIONS)
                    .then(argument("target", EntityArgumentType.player())
                        .then(argument("effect", StringArgumentType.string())
                            .suggests(EFFECT_SUGGESTIONS)
                            .executes(context -> {
                                String slot = StringArgumentType.getString(context, "slot");
                                if (!slot.equals("1") && !slot.equals("2")) {
                                    Message msg = new Message(MessageType.INFUSE_INVALID_SLOT);
                                    msg.applyPlaceholder("slot", slot);
                                    context.getSource().sendMessage(msg.toComponent());
                                    return 0;
                                }
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                                String effectKey = StringArgumentType.getString(context, "effect");
                                InfuseEffect mapping = InfuseEffect.fromString(effectKey);
                                if (mapping == null) {
                                    context.getSource().sendMessage(new Message(MessageType.INFUSE_INVALID_PARAM).toComponent());
                                    return 0;
                                }
                                plugin.getEffectManager().setEffect(target, mapping, slot);
                                Message msg = new Message(MessageType.INFUSE_SETEFFECT_SUCCESS);
                                msg.applyPlaceholder("slot", slot);
                                msg.applyPlaceholder("player_name", target.getName().getString());
                                msg.applyPlaceholder("effect_name", mapping.getName().toString());
                                context.getSource().sendMessage(msg.toComponent());
                                return 1;
                            })
                        )
                    )
                )
            )
            .then(literal("cleareffects")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("target", EntityArgumentType.player())
                    .executes(context -> {
                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                        plugin.getDataManager().removeEffect(target.getUuid(), "1");
                        plugin.getDataManager().removeEffect(target.getUuid(), "2");
                        Message msg = new Message(MessageType.INFUSE_CLEAREFFECTS_SUCCESS);
                        msg.applyPlaceholder("player_name", target.getName().getString());
                        context.getSource().sendMessage(msg.toComponent());
                        return 1;
                    })
                )
            )
            .then(literal("cooldown")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("target", EntityArgumentType.player())
                    .executes(context -> {
                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                        CooldownManager.removeAllCooldowns(target.getUuid());
                        Message msg = new Message(MessageType.INFUSE_COOLDOWN_SUCCESS);
                        msg.applyPlaceholder("player_name", target.getName().getString());
                        context.getSource().sendMessage(msg.toComponent());
                        return 1;
                    })
                )
            )
        );
    }

    private void registerControlsCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("controls")
            .then(argument("mode", StringArgumentType.word())
                .suggests(CONTROL_SUGGESTIONS)
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                        return 0;
                    }
                    String mode = StringArgumentType.getString(context, "mode").toLowerCase();
                    if (!mode.equals("offhand") && !mode.equals("command")) {
                        context.getSource().sendMessage(new Message(MessageType.CONTROLS_INVALID_PARAM).toComponent());
                        return 0;
                    }
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    plugin.getDataManager().setControlMode(player.getUuid(), mode);
                    Message msg = new Message(MessageType.INFUSE_CONTROLS_SUCCESS);
                    msg.applyPlaceholder("control_mode", mode);
                    player.sendMessage(msg.toComponent());
                    return 1;
                })
            )
            .executes(context -> {
                context.getSource().sendMessage(new Message(MessageType.CONTROLS_USAGE).toComponent());
                return 0;
            })
        );
    }

    private void registerRecipesCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("recipes")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                    context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                    return 0;
                }
                new RecipeListGUI(context.getSource().getPlayer()).open();
                return 1;
            })
            .then(argument("effect", StringArgumentType.string())
                .suggests(REGULAR_EFFECT_SUGGESTIONS)
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) return 0;
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    String effectKey = StringArgumentType.getString(context, "effect");
                    InfuseEffect effect = InfuseEffect.fromString(effectKey);
                    if (effect == null) {
                        player.sendMessage(new Message(MessageType.RECIPE_NOT_FOUND).toComponent());
                        return 0;
                    }
                    if (effect.getAugmentedVersion() != null) {
                        new AugOrRegChooser(player, effect).open();
                    } else {
                        new RecipeGUI(player, effect, null).open();
                    }
                    return 1;
                })
            )
        );

        dispatcher.register(literal("recipe")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                    context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                    return 0;
                }
                new RecipeListGUI(context.getSource().getPlayer()).open();
                return 1;
            })
            .then(argument("effect", StringArgumentType.string())
                .suggests(REGULAR_EFFECT_SUGGESTIONS)
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) return 0;
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    String effectKey = StringArgumentType.getString(context, "effect");
                    InfuseEffect effect = InfuseEffect.fromString(effectKey);
                    if (effect == null) {
                        player.sendMessage(new Message(MessageType.RECIPE_NOT_FOUND).toComponent());
                        return 0;
                    }
                    if (effect.getAugmentedVersion() != null) {
                        new AugOrRegChooser(player, effect).open();
                    } else {
                        new RecipeGUI(player, effect, null).open();
                    }
                    return 1;
                })
            )
        );
    }

    private void registerAbilitiesCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("abilities")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                    context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                    return 0;
                }
                ServerPlayerEntity player = context.getSource().getPlayer();
                InfuseEffect effect1 = plugin.getDataManager().getEffect(player.getUuid(), "1");
                InfuseEffect effect2 = plugin.getDataManager().getEffect(player.getUuid(), "2");

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
                .suggests(SLOT_SUGGESTIONS)
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                        return 0;
                    }
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    String slot = StringArgumentType.getString(context, "slot");
                    if (!slot.equals("1") && !slot.equals("2")) {
                        Message msg = new Message(MessageType.SLOT_EMPTY);
                        msg.applyPlaceholder("slot", slot);
                        player.sendMessage(msg.toComponent());
                        return 0;
                    }
                    InfuseEffect effect = plugin.getDataManager().getEffect(player.getUuid(), slot);
                    if (effect != null) {
                        effect.activateSpark(player);
                        return 1;
                    } else {
                        Message msg = new Message(MessageType.SLOT_EMPTY);
                        msg.applyPlaceholder("slot", slot);
                        player.sendMessage(msg.toComponent());
                        return 0;
                    }
                })
            )
        );
    }

    private void registerClearEffectsCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("cleareffects")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                    context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                    return 0;
                }
                ServerPlayerEntity player = context.getSource().getPlayer();
                plugin.getDataManager().removeEffect(player.getUuid(), "1");
                plugin.getDataManager().removeEffect(player.getUuid(), "2");
                Message msg = new Message(MessageType.INFUSE_CLEAREFFECTS_SUCCESS);
                msg.applyPlaceholder("player_name", player.getName().getString());
                player.sendMessage(msg.toComponent());
                return 1;
            })
            .then(argument("target", EntityArgumentType.player())
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                    plugin.getDataManager().removeEffect(target.getUuid(), "1");
                    plugin.getDataManager().removeEffect(target.getUuid(), "2");
                    Message msg = new Message(MessageType.INFUSE_CLEAREFFECTS_SUCCESS);
                    msg.applyPlaceholder("player_name", target.getName().getString());
                    context.getSource().sendMessage(msg.toComponent());
                    return 1;
                })
            )
        );
    }

    private void registerDrainCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("drain")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                    context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                    return 0;
                }
                ServerPlayerEntity player = context.getSource().getPlayer();
                boolean drained1 = plugin.getEffectManager().drainEffect(player, "1").type() == EffectManager.EquipResultType.SUCCESS;
                boolean drained2 = plugin.getEffectManager().drainEffect(player, "2").type() == EffectManager.EquipResultType.SUCCESS;
                if (!drained1 && !drained2) {
                    Message msg = new Message(MessageType.EFFECT_NONE_EQUIPPED);
                    msg.applyPlaceholder("slot", "1 or 2");
                    player.sendMessage(msg.toComponent());
                }
                return 1;
            })
            .then(argument("slot", StringArgumentType.word())
                .suggests(SLOT_SUGGESTIONS)
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                        return 0;
                    }
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    String slot = StringArgumentType.getString(context, "slot");
                    if (!slot.equals("1") && !slot.equals("2")) {
                        Message msg = new Message(MessageType.INFUSE_INVALID_SLOT);
                        msg.applyPlaceholder("slot", slot);
                        player.sendMessage(msg.toComponent());
                        return 0;
                    }
                    plugin.getEffectManager().drainEffect(player, slot);
                    return 1;
                })
            )
        );

        dispatcher.register(literal("ldrain")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) return 0;
                plugin.getEffectManager().drainEffect(context.getSource().getPlayer(), "1");
                return 1;
            })
        );

        dispatcher.register(literal("rdrain")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) return 0;
                plugin.getEffectManager().drainEffect(context.getSource().getPlayer(), "2");
                return 1;
            })
        );
    }

    private void registerDrawCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("draw")
            .then(argument("mode", StringArgumentType.word())
                .suggests((ctx, builder) -> CommandSource.suggestMatching(List.of("left", "right", "1", "2", "both"), builder))
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                        return 0;
                    }
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    String mode = StringArgumentType.getString(context, "mode").toLowerCase();
                    switch (mode) {
                        case "left", "1" -> plugin.getEffectManager().drainEffect(player, "1");
                        case "right", "2" -> plugin.getEffectManager().drainEffect(player, "2");
                        case "both" -> {
                            plugin.getEffectManager().drainEffect(player, "1");
                            plugin.getEffectManager().drainEffect(player, "2");
                        }
                        default -> player.sendMessage(new Message(MessageType.WITHDRAW_INVALID).toComponent());
                    }
                    return 1;
                })
            )
            .executes(context -> {
                context.getSource().sendMessage(new Message(MessageType.WITHDRAW_INVALID).toComponent());
                return 0;
            })
        );
    }

    private void registerSwapEffectsCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("swapeffects")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                    context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                    return 0;
                }
                ServerPlayerEntity player = context.getSource().getPlayer();
                InfuseEffect effect1 = plugin.getDataManager().getEffect(player.getUuid(), "1");
                InfuseEffect effect2 = plugin.getDataManager().getEffect(player.getUuid(), "2");

                if (effect1 == null && effect2 == null) {
                    player.sendMessage(new Message(MessageType.SWAP_NO_EFFECTS).toComponent());
                    return 0;
                }

                plugin.getDataManager().setEffect(player.getUuid(), "1", effect2);
                plugin.getDataManager().setEffect(player.getUuid(), "2", effect1);

                player.sendMessage(new Message(MessageType.SWAP_SUCCESS).toComponent());
                return 1;
            })
        );
    }

    private void registerTrustCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("trust")
            .then(argument("target", EntityArgumentType.player())
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(new Message(MessageType.TRUST_CONSOLE_USAGE).toComponent());
                        return 0;
                    }
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                    ServerPlayerEntity player = context.getSource().getPlayer();

                    if (player.getUuid().equals(target.getUuid())) {
                        player.sendMessage(new Message(MessageType.TRUST_SELF).toComponent());
                        return 0;
                    }

                    if (plugin.getDataManager().isTrusted(player.getUuid(), target.getUuid())) {
                        Message msg = new Message(MessageType.TRUST_ALREADY_TRUSTED);
                        msg.applyPlaceholder("target", target.getName().getString());
                        player.sendMessage(msg.toComponent());
                        return 0;
                    }

                    plugin.getDataManager().addTrust(player.getUuid(), target.getUuid());
                    Message msg = new Message(MessageType.TRUST_ADDED);
                    msg.applyPlaceholder("target", target.getName().getString());
                    player.sendMessage(msg.toComponent());
                    return 1;
                })
            )
            .executes(context -> {
                Message msg = new Message(MessageType.TRUST_INCORRECT_USAGE);
                msg.applyPlaceholder("label", "trust");
                context.getSource().sendMessage(msg.toComponent());
                return 0;
            })
        );
    }

    private void registerUntrustCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("untrust")
            .then(argument("target", EntityArgumentType.player())
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(new Message(MessageType.TRUST_CONSOLE_USAGE).toComponent());
                        return 0;
                    }
                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                    ServerPlayerEntity player = context.getSource().getPlayer();

                    if (player.getUuid().equals(target.getUuid())) {
                        player.sendMessage(new Message(MessageType.TRUST_SELF).toComponent());
                        return 0;
                    }

                    if (!plugin.getDataManager().isTrusted(player.getUuid(), target.getUuid())) {
                        Message msg = new Message(MessageType.TRUST_NOT_TRUSTED);
                        msg.applyPlaceholder("target", target.getName().getString());
                        player.sendMessage(msg.toComponent());
                        return 0;
                    }

                    plugin.getDataManager().removeTrust(player.getUuid(), target.getUuid());
                    Message msg = new Message(MessageType.TRUST_REMOVED);
                    msg.applyPlaceholder("target", target.getName().getString());
                    player.sendMessage(msg.toComponent());
                    return 1;
                })
            )
            .executes(context -> {
                Message msg = new Message(MessageType.TRUST_INCORRECT_USAGE);
                msg.applyPlaceholder("label", "untrust");
                context.getSource().sendMessage(msg.toComponent());
                return 0;
            })
        );
    }

    private void registerUninfuseCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("uninfuse")
            .executes(context -> {
                if (!context.getSource().isExecutedByPlayer()) {
                    context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                    return 0;
                }
                ServerPlayerEntity player = context.getSource().getPlayer();
                boolean drained1 = plugin.getEffectManager().drainEffect(player, "1").type() == EffectManager.EquipResultType.SUCCESS;
                boolean drained2 = plugin.getEffectManager().drainEffect(player, "2").type() == EffectManager.EquipResultType.SUCCESS;
                if (!drained1 && !drained2) {
                    Message msg = new Message(MessageType.EFFECT_NONE_EQUIPPED);
                    msg.applyPlaceholder("slot", "1 or 2");
                    player.sendMessage(msg.toComponent());
                }
                return 1;
            })
            .then(argument("slot", StringArgumentType.word())
                .suggests(SLOT_SUGGESTIONS)
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) {
                        context.getSource().sendMessage(new Message(MessageType.ERROR_NOT_PLAYER).toComponent());
                        return 0;
                    }
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    String slot = StringArgumentType.getString(context, "slot");
                    if (!slot.equals("1") && !slot.equals("2")) {
                        Message msg = new Message(MessageType.INFUSE_INVALID_SLOT);
                        msg.applyPlaceholder("slot", slot);
                        player.sendMessage(msg.toComponent());
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
                    boolean drained1 = plugin.getEffectManager().drainEffect(target, "1").type() == EffectManager.EquipResultType.SUCCESS;
                    boolean drained2 = plugin.getEffectManager().drainEffect(target, "2").type() == EffectManager.EquipResultType.SUCCESS;
                    if (!drained1 && !drained2) {
                        context.getSource().sendMessage(Text.literal("Target has no equipped effects to uninfuse."));
                    } else {
                        context.getSource().sendMessage(Text.literal("Uninfused " + target.getName().getString() + "'s effects."));
                    }
                    return 1;
                })
                .then(argument("slot", StringArgumentType.word())
                    .suggests(SLOT_SUGGESTIONS)
                    .executes(context -> {
                        ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "target");
                        String slot = StringArgumentType.getString(context, "slot");
                        if (!slot.equals("1") && !slot.equals("2")) {
                            Message msg = new Message(MessageType.INFUSE_INVALID_SLOT);
                            msg.applyPlaceholder("slot", slot);
                            context.getSource().sendMessage(msg.toComponent());
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
