package org.clockworx.vampire.cmd;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Command to set a player's vampire status (true or false).
 */
public class CmdVampireSetVampire extends CmdVampireSetAbstract {
    
    public CmdVampireSetVampire(VampirePlugin plugin) {
        super(plugin, "vampire", VampirePermission.SET, 
              "Set a player's vampire status", "<player> <true|false>");
    }

    @Override
    protected boolean setValue(VampirePlayer targetVampirePlayer, Player targetPlayer, String valueStr, CommandSender sender, VampireManager manager) {
        boolean targetValue;
        // Parse boolean value
        if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("on") || valueStr.equals("1")) {
            targetValue = true;
        } else if (valueStr.equalsIgnoreCase("false") || valueStr.equalsIgnoreCase("off") || valueStr.equals("0")) {
            targetValue = false;
        } else {
            sendError(sender, "Invalid boolean value: " + valueStr + ". Use true/false.");
            return false;
        }

        // Check specific sub-permission based on target value
        String requiredPerm = targetValue ? VampirePermission.SET_VAMPIRE_TRUE : VampirePermission.SET_VAMPIRE_FALSE;
        if (!sender.hasPermission(requiredPerm)) {
            sendError(sender, getMessage("no_permission"));
            return false;
        }

        // Call the manager to set the status
        String reason = "Set by command by " + sender.getName();
        manager.setVampireStatus(targetVampirePlayer.getUuid(), targetValue, reason);

        // Send feedback (Manager might also send feedback to target player)
        VampireMessages.sendLocalized(sender, "command.set.success.vampire", targetPlayer.getName(), String.valueOf(targetValue));

        return true;
    }

    @Override
    protected String getValueName() {
        return "Vampire Status";
    }

    @Override
    protected void addValueCompletions(List<String> completions, String currentInput) {
        List<String> options = Arrays.asList("true", "false");
        completions.addAll(
            options.stream()
                   .filter(opt -> opt.startsWith(currentInput.toLowerCase()))
                   .collect(Collectors.toList())
        );
    }
} 