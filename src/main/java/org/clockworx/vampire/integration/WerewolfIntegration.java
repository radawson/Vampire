package org.clockworx.vampire.integration;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.clockworx.vampire.VampirePlugin;

/**
 * Handles integration with the Werewolf plugin.
 * Provides methods to check if a player is a werewolf and prevent conflicts.
 */
public class WerewolfIntegration {
    
    private final VampirePlugin plugin;
    private Plugin werewolfPlugin;
    private boolean available;
    
    /**
     * Creates a new WerewolfIntegration instance.
     *
     * @param plugin The VampirePlugin instance.
     */
    public WerewolfIntegration(VampirePlugin plugin) {
        this.plugin = plugin;
        this.werewolfPlugin = plugin.getServer().getPluginManager().getPlugin("Werewolf");
        this.available = werewolfPlugin != null && werewolfPlugin.isEnabled();
        
        if (available) {
            plugin.getLogger().info("Werewolf plugin integration enabled.");
        } else {
            plugin.getLogger().info("Werewolf plugin not found or disabled. Integration unavailable.");
        }
    }
    
    /**
     * Checks if the Werewolf plugin is available and enabled.
     *
     * @return True if Werewolf plugin is available, false otherwise.
     */
    public boolean isAvailable() {
        return available;
    }
    
    /**
     * Checks if a player is a werewolf.
     * This method attempts to use the Werewolf plugin's API if available.
     *
     * @param player The player to check.
     * @return True if the player is a werewolf, false otherwise.
     */
    public boolean isWerewolf(Player player) {
        if (!available || player == null) {
            return false;
        }
        
        try {
            // Try to access Werewolf plugin's API
            // This assumes Werewolf exposes a static method or manager
            // We'll use reflection to safely access it
            Class<?> werewolfPluginClass = werewolfPlugin.getClass();
            
            // Try to get WerewolfPlugin.getInstance() (static method)
            java.lang.reflect.Method getInstanceMethod = werewolfPluginClass.getMethod("getInstance");
            Object werewolfInstance = getInstanceMethod.invoke(null);
            
            // Try to get WerewolfManager
            java.lang.reflect.Method getManagerMethod = werewolfPluginClass.getMethod("getWerewolfManager");
            Object werewolfManager = getManagerMethod.invoke(werewolfInstance);
            
            // Try to call isWerewolf method with UUID
            java.lang.reflect.Method isWerewolfMethod = werewolfManager.getClass()
                .getMethod("isWerewolf", UUID.class);
            Boolean result = (Boolean) isWerewolfMethod.invoke(werewolfManager, player.getUniqueId());
            
            return result != null && result;
        } catch (Exception e) {
            // If reflection fails, assume player is not a werewolf
            plugin.debug("Failed to check werewolf status via API: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a player is a werewolf by UUID.
     *
     * @param uuid The player's UUID.
     * @return True if the player is a werewolf, false otherwise.
     */
    public boolean isWerewolf(UUID uuid) {
        if (!available || uuid == null) {
            return false;
        }
        
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            return isWerewolf(player);
        }
        
        // If player is offline, we can't check via API
        // Return false as we can't determine status
        return false;
    }
    
    /**
     * Checks if transformation should be prevented due to werewolf conflict.
     *
     * @param player The player attempting to transform.
     * @return True if transformation should be prevented, false otherwise.
     */
    public boolean shouldPreventTransformation(Player player) {
        if (!available) {
            return false;
        }
        
        return isWerewolf(player);
    }
}

