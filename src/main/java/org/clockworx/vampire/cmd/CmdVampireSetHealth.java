package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for setting a player's health level.
 */
public class CmdVampireSetHealth extends CmdVampireSetAbstract {
    
    /**
     * Creates a new set health command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireSetHealth(VampirePlugin plugin) {
        super(plugin, "health", "vampire.set.health");
    }
    
    @Override
    protected boolean setValue(VampirePlayer vampirePlayer, Player player, String valueStr, CommandSender sender) {
        // Check if value is provided
        if (valueStr == null) {
            sendError(sender, getMessage("command.set.health.usage"));
            return false;
        }
        
        // Parse value
        double value;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            sendError(sender, getMessage("command.set.health.invalid"));
            return false;
        }
        
        // Validate value
        if (value < 0 || value > player.getMaxHealth()) {
            sendError(sender, getMessage("command.set.health.range")
                .replace("%max%", String.valueOf(player.getMaxHealth())));
            return false;
        }
        
        // Set the value
        player.setHealth(value);
        return true;
    }
    
    @Override
    protected String getValueName() {
        return "health level";
    }
    
    @Override
    protected void addValueCompletions(List<String> completions) {
        completions.add("1");
        completions.add("5");
        completions.add("10");
        completions.add("15");
        completions.add("20");
    }
} 