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
            VampireMessages.sendLocalized(sender, "command.error.missing_argument", "player");
            return true;
        }

        // Resolve target player (online only for reset command)
        TargetResolution target = resolveTargetPlayer(sender, args, 0, false);
        if (target == null) {
            return true; // Error message already sent
        }
        
        // Reset command requires online player
        if (target.onlinePlayer == null) {
            VampireMessages.sendLocalized(sender, "player.not_online", target.name);
            return true;
        }

        VampirePlayer targetVampirePlayer = vampireManager.getCachedVampirePlayer(target.uuid);
        if (targetVampirePlayer == null) {
            VampireMessages.sendLocalized(sender, "command.error.player_data_not_found", target.name);
            return true;
        }

        // Check if player actually needs resetting
        boolean wasVampire = targetVampirePlayer.isVampire();
        boolean wasInfected = targetVampirePlayer.isInfected();

        if (!wasVampire && !wasInfected) {
            VampireMessages.sendLocalized(sender, "command.reset.not_needed", target.name);
            return true;
        }

        // Perform the reset using VampireManager
        String reason = "Reset by command by " + sender.getName();
        // Setting status to false handles removing vampirism, infection, blood, modes.
        vampireManager.setVampireStatus(targetVampirePlayer.getUuid(), false, reason); 

        // Send feedback
        VampireMessages.sendLocalized(sender, "command.reset.success_sender", target.name);
        VampireMessages.sendLocalized(target.onlinePlayer, "command.reset.success_target");
        
        VampireMessages.debug("Reset player " + target.name + " requested by " + sender.getName());

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