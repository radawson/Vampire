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
 * Command to set a player's food level.
 */
public class CmdVampireSetFood extends CmdVampireSetAbstract {
    
    /**
     * Creates a new set food command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireSetFood(VampirePlugin plugin) {
        super(plugin, "food", VampirePermission.SET_FOOD);
    }
    
    @Override
    protected boolean setValue(VampirePlayer targetVampirePlayer, Player targetPlayer, String valueStr, CommandSender sender, VampireManager manager) {
        int targetValue;
        try {
            targetValue = Integer.parseInt(valueStr);
            // Clamp value between 0 and 20 (Minecraft food levels)
            targetValue = Math.max(0, Math.min(20, targetValue));
        } catch (NumberFormatException e) {
            sendError(sender, "Invalid integer format: " + valueStr + ". Use a whole number between 0 and 20.");
            return false;
        }

        // Permission already checked by VCommand

        // Set food level directly on the Bukkit Player object
        targetPlayer.setFoodLevel(targetValue);

        // Send feedback
        VampireMessages.sendLocalized(sender, "command.set.success.food", targetPlayer.getName(), String.valueOf(targetValue));

        return true;
    }
    
    @Override
    protected String getValueName() {
        return "Food Level";
    }
    
    @Override
    protected void addValueCompletions(List<String> completions, String currentInput) {
        // Suggest common food levels
        List<String> options = Arrays.asList("0", "10", "20");
        completions.addAll(options);
    }
} 