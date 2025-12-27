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
import org.clockworx.vampire.level.LevelManager;
import org.clockworx.vampire.level.VampireLevel;
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
    private final LevelManager levelManager;

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
        this.levelManager = plugin.getLevelManager();
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
        boolean showingOther = args.length > 0;

        if (showingOther) {
            // Check permission specifically for viewing others
            if (!sender.hasPermission(VampirePermission.SHOW_OTHER)) { 
                // Use a specific message key for lack of permission to see others
                VampireMessages.sendLocalized(sender, "command.show.no_permission_other");
                return true;
            }
        }
        // Basic 'vampire.show' permission checked by VCommand superclass

        // Resolve target player (online only for show command)
        TargetResolution target = resolveTargetPlayer(sender, args, args.length > 0 ? 0 : -1, false);
        if (target == null) {
            return true; // Error message already sent
        }
        
        // Show command requires online player
        if (target.onlinePlayer == null) {
            VampireMessages.sendLocalized(sender, "player.not_online", target.name);
            return true;
        }
        
        Player targetPlayer = target.onlinePlayer;
        VampirePlayer vampirePlayer = vampireManager.getCachedVampirePlayer(target.uuid);

        // Check if data exists
        if (vampirePlayer == null) {
            VampireMessages.sendLocalized(sender, "command.error.player_data_not_found", target.name);
            return true;
        }
        
        // Check if the target is actually a vampire
        if (!vampirePlayer.isVampire()) {
             VampireMessages.sendLocalized(sender, "command.show.target_not_vampire", target.name);
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
        // Get level data
        int currentLevel = vampirePlayer.getVampireLevel();
        VampireLevel levelData = levelManager.getLevelData(currentLevel);

        // Serialize the Component display name to a legacy string for the placeholder
        String displayName = LegacyComponentSerializer.legacySection().serialize(targetPlayer.displayName());
        VampireMessages.sendLocalized(sender, "command.show.display.header", displayName); 
        
        // Use level data for max blood
        double maxBlood = levelData.maxBlood();
        VampireMessages.sendLocalized(sender, "command.show.display.blood", 
            String.format("%.1f", vampirePlayer.getBlood()), 
            String.format("%.1f", maxBlood));

        // Use level description or level number
        VampireMessages.sendLocalized(sender, "command.show.display.level", 
            String.valueOf(currentLevel), 
            levelData.description());

        // Display modes
        VampireMessages.sendLocalized(sender, "command.show.display.modes_header");
        VampireMessages.sendLocalized(sender, "command.show.display.mode_entry", 
            VampireMessages.getLocalizedMessage("vampire.mode.bloodlust.name"),
            formatBoolean(vampirePlayer.isBloodlusting())); 
        VampireMessages.sendLocalized(sender, "command.show.display.mode_entry", 
            VampireMessages.getLocalizedMessage("vampire.mode.intent.name"),
            formatBoolean(vampirePlayer.isIntending()));
        VampireMessages.sendLocalized(sender, "command.show.display.mode_entry", 
            VampireMessages.getLocalizedMessage("vampire.mode.nightvision.name"),
            formatBoolean(vampirePlayer.isUsingNightVision()));
            
        // Calculate and display current sun exposure
        double irradiation = org.clockworx.vampire.util.SunUtil.calcPlayerIrradiation(targetPlayer);
        VampireMessages.sendLocalized(sender, "command.show.display.sun_level",
            String.format("%.1f%%", irradiation * 100.0)); // Format as percentage
    }
    
    /**
     * Formats a boolean value using localized messages for ON/OFF status.
     * 
     * @param value The boolean value.
     * @return A formatted string representing the boolean status (e.g., "&aON", "&cOFF").
     */
    private String formatBoolean(boolean value) {
        // Use localized keys for ON/OFF
        return VampireMessages.getLocalizedMessage(value ? "general.status.active" : "general.status.inactive"); 
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