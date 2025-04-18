package org.clockworx.vampire.cmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for setting a player's vampire status.
 */
public class CmdVampireSetVampire extends CmdVampireSetAbstract {
    
    /**
     * Creates a new set vampire command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireSetVampire(VampirePlugin plugin) {
        super(plugin, "vampire", "vampire.set.vampire");
    }
    
    @Override
    protected boolean setValue(VampirePlayer vampirePlayer, Player player, String valueStr, CommandSender sender) {
        // Parse value
        Boolean value = null;
        
        if (valueStr != null) {
            valueStr = valueStr.toLowerCase();
            if (valueStr.equals("true") || valueStr.equals("yes") || valueStr.equals("1") || valueStr.equals("on")) {
                value = true;
            } else if (valueStr.equals("false") || valueStr.equals("no") || valueStr.equals("0") || valueStr.equals("off")) {
                value = false;
            } else {
                sendError(sender, getMessage("command.set.vampire.invalid"));
                return false;
            }
        } else {
            // Toggle current value
            value = !vampirePlayer.isVampire();
        }
        
        // Set the value
        vampirePlayer.setVampire(value);
        
        // Notify the player
        if (player.isOnline()) {
            if (value) {
                player.sendMessage(ChatColor.RED + "You are now a vampire!");
            } else {
                player.sendMessage(ChatColor.GREEN + "You are no longer a vampire.");
            }
        }
        
        return true;
    }
    
    @Override
    public String getName() {
        return "vampire";
    }
    
    @Override
    protected String getValueName() {
        return "vampire status";
    }
    
    @Override
    protected void addValueCompletions(List<String> completions) {
        completions.add("true");
        completions.add("false");
        completions.add("toggle");
    }
} 