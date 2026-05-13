package com.catadmirer.infuseSMP.commands;

import com.catadmirer.infuseSMP.Infuse;
import com.catadmirer.infuseSMP.Message;
import com.catadmirer.infuseSMP.managers.CooldownManager;
import com.catadmirer.infuseSMP.managers.EffectMapping;
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
                    new com.catadmirer.infuseSMP.inventories.EffectChooser(context.getSource().getPlayer()).open();
                    return 1;
                })
            )
            .then(literal("recipes")
                .executes(context -> {
                    if (!context.getSource().isExecutedByPlayer()) return 0;
                    new com.catadmirer.infuseSMP.inventories.RecipeListGUI(context.getSource().getPlayer()).open();
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
                            EffectMapping mapping = EffectMapping.fromEffectKey(effectKey);
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
                context.getSource().sendMessage(Text.literal("Abilities command (stub)"));
                return 1;
            })
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
                context.getSource().sendMessage(Text.literal("Drain command (stub)"));
                return 1;
            })
        );
    }

    private void registerSwapEffectsCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("swapeffects")
            .executes(context -> {
                context.getSource().sendMessage(Text.literal("Swapped effects (stub)"));
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
}
