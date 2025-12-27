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
        super(plugin, "Intent", VampirePermission.MODE_INTENT, 
              "Toggle infection intent mode", "", true);
    }
    
    @Override
    protected void executeMode(Player player, VampirePlayer vampirePlayer, VampireManager manager, String[] args) {
        boolean currentValue = vampirePlayer.isIntending();
        boolean newValue = !currentValue;

        manager.setModeIntent(player.getUniqueId(), newValue);
        
        String messageKey = newValue ? "vampire.mode.intent.enabled" : "vampire.mode.intent.disabled";
        VampireMessages.sendLocalized(player, messageKey);
    }
    
    @Override
    protected List<String> getModeCompletions(String partial) {
        List<String> options = Arrays.asList("on", "off");
        return options.stream()
                    .filter(opt -> opt.startsWith(partial.toLowerCase()))
                    .collect(Collectors.toList());
    }
} 