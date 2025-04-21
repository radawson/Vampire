package org.clockworx.vampire.cmd;

import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        super(plugin, "Bloodlust", VampirePermission.MODE_BLOODLUST);
    }
    
    @Override
    protected void executeMode(Player player, VampirePlayer vampirePlayer, VampireManager manager, String[] args)
    {
        boolean currentValue = vampirePlayer.isBloodlusting();
        boolean newValue = !currentValue;

        manager.setModeBloodlust(player.getUniqueId(), newValue);
        
        String messageKey = newValue ? "mode.bloodlust.enabled" : "mode.bloodlust.disabled";
        VampireMessages.sendLocalized(player, messageKey);
    }
    
    @Override
    protected List<String> getModeCompletions(String partial)
    {
        List<String> options = Arrays.asList("on", "off");
        return options.stream()
                    .filter(opt -> opt.startsWith(partial.toLowerCase()))
                    .collect(Collectors.toList());
    }
} 