package org.clockworx.vampire.cmd;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Command to toggle nightvision mode for vampires.
 * Nightvision mode allows vampires to see in the dark.
 */
public class CmdVampireModeNightvision extends CmdVampireModeAbstract {
    
    /**
     * Creates a new nightvision mode command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireModeNightvision(VampirePlugin plugin) {
        super(plugin, "Nightvision", VampirePermission.MODE_NIGHTVISION, 
              "Toggle permanent night vision", "", true); // Added toggleable=true
    }
    
    @Override
    protected void executeMode(Player player, VampirePlayer vampirePlayer, VampireManager manager, String[] args) {
        // Check if the feature is enabled globally first
        if (!manager.getPlugin().getVampireConfig().isNightVisionEnabled()) {
            VampireMessages.sendLocalized(player, "mode.nightvision.globally_disabled"); // Need lang key
            return;
        }
        
        boolean currentValue = vampirePlayer.isUsingNightVision();
        boolean newValue = !currentValue;

        // Tell the manager to change the state
        manager.setModeNightvision(player.getUniqueId(), newValue);
        
        // Send feedback based on the *intended* new state
        // The manager/task will handle the actual effect application
        String messageKey = newValue ? "mode.nightvision.enabled" : "mode.nightvision.disabled";
        VampireMessages.sendLocalized(player, messageKey); // Need lang keys
    }
    
    @Override
    protected List<String> getModeCompletions(String partial) {
        List<String> options = Arrays.asList("on", "off");
        return options.stream()
                    .filter(opt -> opt.startsWith(partial.toLowerCase()))
                    .collect(Collectors.toList());
    }
} 