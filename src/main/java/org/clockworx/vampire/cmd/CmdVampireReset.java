package org.clockworx.vampire.cmd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for resetting vampire status.
 */
public class CmdVampireReset extends VCommand {
    
    /**
     * Creates a new reset command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireReset(VampirePlugin plugin) {
        super(plugin, "reset", "vampire.reset");
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("vampire.reset")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        // Get target player
        Player targetPlayer;
        if (args.length > 0) {
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must specify a player when using this command from console.");
                return true;
            }
            targetPlayer = (Player) sender;
        }
        
        // Get the VampirePlayer instance
        VampirePlayer vampirePlayer = plugin.getVampirePlayer(targetPlayer.getUniqueId()).join();
        if (vampirePlayer == null) {
            sender.sendMessage(ChatColor.RED + "Player data not found.");
            return true;
        }
        
        // Reset vampire status
        boolean wasVampire = vampirePlayer.isVampire();
        boolean wasInfected = vampirePlayer.isInfected();
        
        vampirePlayer.setVampire(false);
        vampirePlayer.setInfectionLevel(0.0);
        
        // Notify the player
        if (targetPlayer.isOnline()) {
            if (wasVampire || wasInfected) {
                targetPlayer.sendMessage(ChatColor.GREEN + "Your vampire status has been reset.");
            } else {
                targetPlayer.sendMessage(ChatColor.YELLOW + "You were not a vampire or infected.");
            }
        }
        
        // Notify the sender if different from target
        if (sender != targetPlayer) {
            sender.sendMessage(ChatColor.GREEN + "Reset vampire status for " + targetPlayer.getName() + ".");
        }
        
        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Complete player names
            String partial = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partial)) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
} 