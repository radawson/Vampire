package org.clockworx.vampire.cmd;

import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;
import org.bukkit.attribute.AttributeInstance;

import java.util.List;
import java.util.Arrays;

/**
 * Command to set a player's health level.
 */
public class CmdVampireSetHealth extends CmdVampireSetAbstract {
    
    /**
     * Creates a new set health command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireSetHealth(VampirePlugin plugin) {
        super(plugin, "health", VampirePermission.SET_HEALTH);
    }
    
    @Override
    protected boolean setValue(VampirePlayer targetVampirePlayer, Player targetPlayer, String valueStr, CommandSender sender, VampireManager manager) {
        double targetValue;
        double maxHealth = 20.0; // Default value
        try {
            AttributeInstance maxHealthAttribute = targetPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttribute != null) {
                maxHealth = maxHealthAttribute.getValue();
            }
            targetValue = Double.parseDouble(valueStr);
            targetValue = Math.max(0.0, Math.min(maxHealth, targetValue));
        } catch (NumberFormatException e) {
            sendError(sender, "Invalid number format: " + valueStr + ". Use a number between 0 and the player's max health (" + String.format("%.1f", maxHealth) + ").");
            return false;
        }

        // Permission already checked by VCommand

        // Set health directly on the Bukkit Player object
        targetPlayer.setHealth(targetValue);

        // Send feedback
        VampireMessages.sendLocalized(sender, "command.set.success.health", targetPlayer.getName(), String.format("%.1f", targetValue)); // Need new lang key

        return true;
    }
    
    @Override
    protected String getValueName() {
        return "Health Level";
    }
    
    @Override
    protected void addValueCompletions(List<String> completions, String currentInput) {
        // Suggest common health levels or percentages?
        List<String> options = Arrays.asList("0", "10", "20"); // Max health varies, so absolute numbers might be less useful
        completions.addAll(options);
        // TODO: Could try getting the target player's max health here if possible for better suggestions?
    }
} 