package org.clockworx.vampire.cmd;

import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to toggle infection intent mode for vampires.
 * When enabled, vampires have a higher chance to infect players during combat.
 */
public class CmdVampireModeIntend extends CmdVampireModeAbstract {
    
    /**
     * Creates a new intend mode command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireModeIntend(VampirePlugin plugin) {
        super(plugin, "Infection Intent", "vampire.mode.intend");
    }
    
    @Override
    protected void executeMode(Player player, VampirePlayer vampirePlayer, String[] args) {
        boolean newValue = !vampirePlayer.isIntending();
        vampirePlayer.setIntending(newValue);
        
        if (newValue) {
            player.sendMessage(getMessage("mode.intend.enabled"));
        } else {
            player.sendMessage(getMessage("mode.intend.disabled"));
        }
    }
    
    @Override
    protected List<String> getModeCompletions(String partial) {
        List<String> completions = new ArrayList<>();
        if ("on".startsWith(partial.toLowerCase())) {
            completions.add("on");
        }
        if ("off".startsWith(partial.toLowerCase())) {
            completions.add("off");
        }
        return completions;
    }
} 