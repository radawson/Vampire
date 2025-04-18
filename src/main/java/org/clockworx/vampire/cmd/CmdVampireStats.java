package org.clockworx.vampire.cmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command for displaying vampire statistics.
 * This command shows various statistics about a player's vampire status,
 * such as infection level, blood level, and other relevant information.
 */
public class CmdVampireStats extends VCommand {
    
    /**
     * Creates a new stats command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireStats(VampirePlugin plugin) {
        super(plugin, "stats", "vampire.stats");
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Get player data
        CompletableFuture<VampirePlayer> future = plugin.getVampirePlayer(player.getUniqueId());
        future.thenAccept(vampirePlayer -> {
            if (vampirePlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player data not found.");
                return;
            }
            
            // Display stats
            sender.sendMessage(ChatColor.GOLD + "=== Vampire Stats ===");
            sender.sendMessage(ChatColor.YELLOW + "Vampire: " + (vampirePlayer.isVampire() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"));
            sender.sendMessage(ChatColor.YELLOW + "Infection Level: " + ChatColor.WHITE + vampirePlayer.getInfectionLevel());
            sender.sendMessage(ChatColor.YELLOW + "Blood Level: " + ChatColor.WHITE + vampirePlayer.getBloodLevel());
            // Add more stats as needed
        });
        
        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
} 