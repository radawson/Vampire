package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for setting a player's food level.
 */
public class CmdVampireSetFood extends CmdVampireSetAbstract {
    
    /**
     * Creates a new set food command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireSetFood(VampirePlugin plugin) {
        super(plugin, "food", "vampire.set.food");
    }
    
    @Override
    protected boolean setValue(VampirePlayer vampirePlayer, Player player, String valueStr, CommandSender sender) {
        // Check if value is provided
        if (valueStr == null) {
            sendError(sender, getMessage("command.set.food.usage"));
            return false;
        }
        
        // Parse value
        int value;
        try {
            value = Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            sendError(sender, getMessage("command.set.food.invalid"));
            return false;
        }
        
        // Validate value
        if (value < 0 || value > 20) {
            sendError(sender, getMessage("command.set.food.range"));
            return false;
        }
        
        // Set the value
        player.setFoodLevel(value);
        return true;
    }
    
    @Override
    protected String getValueName() {
        return "food level";
    }
    
    @Override
    protected void addValueCompletions(List<String> completions) {
        completions.add("0");
        completions.add("5");
        completions.add("10");
        completions.add("15");
        completions.add("20");
    }
} 