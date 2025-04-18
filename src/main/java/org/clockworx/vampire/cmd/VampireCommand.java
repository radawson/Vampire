package org.clockworx.vampire.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.util.ResourceUtil;
import org.clockworx.vampire.database.DatabaseManager;
import org.clockworx.vampire.entity.BloodOffer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

/**
 * Main command handler for the vampire plugin.
 * This class handles the /vampire command and its subcommands.
 */
public class VampireCommand implements CommandExecutor, TabCompleter {
    
    private final VampirePlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<String, VCommand> subcommands;
    private final Map<UUID, BloodOffer> bloodOffers;
    
    /**
     * Creates a new VampireCommand.
     * 
     * @param plugin The plugin instance
     */
    public VampireCommand(VampirePlugin plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.subcommands = new HashMap<>();
        this.bloodOffers = new HashMap<>();
        
        // Register subcommands
        registerSubcommand(new CmdVampireAccept(plugin));
        registerSubcommand(new CmdVampireFlask(plugin));
        registerSubcommand(new CmdVampireHelp(plugin));
        registerSubcommand(new CmdVampireList(plugin));
        registerSubcommand(new CmdVampireModeBloodlust(plugin));
        registerSubcommand(new CmdVampireModeNightvision(plugin));
        registerSubcommand(new CmdVampireModeIntend(plugin));
        registerSubcommand(new CmdVampireOffer(plugin));
        registerSubcommand(new CmdVampireReset(plugin));
        registerSubcommand(new CmdVampireReload(plugin));
        registerSubcommand(new CmdVampireShow(plugin));
        registerSubcommand(new CmdVampireSetFood(plugin));
        registerSubcommand(new CmdVampireSetHealth(plugin));
        registerSubcommand(new CmdVampireSetInfection(plugin));
        registerSubcommand(new CmdVampireSetVampire(plugin));
        registerSubcommand(new CmdVampireShriek(plugin));
        registerSubcommand(new CmdVampireStats(plugin));
        registerSubcommand(new CmdVampireVersion(plugin));
    }
    
    /**
     * Registers a subcommand.
     * 
     * @param command The command to register
     */
    private void registerSubcommand(VCommand command) {
        subcommands.put(command.getName().toLowerCase(), command);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subcommand = args[0].toLowerCase();
        VCommand cmd = subcommands.get(subcommand);
        
        if (cmd == null) {
            ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.unknown_subcommand"));
            return true;
        }
        
        // Check permission
        if (!sender.hasPermission(cmd.getPermission())) {
            ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.no_permission"));
            return true;
        }
        
        // Execute subcommand
        return cmd.execute(sender, command, label, Arrays.copyOfRange(args, 1, args.length));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0 || args.length == 1) {
            return subcommands.keySet().stream()
                .filter(name -> name.startsWith(args[0].toLowerCase()))
                .filter(name -> sender.hasPermission(subcommands.get(name).getPermission()))
                .collect(Collectors.toList());
        }
        
        String subcommand = args[0].toLowerCase();
        VCommand cmd = subcommands.get(subcommand);
        
        if (cmd != null && sender.hasPermission(cmd.getPermission())) {
            return cmd.tabComplete(sender, command, alias, Arrays.copyOfRange(args, 1, args.length));
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Sends the help message to a player.
     * 
     * @param sender The command sender
     */
    private void sendHelp(CommandSender sender) {
        ResourceUtil.sendMessage(sender, ResourceUtil.getMessage("command.help.header"));
        
        subcommands.values().stream()
            .filter(cmd -> sender.hasPermission(cmd.getPermission()))
            .forEach(cmd -> {
                ResourceUtil.sendMessage(sender, ResourceUtil.getMessage("command.help.format")
                    .replace("%command%", cmd.getName())
                    .replace("%description%", ResourceUtil.getMessage("command.help." + cmd.getName())));
            });
    }
    
    /**
     * Gets the blood offers map.
     * 
     * @return The blood offers map
     */
    public Map<UUID, BloodOffer> getBloodOffers() {
        return bloodOffers;
    }
} 