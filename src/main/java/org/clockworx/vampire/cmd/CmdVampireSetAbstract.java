package org.clockworx.vampire.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for vampire set commands.
 * This class provides common functionality for commands that set vampire properties.
 */
public abstract class CmdVampireSetAbstract extends VCommand {
    
    /**
     * Creates a new vampire set command.
     * 
     * @param plugin The plugin instance
     * @param name The command name
     * @param permission The permission required to use this command
     */
    public CmdVampireSetAbstract(VampirePlugin plugin, String name, String permission) {
        super(plugin, name, permission);
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Check arguments
        if (args.length < 2) {
            sendError(sender, getMessage("command.set.usage")
                .replace("%command%", getName()));
            return false;
        }
        
        // Get target player
        String targetName = args[1];
        Player targetPlayer = Bukkit.getPlayer(targetName);
        
        if (targetPlayer == null) {
            sendError(sender, getMessage("command.player_not_found")
                .replace("%player%", targetName));
            return false;
        }
        
        // Get vampire player data
        plugin.getVampirePlayer(targetPlayer.getUniqueId()).thenAccept(targetVampirePlayer -> {
            if (targetVampirePlayer == null) {
                sendError(sender, getMessage("command.player_data_not_found"));
                return;
            }
            
            // Parse value if provided
            String valueStr = args.length > 2 ? args[2] : null;
            
            // Set the value
            boolean success = setValue(targetVampirePlayer, targetPlayer, valueStr, sender);
            
            if (success) {
                sendSuccess(sender, getMessage("command.set.success")
                    .replace("%property%", getValueName())
                    .replace("%player%", targetPlayer.getName()));
            }
        });
        
        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 2) {
            // Complete player names
            String partial = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partial)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 3) {
            // Complete value suggestions
            addValueCompletions(completions);
        }
        
        return completions;
    }
    
    /**
     * Sets the value for the target player.
     * 
     * @param vampirePlayer The target VampirePlayer
     * @param player The target Player
     * @param valueStr The value as a string, or null if not provided
     * @param sender The command sender
     * @return true if successful, false otherwise
     */
    protected abstract boolean setValue(VampirePlayer vampirePlayer, Player player, String valueStr, CommandSender sender);
    
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
     * Adds value completions to the list.
     * 
     * @param completions The list to add completions to
     */
    protected abstract void addValueCompletions(List<String> completions);
} 