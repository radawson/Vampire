package org.clockworx.vampire.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.clockworx.vampire.VampirePlugin;

/**
 * Listener class for handling altar-related interactions in the Vampire plugin.
 * This class manages player interactions with special altar structures that provide
 * various effects and abilities. The altars include:
 * - Dark Altar: Allows players to become infected with vampirism
 * - Light Altar: Allows players to cure themselves of vampirism or infection
 * 
 * Each altar requires specific materials to construct and resources to activate.
 * The altars are managed by the AltarManager class and their configurations
 * are stored in the plugin's config file.
 */
public class AltarListener implements Listener {
    
    /**
     * Handles player interactions with altar blocks.
     * This method is called when a player interacts with any block and:
     * 1. Checks if the interaction is a right-click on a block
     * 2. Verifies if the block is part of a valid altar structure
     * 3. Processes the altar interaction if valid
     * 4. Cancels the event to prevent normal block usage
     * 
     * The altar interaction is handled by the AltarManager, which:
     * - Validates the altar structure
     * - Checks player permissions
     * - Verifies required resources
     * - Applies altar-specific effects
     * 
     * @param event The PlayerInteractEvent that triggered this handler
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the player right-clicked a block
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        // Check if the block is part of an altar
        if (VampirePlugin.getInstance().getAltarManager().handleBlockInteract(event.getClickedBlock(), event.getPlayer())) {
            // Cancel the event to prevent the player from using the block normally
            event.setCancelled(true);
        }
    }
} 