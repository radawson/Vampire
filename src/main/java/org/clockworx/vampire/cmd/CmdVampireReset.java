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

/**
 * Command class for resetting a player's vampire data (curing, removing infection/blood).
 */
public class CmdVampireReset extends VCommand {

    private final VampireManager vampireManager;

    public CmdVampireReset(VampirePlugin plugin) {
        // Use the specific permission constant
        super(plugin, "reset", VampirePermission.RESET, 
              "Reset a player's vampire data", "<player>");
        this.vampireManager = plugin.getVampireManager();
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Permission already checked by VCommand

        if (args.length < 1) {
            sendError(sender, VampireMessages.getLocalizedMessage("command.error.missing_argument", "player"));
            // Optional: Send usage message
            return true;
        }

        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            sendError(sender, VampireMessages.getLocalizedMessage("player.not_online", targetName));
            return true;
        }

        VampirePlayer targetVampirePlayer = vampireManager.getCachedVampirePlayer(targetPlayer.getUniqueId());
        if (targetVampirePlayer == null) {
            sendError(sender, VampireMessages.getLocalizedMessage("command.error.player_data_not_found"));
            return true;
        }

        // Check if player actually needs resetting
        boolean wasVampire = targetVampirePlayer.isVampire();
        boolean wasInfected = targetVampirePlayer.isInfected();

        if (!wasVampire && !wasInfected) {
            VampireMessages.sendLocalized(sender, "command.reset.not_needed", targetName); // Need lang key
            return true;
        }

        // Perform the reset using VampireManager
        String reason = "Reset by command by " + sender.getName();
        // Setting status to false handles removing vampirism, infection, blood, modes.
        vampireManager.setVampireStatus(targetVampirePlayer.getUuid(), false, reason); 

        // Send feedback
        VampireMessages.sendLocalized(sender, "command.reset.success_sender", targetName);
        VampireMessages.sendLocalized(targetPlayer, "command.reset.success_target");
        
        VampireMessages.debug("Reset player " + targetName + " requested by " + sender.getName());

        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            // Complete player names
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
} 