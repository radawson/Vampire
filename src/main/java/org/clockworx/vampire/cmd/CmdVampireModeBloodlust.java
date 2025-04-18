package org.clockworx.vampire.cmd;

import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to toggle bloodlust mode for vampires.
 * Bloodlust mode increases combat damage but drains food faster.
 */
public class CmdVampireModeBloodlust extends CmdVampireModeAbstract
{
    /**
     * Creates a new bloodlust mode command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireModeBloodlust(VampirePlugin plugin)
    {
        super(plugin, "Bloodlust", "vampire.mode.bloodlust");
    }
    
    @Override
    protected void executeMode(Player player, VampirePlayer vampirePlayer, String[] args)
    {
        boolean newValue = !vampirePlayer.isBloodlusting();
        vampirePlayer.setBloodlusting(newValue);
        
        if (newValue) {
            player.sendMessage(getMessage("mode.bloodlust.enabled"));
        } else {
            player.sendMessage(getMessage("mode.bloodlust.disabled"));
        }
    }
    
    @Override
    protected List<String> getModeCompletions(String partial)
    {
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