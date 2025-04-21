package org.clockworx.vampire.cmd;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

import java.util.List;
import java.util.Arrays;

/**
 * Command to set a player's infection level.
 */
public class CmdVampireSetInfection extends CmdVampireSetAbstract {
    
    /**
     * Creates a new set infection command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireSetInfection(VampirePlugin plugin) {
        super(plugin, "infection", VampirePermission.SET_INFECTION);
    }
    
    @Override
    protected boolean setValue(VampirePlayer targetVampirePlayer, Player targetPlayer, String valueStr, CommandSender sender, VampireManager manager) {
        double targetValue;
        try {
            // Parse double value (e.g., 0.5, 50, 50%)
            if (valueStr.endsWith("%")) {
                targetValue = Double.parseDouble(valueStr.substring(0, valueStr.length() - 1)) / 100.0;
            } else {
                targetValue = Double.parseDouble(valueStr);
            }
            // Clamp value between 0.0 and 1.0
            targetValue = Math.max(0.0, Math.min(1.0, targetValue));
        } catch (NumberFormatException e) {
            sendError(sender, "Invalid number format: " + valueStr + ". Use a number between 0.0 and 1.0 (or 0% and 100%).");
            return false;
        }

        // Permission already checked by VCommand based on constructor perm

        // Call the manager to set the infection level
        String reason = "Set by command by " + sender.getName();
        manager.setInfectionLevel(targetVampirePlayer.getUuid(), targetValue, reason);

        // Send feedback
        VampireMessages.sendLocalized(sender, "command.set.success.infection", targetPlayer.getName(), String.format("%.1f%%", targetValue * 100)); // Need new lang key

        return true;
    }
    
    @Override
    protected String getValueName() {
        return "Infection Level";
    }
    
    @Override
    protected void addValueCompletions(List<String> completions, String currentInput) {
        // Suggest common percentages or levels
        List<String> options = Arrays.asList("0.0", "0.25", "0.5", "0.75", "1.0", "0%", "25%", "50%", "75%", "100%");
        // No specific filtering based on currentInput for these simple suggestions
        completions.addAll(options);
    }
} 