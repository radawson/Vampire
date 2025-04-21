package org.clockworx.vampire.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command for showing vampire plugin information.
 * Retrieves data from VampireManager and displays relevant status.
 */
public class CmdVampireShow extends VCommand {
    
    private final VampireManager vampireManager;

    /**
     * Creates a new show command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireShow(VampirePlugin plugin) {
        super(plugin, "show", VampirePermission.SHOW);
        this.vampireManager = plugin.getVampireManager();
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        Player targetPlayer;
        boolean showingOther = args.length > 0;

        if (showingOther) {
            if (!sender.hasPermission(VampirePermission.SHOW_OTHER)) { 
                sendError(sender, getMessage("command.show.no_permission_other"));
                return true;
            }
            
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                sendError(sender, getMessage("player.not_online").replace("%player%", args[0]));
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sendError(sender, getMessage("command.player_only"));
                return true;
            }
            targetPlayer = (Player) sender;
        }
        
        VampirePlayer vampirePlayer = vampireManager.getCachedVampirePlayer(targetPlayer.getUniqueId());

        if (vampirePlayer == null) {
            sendError(sender, getMessage("command.player_data_not_found"));
            return true;
        }
        
        displayVampireStatus(sender, vampirePlayer, targetPlayer);
        
        return true;
    }
    
    /**
     * Displays the vampire status information for a player.
     * 
     * @param sender The command sender
     * @param vampirePlayer The vampire player data (POJO)
     * @param targetPlayer The target player entity
     */
    private void displayVampireStatus(CommandSender sender, VampirePlayer vampirePlayer, Player targetPlayer) {
        boolean self = (sender == targetPlayer);
        String name = targetPlayer.getDisplayName();
        String namePlaceholder = self ? "You" : name;
        String verbPlaceholder = self ? "are" : "is";
        
        sendInfo(sender, getMessage("vampire.status.header"));
        sendInfo(sender, getMessage("vampire.status.name").replace("%player%", name));
        
        if (vampirePlayer.isVampire()) {
            sendInfo(sender, "&aVampire Status: &cVampire");
            displayVampireDetails(sender, vampirePlayer, targetPlayer);
        } else if (vampirePlayer.isInfected()) {
             sendInfo(sender, "&aVampire Status: &eInfected");
             sendInfo(sender, getMessage("vampire.status.infection")
                .replace("%infection%", String.format("%.1f", vampirePlayer.getInfectionLevel() * 100)));
             String reason = vampirePlayer.getInfectionReason();
             if (reason != null && !reason.isEmpty()) {
                 sendInfo(sender, "&7Reason: &f" + reason);
             }
        } else {
            sendInfo(sender, "&aVampire Status: &fHuman");
        }
    }
    
    /**
     * Displays detailed vampire information.
     */
    private void displayVampireDetails(CommandSender sender, VampirePlayer vampirePlayer, Player player) {
        double maxBlood = vampireManager.getEffectiveMaxBlood(vampirePlayer);
        sendInfo(sender, getMessage("vampire.status.blood")
            .replace("%blood%", String.format("%.1f / %.1f", vampirePlayer.getBlood(), maxBlood)));

        sendInfo(sender, "&7Level: &f" + vampirePlayer.getVampireLevel());

        sendInfo(sender, "&7Modes:");
        sendInfo(sender, "  " + getMessage("vampire.mode.bloodlust") + ": " + formatBoolean(vampirePlayer.isBloodlusting()));
        sendInfo(sender, "  " + getMessage("vampire.mode.intent") + ": " + formatBoolean(vampirePlayer.isIntending()));
        sendInfo(sender, "  " + getMessage("vampire.mode.nightvision") + ": " + formatBoolean(vampirePlayer.isUsingNightVision()));
    }
    
    /**
     * Formats a boolean value using message keys for ON/OFF.
     */
    private String formatBoolean(boolean value) {
        return value ? "&aON" : "&cOFF";
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission(VampirePermission.SHOW_OTHER)) {
            String currentArg = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                       .map(Player::getName)
                       .filter(name -> name.toLowerCase().startsWith(currentArg))
                       .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
} 