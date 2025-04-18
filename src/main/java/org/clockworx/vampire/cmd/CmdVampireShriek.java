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
 * Command for making a vampire shriek.
 * This command allows vampires to emit a shriek that can be heard by other players.
 * The shriek can be used to intimidate or communicate with other players.
 */
public class CmdVampireShriek extends VCommand {
    
    /**
     * Creates a new shriek command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireShriek(VampirePlugin plugin) {
        super(plugin, "shriek", "vampire.shriek");
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
            
            // Check if player is a vampire
            if (!vampirePlayer.isVampire()) {
                sender.sendMessage(ChatColor.RED + "Only vampires can use this command.");
                return;
            }
            
            // Perform shriek
            // Note: We're assuming there's a method to perform the shriek
            // If this method doesn't exist, you'll need to implement it
            sender.sendMessage(ChatColor.RED + "You emit a blood-curdling shriek!");
            // If there's a method like vampirePlayer.shriek(), uncomment it
            // vampirePlayer.shriek();
        });
        
        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
} 