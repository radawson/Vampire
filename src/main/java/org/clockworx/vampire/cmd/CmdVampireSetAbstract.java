package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;

/**
 * Abstract base class for vampire set commands.
 * This class provides common functionality for commands that set vampire properties.
 */
public abstract class CmdVampireSetAbstract extends VCommand {
    
    protected final VampireManager vampireManager;

    /**
     * Creates a new vampire set command.
     * 
     * @param plugin The plugin instance
     * @param name The command name
     * @param permission The permission required to use this command
     * @param description A brief description of the command.
     * @param usage A string indicating the command's arguments/usage pattern.
     */
    public CmdVampireSetAbstract(VampirePlugin plugin, String name, String permission, String description, String usage) {
        super(plugin, name, permission, description, usage);
        this.vampireManager = plugin.getVampireManager();
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Permission check already done by VCommand
        
        // Argument validation (expecting at least <player> <value>, type is implied by command name)
        if (args.length < 2) {
            // TODO: Improve usage message based on specific command
            sendError(sender, "Usage: /vampire set " + getName() + " <player> <value>");
            return true;
        }
        
        String targetName = args[0]; // Argument 0 is player name
        String valueStr = args[1];   // Argument 1 is the value

        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null) {
            sendError(sender, getMessage("player.not_online").replace("%player%", targetName));
            return true;
        }
        
        // Get vampire player data synchronously
        VampirePlayer targetVampirePlayer = vampireManager.getCachedVampirePlayer(targetPlayer.getUniqueId());
        if (targetVampirePlayer == null) {
            sendError(sender, getMessage("command.player_data_not_found")); // Need lang key
            return true;
        }
        
        // Call subclass to parse and set the value, passing the manager
        boolean success = setValue(targetVampirePlayer, targetPlayer, valueStr, sender, vampireManager);
            
        // Subclass should handle feedback messages
        // if (success) {
        //     sendSuccess(sender, getMessage("command.set.success") // Generic success?
        //         .replace("%property%", getValueName())
        //         .replace("%player%", targetPlayer.getName()));
        // }
        
        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Permission check done by VCommand
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Complete player names (Arg 0)
            String partial = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                       .map(Player::getName)
                       .filter(name -> name.toLowerCase().startsWith(partial))
                       .collect(Collectors.toList());

        } else if (args.length == 2) {
            // Complete value suggestions (Arg 1)
            addValueCompletions(completions, args[1]); // Pass current value for filtering
        }
        
        return completions;
    }
    
    /**
     * Sets the value for the target player.
     * Implementations should parse valueStr and call the appropriate VampireManager method.
     * 
     * @param targetVampirePlayer The target VampirePlayer POJO
     * @param targetPlayer The target Player entity
     * @param valueStr The value as a string
     * @param sender The command sender (for feedback)
     * @param manager The VampireManager instance
     * @return true if successful, false otherwise (subclass should send error messages)
     */
    protected abstract boolean setValue(VampirePlayer targetVampirePlayer, Player targetPlayer, String valueStr, CommandSender sender, VampireManager manager);
    
    /**
     * Gets the name of this set command.
     * 
     * @return The command name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the name of the value being set.
     * 
     * @return The value name
     */
    protected abstract String getValueName();
    
    /**
     * Adds value completions to the list based on the current partial input.
     * 
     * @param completions The list to add completions to
     * @param currentInput The current value string typed by the user
     */
    protected abstract void addValueCompletions(List<String> completions, String currentInput);
} 