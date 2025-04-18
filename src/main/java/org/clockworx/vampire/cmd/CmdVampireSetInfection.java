package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for setting a player's infection level.
 */
public class CmdVampireSetInfection extends CmdVampireSetAbstract {
    
    /**
     * Creates a new set infection command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireSetInfection(VampirePlugin plugin) {
        super(plugin, "infection", "vampire.set.infection");
    }
    
    @Override
    protected boolean setValue(VampirePlayer vampirePlayer, Player player, String valueStr, CommandSender sender) {
        // Check if value is provided
        if (valueStr == null) {
            sendError(sender, getMessage("command.set.infection.usage"));
            return false;
        }
        
        // Parse value
        double value;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            sendError(sender, getMessage("command.set.infection.invalid"));
            return false;
        }
        
        // Validate value
        if (value < 0 || value > 100) {
            sendError(sender, getMessage("command.set.infection.range"));
            return false;
        }
        
        // Set the value
        vampirePlayer.setInfectionLevel(value);
        return true;
    }
    
    @Override
    protected String getValueName() {
        return "infection level";
    }
    
    @Override
    protected void addValueCompletions(List<String> completions) {
        completions.add("0");
        completions.add("25");
        completions.add("50");
        completions.add("75");
        completions.add("100");
    }
} 