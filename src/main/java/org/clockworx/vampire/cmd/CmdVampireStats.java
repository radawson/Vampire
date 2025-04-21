package org.clockworx.vampire.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.ResourceUtil;
import org.clockworx.vampire.util.VampireMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command for displaying vampire statistics for self or others.
 */
public class CmdVampireStats extends VCommand {

    private final VampireManager vampireManager;

    public CmdVampireStats(VampirePlugin plugin) {
        // Use the base permission for self-checking
        super(plugin, "stats", VampirePermission.STATS);
        this.vampireManager = plugin.getVampireManager();
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        Player targetPlayer = null;
        VampirePlayer targetVampirePlayer = null;
        UUID targetUUID = null;

        if (args.length == 0) {
            // Target self
            if (!isPlayer(sender)) {
                sendError(sender, ResourceUtil.getMessage("command.error.must_be_player_or_specify")); // Need lang key
                return true;
            }
            // Base permission VampirePermission.STATS already checked by VCommand
            targetPlayer = (Player) sender;
            targetUUID = targetPlayer.getUniqueId();
            targetVampirePlayer = vampireManager.getCachedVampirePlayer(targetUUID);
        } else {
            // Target other player
            if (!sender.hasPermission(VampirePermission.STATS_OTHER)) {
                sendError(sender, ResourceUtil.getMessage("command.no_permission"));
                return true;
            }
            String targetName = args[0];
            targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer == null || !targetPlayer.isOnline()) {
                // Try loading offline data if implemented? For now, assume online only.
                sendError(sender, ResourceUtil.getMessage("player.not_online", targetName));
                return true;
            }
            targetUUID = targetPlayer.getUniqueId();
            targetVampirePlayer = vampireManager.getCachedVampirePlayer(targetUUID);
        }

        // Check if player data was found
        if (targetVampirePlayer == null) {
            // This might happen if player joined before manager could cache them, or an error occurred.
            sendError(sender, ResourceUtil.getMessage("command.error.player_data_not_found")); // Need lang key
            return true;
        }

        // Display Stats
        displayStats(sender, targetVampirePlayer, targetPlayer);
        return true;
    }

    private void displayStats(CommandSender sender, VampirePlayer vp, Player player) {
        String targetName = (player != null) ? player.getName() : vp.getName(); // Use player name if online
        
        // Use VampireMessages for localized and formatted output
        VampireMessages.sendLocalized(sender, "vampire.status.header", targetName);
        VampireMessages.sendLocalized(sender, "vampire.status.is_vampire", 
            vp.isVampire() ? "&aYes" : "&cNo"); // Direct color codes ok for simple cases?

        if (vp.isVampire()) {
            double maxBlood = vampireManager.getEffectiveMaxBlood(vp);
            VampireMessages.sendLocalized(sender, "vampire.status.blood", 
                String.format("%.2f", vp.getBlood()), 
                String.format("%.2f", maxBlood));
            VampireMessages.sendLocalized(sender, "vampire.status.level", 
                String.valueOf(vp.getVampireLevel()));
            VampireMessages.sendLocalized(sender, "vampire.status.mode_bloodlust", 
                vp.isBloodlusting() ? "&aActive" : "&cInactive");
            VampireMessages.sendLocalized(sender, "vampire.status.mode_intent", 
                vp.isIntending() ? "&aActive" : "&cInactive");
             VampireMessages.sendLocalized(sender, "vampire.status.mode_nightvision", 
                vp.isUsingNightVision() ? "&aActive" : "&cInactive");
        } else {
            VampireMessages.sendLocalized(sender, "vampire.status.infection", 
                String.format("%.1f", vp.getInfectionLevel() * 100));
            if (vp.getInfectionReason() != null && !vp.getInfectionReason().isEmpty()) {
                VampireMessages.sendLocalized(sender, "vampire.status.infection_reason", 
                    vp.getInfectionReason());
            }
        }
        // Add any other relevant stats here (e.g., last damage time, etc.)
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission(VampirePermission.STATS_OTHER)) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
} 