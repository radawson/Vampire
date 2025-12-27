package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.VampireMessages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

/**
 * Main command handler for the Vampire plugin.
 * This class handles the /vampire base command and delegates to registered subcommands.
 * It manages subcommand registration, permission checks, execution, and tab completion.
 */
public class VampireCommand implements CommandExecutor, TabCompleter {
    
    private final VampirePlugin plugin;
    private final Map<String, VCommand> subcommands;
    
    /**
     * Creates a new VampireCommand instance and registers all subcommands.
     * 
     * @param plugin The main VampirePlugin instance.
     */
    public VampireCommand(VampirePlugin plugin) {
        this.plugin = plugin;
        this.subcommands = new HashMap<>();
        
        // Register subcommands
        registerSubcommand(new CmdVampireAcceptGift(plugin));
        registerSubcommand(new CmdVampireFlask(plugin));
        registerSubcommand(new CmdVampireHelp(plugin, this.subcommands));
        registerSubcommand(new CmdVampireInfo(plugin));
        registerSubcommand(new CmdVampireList(plugin));
        registerSubcommand(new CmdVampireMode(plugin));
        registerSubcommand(new CmdVampireOfferGift(plugin));
        registerSubcommand(new CmdVampireRejectGift(plugin));
        registerSubcommand(new CmdVampireReload(plugin));
        registerSubcommand(new CmdVampireReset(plugin));
        registerSubcommand(new CmdVampireSet(plugin));
        registerSubcommand(new CmdVampireShow(plugin));
        registerSubcommand(new CmdVampireShriek(plugin));
        registerSubcommand(new CmdVampireStats(plugin));
        registerSubcommand(new CmdVampireDebug(plugin));
        registerSubcommand(new CmdVampireKit(plugin));
    }
    
    /**
     * Registers a subcommand, making it available under the base /vampire command.
     * Stores the command instance mapped by its lowercase name.
     * 
     * @param command The {@link VCommand} instance to register.
     */
    private void registerSubcommand(VCommand command) {
        if (command != null && command.getName() != null) {
            subcommands.put(command.getName().toLowerCase(), command);
        } else {
            plugin.getLogger().warning("Attempted to register a null or unnamed subcommand.");
        }
    }
    
    /**
     * Handles the execution of the /vampire command and its subcommands.
     * Parses the first argument to determine the subcommand, checks permissions,
     * and delegates execution to the appropriate {@link VCommand} instance.
     * If no subcommand is provided or found, it displays the help message.
     * 
     * @param sender The source of the command.
     * @param command The command which was executed.
     * @param label The alias of the command used.
     * @param args Passed command arguments.
     * @return True if a valid command was executed, false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            VCommand helpCmd = subcommands.get("help");
            if (helpCmd != null && sender.hasPermission(helpCmd.getPermission())) {
                return helpCmd.execute(sender, command, label, new String[0]);
            } else {
                 VampireMessages.sendLocalized(sender, "command.help.no_base_permission");
                 return true;
            }
        }
        
        String subcommandName = args[0].toLowerCase();
        VCommand cmd = subcommands.get(subcommandName);
        
        // Check for direct command execution or potential toggle shortcuts
        if (cmd == null) {
            // Check if the input name matches any registered TOGGLEABLE command
            for (VCommand potentialToggleCmd : subcommands.values()) {
                if (potentialToggleCmd.isToggleable() && potentialToggleCmd.getName().equalsIgnoreCase(subcommandName)) {
                    // Found a match! Execute it as a shortcut.
                    cmd = potentialToggleCmd;
                    break;
                }
            }
            // If we found a toggleable command via the loop, cmd is no longer null
        }

        // Proceed if a command (regular or toggle shortcut) was found
        if (cmd != null) {
            if (!sender.hasPermission(cmd.getPermission())) {
                VampireMessages.sendLocalized(sender, "command.error.no_permission");
                return true;
            }
            // Determine arguments: empty for toggle shortcuts, subArgs otherwise
            String[] executionArgs = (cmd.isToggleable() && cmd.getName().equalsIgnoreCase(subcommandName)) 
                                     ? new String[0] 
                                     : Arrays.copyOfRange(args, 1, args.length);
            return cmd.execute(sender, command, label, executionArgs);
        } else {
            // No matching subcommand or toggleable command found
            VampireMessages.sendLocalized(sender, "command.error.unknown_subcommand", subcommandName);
            return true;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            // Get standard subcommands sender has permission for
            List<String> suggestions = subcommands.keySet().stream()
                .filter(name -> name.startsWith(input))
                .filter(name -> sender.hasPermission(subcommands.get(name).getPermission()))
                .filter(name -> !subcommands.get(name).isToggleable())
                .collect(Collectors.toList());
            
            // Add any toggleable command shortcuts the sender has permission for
            for (VCommand toggleCmd : subcommands.values()) {
                if (toggleCmd.isToggleable() && toggleCmd.getName().startsWith(input) && sender.hasPermission(toggleCmd.getPermission())) {
                    if (!suggestions.contains(toggleCmd.getName())) { // Avoid duplicates
                        suggestions.add(toggleCmd.getName());
                    }
                }
            }
            
            suggestions.sort(String.CASE_INSENSITIVE_ORDER);
            return suggestions;
        }
        
        if (args.length > 1) {
            String subcommandName = args[0].toLowerCase();
            VCommand cmd = subcommands.get(subcommandName);
            
            if (cmd != null && sender.hasPermission(cmd.getPermission())) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return cmd.tabComplete(sender, command, alias, subArgs);
            }
        }
        
        return new ArrayList<>();
    }
} 