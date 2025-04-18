package org.clockworx.vampire.cmd;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;

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
        super(plugin, "Nightvision", "vampire.mode.nightvision");
    }
    
    @Override
    protected void executeMode(Player player, VampirePlayer vampirePlayer, String[] args) {
        boolean newValue = !vampirePlayer.isUsingNightVision();
        vampirePlayer.setUsingNightVision(newValue);
        
        // Apply or remove nightvision effect
        if (newValue) {
            player.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(Integer.MAX_VALUE, 0));
            player.sendMessage(getMessage("mode.nightvision.enabled"));
        } else {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            player.sendMessage(getMessage("mode.nightvision.disabled"));
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