package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Command for making a vampire shriek.
 * This command allows vampires to emit a shriek that can be heard by other players.
 * The shriek can be used to intimidate or communicate with other players.
 */
public class CmdVampireShriek extends VCommand {
    
    private final VampireManager vampireManager;

    /**
     * Creates a new shriek command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireShriek(VampirePlugin plugin) {
        super(plugin, "shriek", VampirePermission.SHRIEK, 
              "Make a shriek", "");
        this.vampireManager = plugin.getVampireManager();
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Permission check handled by VCommand
        
        if (!(sender instanceof Player)) {
            VampireMessages.sendLocalized(sender, "command.player_only");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Get player data synchronously
        VampirePlayer vampirePlayer = vampireManager.getCachedVampirePlayer(player.getUniqueId());
        
        // Check if player data exists and player is a vampire
        if (vampirePlayer == null || !vampirePlayer.isVampire()) {
            sendError(sender, getMessage("vampire.not_vampire").replace("%player%", "You"));
            return true;
        }
            
        // Attempt to perform shriek via the manager
        boolean success = vampireManager.performShriek(player.getUniqueId());
        
        // Manager handles sending success/cooldown messages to the player
        // No need to send additional messages here unless shriek failed for other reasons.
        if (!success) {
            // Optional: Log if needed, but manager already logs/sends feedback
            // VampireMessages.debug("Shriek command failed for " + player.getName());
        }

        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        // No arguments for shriek command
        return new ArrayList<>();
    }
} 