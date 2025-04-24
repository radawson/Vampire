package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

// Import Adventure API
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Command for showing detailed status information about a vampire player.
 * This command specifically targets players who are currently vampires.
 * It can show information about the sender if they are a vampire, or about another
 * player if the sender has the appropriate permission.
 */
public class CmdVampireShow extends VCommand {
    
    private final VampireManager vampireManager;

    /**
     * Creates a new show command focused on displaying vampire status.
     * 
     * @param plugin The plugin instance.
     */
    public CmdVampireShow(VampirePlugin plugin) {
        // Uses the base 'show' permission for self, 'show.other' is checked internally.
        super(plugin, "show", VampirePermission.SHOW, 
              "Show detailed vampire status", "[player]"); 
        this.vampireManager = plugin.getVampireManager();
    }
    
    /**
     * Executes the /vampire show command.
     * Retrieves the target player (self or specified) and checks if they are a vampire.
     * If they are a vampire, displays detailed status. If not, sends an appropriate message.
     * 
     * @param sender The command sender.
     * @param command The command being executed.
     * @param label The alias used for the command.
     * @param args The command arguments.
     * @return True if the command was handled, false otherwise.
     */
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        Player targetPlayer;
        boolean showingOther = args.length > 0;

        if (showingOther) {
            // Check permission specifically for viewing others
            if (!sender.hasPermission(VampirePermission.SHOW_OTHER)) { 
                // Use a specific message key for lack of permission to see others
                VampireMessages.sendLocalized(sender, "command.show.no_permission_other"); // Need lang key
                return true;
            }
            
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                VampireMessages.sendLocalized(sender, "player.not_online", args[0]);
                return true;
            }
        } else {
            // If no arguments, target is the sender
            if (!(sender instanceof Player)) {
                VampireMessages.sendLocalized(sender, "command.error.must_be_player_or_specify"); // Need lang key
                return true;
            }
            targetPlayer = (Player) sender;
            // Basic 'vampire.show' permission checked by VCommand superclass
        }
        
        VampirePlayer vampirePlayer = vampireManager.getCachedVampirePlayer(targetPlayer.getUniqueId());

        // Check if data exists
        if (vampirePlayer == null) {
             VampireMessages.sendLocalized(sender, "command.error.player_data_not_found"); // Use generic data not found
            return true;
        }
        
        // Check if the target is actually a vampire
        if (!vampirePlayer.isVampire()) {
             VampireMessages.sendLocalized(sender, "command.show.target_not_vampire", targetPlayer.getName()); // Need lang key
            return true;
        }
        
        // Target is a vampire, display their status
        displayVampireStatus(sender, vampirePlayer, targetPlayer);
        
        return true;
    }
    
    /**
     * Displays the detailed vampire status information for the target player.
     * Assumes the target player is confirmed to be a vampire.
     * 
     * @param sender The command sender who will receive the information.
     * @param vampirePlayer The {@link VampirePlayer} data object for the target.
     * @param targetPlayer The Bukkit {@link Player} object for the target.
     */
    private void displayVampireStatus(CommandSender sender, VampirePlayer vampirePlayer, Player targetPlayer) {
        // Use localized messages from en.yml under 'command.show.display' or similar structure
        // Serialize the Component display name to a legacy string for the placeholder
        String displayName = LegacyComponentSerializer.legacySection().serialize(targetPlayer.displayName());
        VampireMessages.sendLocalized(sender, "command.show.display.header", displayName); 
        
        double maxBlood = vampireManager.getEffectiveMaxBlood(vampirePlayer);
        VampireMessages.sendLocalized(sender, "command.show.display.blood", 
            String.format("%.1f", vampirePlayer.getBlood()), 
            String.format("%.1f", maxBlood));

        VampireMessages.sendLocalized(sender, "command.show.display.level", 
            String.valueOf(vampirePlayer.getVampireLevel())); // Assuming level is an int

        // Display modes
        VampireMessages.sendLocalized(sender, "command.show.display.modes_header");
        VampireMessages.sendLocalized(sender, "command.show.display.mode_entry", 
            VampireMessages.getLocalizedMessage("vampire.mode.bloodlust.name"), // Get mode name from lang
            formatBoolean(vampirePlayer.isBloodlusting())); 
        VampireMessages.sendLocalized(sender, "command.show.display.mode_entry", 
            VampireMessages.getLocalizedMessage("vampire.mode.intent.name"), // Get mode name from lang
            formatBoolean(vampirePlayer.isIntending()));
        VampireMessages.sendLocalized(sender, "command.show.display.mode_entry", 
            VampireMessages.getLocalizedMessage("vampire.mode.nightvision.name"), // Get mode name from lang
            formatBoolean(vampirePlayer.isUsingNightVision()));
            
        // Add other details as needed (e.g., last shriek time, etc.)
    }
    
    /**
     * Formats a boolean value using localized messages for ON/OFF status.
     * 
     * @param value The boolean value.
     * @return A formatted string representing the boolean status (e.g., "&aON", "&cOFF").
     */
    private String formatBoolean(boolean value) {
        // Consider adding keys like 'status.on' and 'status.off' to en.yml
        return value ? "&aON" : "&cOFF"; 
    }
    
    /**
     * Provides tab completions for the /vampire show command.
     * Suggests online player names if the sender has permission to view others.
     * 
     * @param sender The command sender.
     * @param command The command being executed.
     * @param label The alias used for the command.
     * @param args The command arguments.
     * @return A list of suggested player names or an empty list.
     */
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Suggest player names only for the first argument if permission allows
        if (args.length == 1 && sender.hasPermission(VampirePermission.SHOW_OTHER)) {
            String currentArg = args[0].toLowerCase();
            // Filter online players starting with the argument
            return Bukkit.getOnlinePlayers().stream()
                       .map(Player::getName)
                       .filter(name -> name.toLowerCase().startsWith(currentArg))
                       .collect(Collectors.toList());
        }
        // No completions for other arguments or without permission
        return new ArrayList<>();
    }
} 