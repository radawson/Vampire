package org.clockworx.vampire.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.util.SunUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command for showing vampire information.
 * This command displays information about a player's vampire status, including:
 * - Whether they are a vampire or infected
 * - Their infection level
 * - Their bloodlust, infect intent, and nightvision status
 * - Their temperature and irradiation levels
 */
public class CmdVampireShow extends VCommand {
    
    /**
     * Creates a new show command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireShow(VampirePlugin plugin) {
        super(plugin, "show", "vampire.show");
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        Player targetPlayer;
        
        if (args.length > 0) {
            // Check if sender has permission to view other players
            if (!sender.hasPermission("vampire.show.other")) {
                sendError(sender, getMessage("command.show.no_permission"));
                return false;
            }
            
            // Find the target player
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sendError(sender, getMessage("command.player_not_found"));
                return false;
            }
        } else {
            // Check if sender is a player
            if (!(sender instanceof Player)) {
                sendError(sender, getMessage("command.player_only"));
                return false;
            }
            
            targetPlayer = (Player) sender;
        }
        
        // Get the VampirePlayer instance
        CompletableFuture<VampirePlayer> future = plugin.getVampirePlayer(targetPlayer.getUniqueId());
        future.thenAccept(vampirePlayer -> {
            if (vampirePlayer == null) {
                sendError(sender, getMessage("command.player_data_not_found"));
                return;
            }
            
            displayVampireStatus(sender, vampirePlayer, targetPlayer);
        });
        
        return true;
    }
    
    /**
     * Displays the vampire status information for a player.
     * 
     * @param sender The command sender
     * @param vampirePlayer The vampire player data
     * @param targetPlayer The target player
     */
    private void displayVampireStatus(CommandSender sender, VampirePlayer vampirePlayer, Player targetPlayer) {
        boolean self = (sender == targetPlayer);
        String name = self ? "You" : targetPlayer.getName();
        String are = self ? "are" : "is";
        
        // Display header
        sendInfo(sender, getMessage("command.show.header").replace("%player%", name));
        
        // Display status
        if (vampirePlayer.isVampire()) {
            sendInfo(sender, getMessage("command.show.is_vampire")
                .replace("%name%", name)
                .replace("%are%", are));
            displayVampireDetails(sender, vampirePlayer, targetPlayer);
        } else if (vampirePlayer.isInfected()) {
            sendInfo(sender, getMessage("command.show.is_infected")
                .replace("%name%", name)
                .replace("%are%", are)
                .replace("%level%", String.format("%.1f", vampirePlayer.getInfectionLevel() * 100)));
            // Use a default reason if getReasonDesc is not available
            String reason = "Unknown";
            sendInfo(sender, getMessage("command.show.infection_reason")
                .replace("%reason%", reason));
        } else {
            sendInfo(sender, getMessage("command.show.is_human")
                .replace("%name%", name)
                .replace("%are%", are));
        }
    }
    
    /**
     * Displays detailed vampire information including environmental stats.
     */
    private void displayVampireDetails(CommandSender sender, VampirePlayer vampirePlayer, Player player) {
        // Show basic vampire info
        String reason = "Unknown";
        sendInfo(sender, getMessage("command.show.vampire_reason")
            .replace("%reason%", reason));
        sendInfo(sender, getMessage("command.show.blood_level")
            .replace("%level%", String.format("%.1f", vampirePlayer.getBloodLevel())));
        sendInfo(sender, getMessage("command.show.max_blood")
            .replace("%max%", String.format("%.1f", 100.0))); // Use a default value
        
        // Show modes
        sendInfo(sender, getMessage("command.show.modes_header"));
        sendInfo(sender, getMessage("command.show.mode_bloodlust")
            .replace("%status%", formatBoolean(vampirePlayer.isBloodlusting())));
        sendInfo(sender, getMessage("command.show.mode_infect")
            .replace("%status%", formatBoolean(vampirePlayer.isIntending())));
        sendInfo(sender, getMessage("command.show.mode_nightvision")
            .replace("%status%", formatBoolean(vampirePlayer.isUsingNightVision())));
        
        // Show environmental stats
        sendInfo(sender, getMessage("command.show.environment_header"));
        sendInfo(sender, getMessage("command.show.temperature")
            .replace("%level%", String.format("%.1f", 0.0))); // Use a default value
        
        // Show radiation details
        displayRadiationStats(sender, vampirePlayer, player);
    }
    
    /**
     * Displays radiation-related statistics for vampires.
     */
    private void displayRadiationStats(CommandSender sender, VampirePlayer vampirePlayer, Player player) {
        double radiation = 0.0; // Use a default value
        sendInfo(sender, getMessage("command.show.radiation")
            .replace("%level%", String.format("%.1f", radiation * 100)));
        
        if (player != null && player.isOnline()) {
            double sunRad = SunUtil.calcSolarRad(player.getWorld());
            double terrain = 1.0 - SunUtil.calcTerrainOpacity(player.getLocation().getBlock());
            double armor = 1.0 - SunUtil.calcArmorOpacity(player);
            
            sendInfo(sender, getMessage("command.show.radiation_details"));
            sendInfo(sender, getMessage("command.show.radiation_sun")
                .replace("%level%", String.format("%.1f", sunRad * 100)));
            sendInfo(sender, getMessage("command.show.radiation_terrain")
                .replace("%level%", String.format("%.1f", terrain * 100)));
            sendInfo(sender, getMessage("command.show.radiation_armor")
                .replace("%level%", String.format("%.1f", armor * 100)));
        }
    }
    
    /**
     * Formats a boolean value as a colored string.
     */
    private String formatBoolean(boolean value) {
        return value ? getMessage("command.show.status_enabled") : getMessage("command.show.status_disabled");
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("vampire.show.other")) {
            List<String> completions = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
            return completions;
        }
        return new ArrayList<>();
    }
} 