package org.clockworx.vampire.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.clockworx.vampire.VampirePlugin;

/**
 * Listener for plugin enable events to detect when other plugins (like Werewolf) load.
 * This allows bidirectional integration detection regardless of plugin load order.
 */
public class IntegrationListener implements Listener {
    
    private final VampirePlugin plugin;
    
    /**
     * Creates a new IntegrationListener.
     *
     * @param plugin The VampirePlugin instance.
     */
    public IntegrationListener(VampirePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles plugin enable events to detect when Werewolf loads.
     * If Werewolf loads after Vampire, this will initialize the integration.
     *
     * @param event The PluginEnableEvent.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("Werewolf")) {
            // Re-check for Werewolf integration if not already initialized
            if (plugin.getWerewolfIntegration() == null) {
                plugin.checkWerewolfPlugin();
            }
        }
    }
}

