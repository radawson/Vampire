package org.clockworx.vampire.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.cmd.VCommand;
import static org.clockworx.vampire.command.CommandSuggestions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Builds the Brigadier command tree for the Vampire plugin.
 * This class creates the command structure using Paper's Brigadier API.
 */
public class VampireCommandTree {
    
    private final Map<String, VCommand> subcommands;
    
    public VampireCommandTree(VampirePlugin plugin) {
        this.subcommands = new HashMap<>();
        
        // Register all subcommands (same as VampireCommand does)
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireAcceptGift(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireFlask(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireHelp(plugin, this.subcommands));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireInfo(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireList(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireMode(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireOfferGift(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireRejectGift(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireReload(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireReset(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireSet(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireShow(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireShriek(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireStats(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireDebug(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireKit(plugin));
        registerSubcommand(new org.clockworx.vampire.cmd.CmdVampireLevel(plugin));
    }
    
    private void registerSubcommand(VCommand command) {
        if (command != null && command.getName() != null) {
            subcommands.put(command.getName().toLowerCase(), command);
        }
    }
    
    /**
     * Builds the complete command tree for /vampire command.
     * 
     * @return The root command node
     */
    public LiteralArgumentBuilder<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("vampire");
        
        // Base command (no arguments) - shows help
        root.executes(context -> {
            VCommand helpCmd = subcommands.get("help");
            if (helpCmd != null) {
                return executeSubcommand(context, helpCmd, new String[0]);
            }
            return Command.SINGLE_SUCCESS;
        });
        
        // Register all subcommands
        for (Map.Entry<String, VCommand> entry : subcommands.entrySet()) {
            String name = entry.getKey();
            VCommand cmd = entry.getValue();
            
            LiteralArgumentBuilder<CommandSourceStack> subcommand = Commands.literal(name)
                .requires(source -> source.getSender().hasPermission(cmd.getPermission()))
                .executes(context -> executeSubcommand(context, cmd, new String[0]));
            
            // Add argument handling based on command usage
            addArguments(subcommand, cmd);
            
            root.then(subcommand);
        }
        
        return root;
    }
    
    /**
     * Adds argument handling to a subcommand based on its usage pattern.
     */
    private void addArguments(LiteralArgumentBuilder<CommandSourceStack> subcommand, VCommand cmd) {
        String usage = cmd.getUsage();
        if (usage == null || usage.isEmpty()) {
            return;
        }
        
        // Parse usage to determine arguments needed
        // Simple heuristic: if usage contains [player], add optional player argument
        // If usage contains <player>, add required player argument
        // For more complex commands like "set", handle specially
        
        if (usage.contains("[player]")) {
            // Optional player argument
            subcommand.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                .suggests(CommandSuggestions::suggestOnlinePlayers)
                .executes(context -> {
                    String playerName = StringArgumentType.getString(context, "player");
                    return executeSubcommand(context, cmd, new String[]{playerName});
                })
            );
        } else if (usage.contains("<player>")) {
            // Required player argument
            subcommand.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                .suggests(CommandSuggestions::suggestOnlinePlayers)
                .executes(context -> {
                    String playerName = StringArgumentType.getString(context, "player");
                    return executeSubcommand(context, cmd, new String[]{playerName});
                })
            );
        }
        
        // Special handling for complex commands
        if (cmd.getName().equals("set")) {
            // /vampire set <type> <value> [player]
            subcommand.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("type", StringArgumentType.word())
                .suggests(CommandSuggestions::suggestSetTypes)
                .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("value", StringArgumentType.greedyString())
                    .executes(context -> {
                        String type = StringArgumentType.getString(context, "type");
                        String value = StringArgumentType.getString(context, "value");
                        return executeSubcommand(context, cmd, new String[]{type, value});
                    })
                    .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                        .suggests(CommandSuggestions::suggestOnlinePlayers)
                        .executes(context -> {
                            String type = StringArgumentType.getString(context, "type");
                            String value = StringArgumentType.getString(context, "value");
                            String playerName = StringArgumentType.getString(context, "player");
                            return executeSubcommand(context, cmd, new String[]{type, value, playerName});
                        })
                    )
                )
            );
        } else if (cmd.getName().equals("level")) {
            // /vampire level <op> [amount] [player]
            subcommand.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("op", StringArgumentType.word())
                .suggests(CommandSuggestions::suggestLevelOperations)
                .then(RequiredArgumentBuilder.<CommandSourceStack, Integer>argument("amount", IntegerArgumentType.integer())
                    .executes(context -> {
                        String op = StringArgumentType.getString(context, "op");
                        int amount = IntegerArgumentType.getInteger(context, "amount");
                        return executeSubcommand(context, cmd, new String[]{op, String.valueOf(amount)});
                    })
                    .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                        .suggests(CommandSuggestions::suggestOnlinePlayers)
                        .executes(context -> {
                            String op = StringArgumentType.getString(context, "op");
                            int amount = IntegerArgumentType.getInteger(context, "amount");
                            String playerName = StringArgumentType.getString(context, "player");
                            return executeSubcommand(context, cmd, new String[]{op, String.valueOf(amount), playerName});
                        })
                    )
                )
            );
        } else if (cmd.getName().equals("help")) {
            // /vampire help [topic]
            subcommand.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("topic", StringArgumentType.word())
                .suggests(CommandSuggestions::suggestHelpTopics)
                .executes(context -> {
                    String topic = StringArgumentType.getString(context, "topic");
                    return executeSubcommand(context, cmd, new String[]{topic});
                })
            );
        } else if (cmd.getName().equals("mode")) {
            // /vampire mode <type>
            subcommand.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("type", StringArgumentType.word())
                .suggests(CommandSuggestions::suggestModeTypes)
                .executes(context -> {
                    String type = StringArgumentType.getString(context, "type");
                    return executeSubcommand(context, cmd, new String[]{type});
                })
            );
        } else if (cmd.getName().equals("flask")) {
            // /vampire flask [type]
            subcommand.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("type", StringArgumentType.word())
                .suggests(CommandSuggestions::suggestFlaskTypes)
                .executes(context -> {
                    String type = StringArgumentType.getString(context, "type");
                    return executeSubcommand(context, cmd, new String[]{type});
                })
            );
        } else if (cmd.getName().equals("offergift")) {
            // /vampire offergift <player>
            subcommand.then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                .suggests(CommandSuggestions::suggestOnlinePlayers)
                .executes(context -> {
                    String playerName = StringArgumentType.getString(context, "player");
                    return executeSubcommand(context, cmd, new String[]{playerName});
                })
            );
        }
    }
    
    /**
     * Executes a subcommand by delegating to the existing VCommand handler.
     * 
     * @param context The Brigadier command context
     * @param cmd The VCommand to execute
     * @param args The command arguments
     * @return Command result code
     */
    private int executeSubcommand(CommandContext<CommandSourceStack> context, VCommand cmd, String[] args) {
        CommandSender sender = context.getSource().getSender();
        
        // Create a dummy Command object for compatibility
        org.bukkit.command.Command command = new org.bukkit.command.Command(
            "vampire", "", "", java.util.Collections.emptyList()) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                return false; // Not used
            }
        };
        
        // Execute using the existing command handler (onCommand is public, execute is protected)
        boolean result = cmd.onCommand(sender, command, "vampire", args);
        return result ? Command.SINGLE_SUCCESS : 0;
    }
}

