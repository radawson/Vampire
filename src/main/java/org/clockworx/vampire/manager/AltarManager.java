package org.clockworx.vampire.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.config.VampireConfig;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.event.EventAltarUse;
import org.clockworx.vampire.util.FxUtil;
import org.clockworx.vampire.util.VampireMessages;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.altar.AltarAbstract;
import org.clockworx.vampire.altar.AltarDark;
import org.clockworx.vampire.altar.AltarLight;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Manages the registration and interaction logic for all defined Altars 
 * within the Vampire plugin.
 * 
 * <p>This class is typically instantiated once by the main plugin class. 
 * It initializes all known altar types (e.g., {@link AltarDark}, {@link AltarLight}) 
 * and provides a central point ({@link #handleBlockInteract(Block, Player)}) 
 * for checking if a player interaction corresponds to the use of any valid altar structure.</p>
 */
public class AltarManager {
    
    /** Reference to the main VampirePlugin instance. */
    private final VampirePlugin plugin;
    /** Reference to the VampireManager for accessing player data. */
    private final VampireManager vampireManager;
    /** Reference to the VampireConfig. */
    private final VampireConfig config;
    
    /** 
     * A list holding all registered AltarAbstract instances. 
     * Interactions are checked against each altar in this list.
     */
    private final List<AltarAbstract> altars;
    
    /**
     * Constructs a new AltarManager.
     * Initializes the list of altars and registers the default altar types 
     * (currently AltarDark and AltarLight).
     * 
     * @param plugin The main {@link VampirePlugin} instance.
     */
    public AltarManager(VampirePlugin plugin) {
        this.plugin = plugin;
        // Get manager/config references from the plugin
        this.vampireManager = plugin.getVampireManager(); 
        this.config = plugin.getVampireConfig();
        this.altars = new ArrayList<>();
        
        // --- Register all known Altar types ---
        // Each altar reads its own configuration upon instantiation.
        if (config.isAltarsEnabled()) { // Only register if altars are enabled in config
            registerAltar(new AltarDark(plugin));
            registerAltar(new AltarLight(plugin));
            // Add future altars here...
            plugin.getLogger().info("Registered " + altars.size() + " altars.");
        } else {
            plugin.getLogger().info("Altars are disabled in the config. No altars registered.");
        }
    }
    
    /**
     * Registers a new altar instance to be managed.
     * Adds the altar to the internal list, making it available for interaction checks.
     * 
     * @param altar The {@link AltarAbstract} instance to register.
     */
    public void registerAltar(AltarAbstract altar) {
        if (altar != null) {
            altars.add(altar);
            VampireMessages.debug("Registered altar: " + altar.getName());
        } else {
            VampireMessages.error("Attempted to register a null altar!", null);
        }
    }

    // --- Altar Determination and Validation --- (Helper Methods) ---

    /**
     * Determines if the given block could be the core of any registered altar
     * and validates the surrounding structure based on that altar's configuration.
     * 
     * @param coreBlock The block the player interacted with.
     * @return The specific AltarAbstract instance if the block is a valid core
     *         and the structure matches, otherwise null.
     */
    private AltarAbstract determineAltarType(Block coreBlock) {
        if (coreBlock == null || altars.isEmpty()) return null;
        Material coreMaterial = coreBlock.getType();

        for (AltarAbstract altar : altars) {
            // Check if the interacted block matches the core material for this altar type
            if (altar.getCoreMaterial() == coreMaterial) {
                VampireMessages.debug("Potential " + altar.getName() + " altar found based on core material: " + coreMaterial);
                // If core matches, validate the surrounding structure
                if (isValidAltarStructure(coreBlock, altar)) {
                    VampireMessages.debug("Structure validated for " + altar.getName() + ".");
                    return altar; // Found a matching and valid altar
                }
            }
        }
        return null; // No registered altar matched the core block and structure
    }

    /**
     * Checks if the blocks around the core location form a valid altar structure
     * based on the specific altar's configuration.
     * Uses helper methods from AltarAbstract for block scanning and counting.
     * 
     * @param coreBlock The potential core block.
     * @param altar The specific AltarAbstract instance (used to get required materials/counts).
     * @return true if the structure is valid according to the altar's definition, false otherwise.
     */
    private boolean isValidAltarStructure(Block coreBlock, AltarAbstract altar) {
        Map<Material, Integer> requiredCounts = altar.getMaterialCounts();
        // If only the core material is required (or nothing specific), structure is inherently valid here.
        if (requiredCounts == null || requiredCounts.isEmpty() || (requiredCounts.size() == 1 && requiredCounts.containsKey(altar.getCoreMaterial()))) {
             VampireMessages.debug("Altar " + altar.getName() + " has no specific structure requirements beyond the core block.");
             return true; 
        }

        int searchRadius = config.getAltarSearchRadius();
        double minRatio = config.getAltarMinRatio();

        // 1. Get all non-air blocks within the search radius (excluding the core block itself for counting purposes? TBD)
        // Let's use the static helper from AltarAbstract for now.
        ArrayList<Block> blocks = AltarAbstract.getCubeBlocks(coreBlock, searchRadius);

        // 2. Count the materials found nearby that are required by this altar.
        // We need the *deprecated* static countMaterials here, or reimplement its logic.
        // Let's assume AltarAbstract still has the static helper for now.
        @SuppressWarnings("deprecation")
        Map<Material, Integer> nearbyMaterialCounts = AltarAbstract.countMaterials(blocks, requiredCounts.keySet());

        // 3. Check overall ratio.
        // Use the helper from AltarAbstract instance to calculate sum.
        int requiredMaterialCountSum = altar.sumCollection(requiredCounts.values()); 
        int nearbyMaterialCountSum = altar.sumCollection(nearbyMaterialCounts.values());
        
        // Adjust required sum if core is part of the count, as it's not included in nearbyMaterialCountSum?
        // This depends on whether getCubeBlocks includes the center block. Let's assume it does for now.

        // Handle edge case where only the core block is listed in materials (should have been caught above, but defense)
        if (requiredMaterialCountSum <= 1 && nearbyMaterialCountSum >= 1) {
             return true; // Only core needed, and it's present
        }
        
        // Check ratio if more than just the core is needed
        if (requiredMaterialCountSum > 0 && // Avoid division by zero
            (double)nearbyMaterialCountSum / requiredMaterialCountSum < minRatio) {
            VampireMessages.debug("Altar structure failed ratio check for " + altar.getName() + ". Found: " + nearbyMaterialCountSum + ", Required: " + requiredMaterialCountSum + ", Ratio Needed: " + minRatio);
            return false;
        }

        // 4. Check if minimum count for *each* specific material is met.
        Map<Material, Integer> missingCounts = altar.getMissingMaterialCounts(nearbyMaterialCounts);
        if (!missingCounts.isEmpty()) {
             // Specifically check if the only missing item is the core block itself, 
             // which we know is present because determineAltarType checked it.
             // This handles cases where the core block might be included in the configured count.
             if (missingCounts.size() == 1 && missingCounts.containsKey(altar.getCoreMaterial())) {
                 // The only missing block is the core, which isn't actually missing. Structure is valid.
                  VampireMessages.debug("Altar structure specific counts check for " + altar.getName() + " passed (only core was technically missing in radius scan). Required: " + requiredCounts + " Nearby: " + nearbyMaterialCounts);
             } else {
                 VampireMessages.debug("Altar structure failed specific counts check for " + altar.getName() + ". Missing: " + missingCounts + ". Required: " + requiredCounts + " Nearby: " + nearbyMaterialCounts);
                 return false;
             }
        }

        VampireMessages.debug("Altar structure passed all checks for " + altar.getName() + ". Required: " + requiredCounts + " Nearby: " + nearbyMaterialCounts);
        return true; // All checks passed
    }
    
    /**
     * Handles a player interacting with a block, checking if it triggers any registered altar.
     * This method should be called from a relevant event listener (e.g., PlayerInteractEvent).
     * It determines the altar type, validates structure, fires EventAltarUse,
     * checks permissions and resources, and then initiates the ritual process.
     * 
     * @param block The {@link Block} the player interacted with.
     * @param player The {@link Player} who performed the interaction.
     * @return {@code true} if the interaction was identified as an altar attempt (even if subsequently cancelled or failed), 
     *         {@code false} if it wasn't an altar interaction at all.
     */
    public boolean handleBlockInteract(Block block, Player player) {
        // Optimization: Check if altars are enabled globally first
        if (!config.isAltarsEnabled() || altars.isEmpty()) {
            return false;
        }

        VampirePlayer vampirePlayer = vampireManager.getCachedVampirePlayer(player.getUniqueId()); 
        if (vampirePlayer == null) {
             VampireMessages.debug("Altar interaction cancelled: VampirePlayer data not cached for " + player.getName());
             return false; // Cannot proceed without player data
        }
        
        // --- Determine Altar Type and Structure --- 
        AltarAbstract altar = determineAltarType(block);
        if (altar == null) {
            return false; // Not a valid altar interaction
        }

        VampireMessages.debug("Player " + player.getName() + " interacted with validated " + altar.getName() + " altar core.");

        // --- Fire the Event --- 
        EventAltarUse event = new EventAltarUse(altar, vampirePlayer, player);
        Bukkit.getPluginManager().callEvent(event);

        // --- Check Cancellation --- 
        if (event.isCancelled()) {
            VampireMessages.debug("Altar use event cancelled by listener for " + player.getName());
            // Optionally send a generic cancelled message?
            // VampireMessages.sendLocalized(player, "altar.fail.cancelled_by_event"); 
            return true; // Mark as handled (was an altar attempt), but cancelled by event
        }

        // --- Initial Checks (Post-Event) --- 

        // 1. Permission Check
        if (!VampirePermission.has(player, altar.getUsePermission(), true)) { // Sends default no-perm message on fail
             VampireMessages.debug("Player " + player.getName() + " lacks permission " + altar.getUsePermission() + " for " + altar.getName());
             return true; // Handled, but failed due to permissions
        }
        
        // 2. Altar-Specific Preconditions (e.g., already vampire/cured)
        if (!altar.checkPreconditions(vampirePlayer, player)) { // Delegate precondition check to altar
            // Specific message should be sent by checkPreconditions
            VampireMessages.debug("Player " + player.getName() + " failed preconditions for " + altar.getName());
            return true; // Handled, but failed preconditions
        }

        // 3. Resource Check (Items/Blood)
        if (!altar.checkResources(vampirePlayer, player)) { // Delegate resource check to altar
            // Specific message should be sent by checkResources
            VampireMessages.debug("Player " + player.getName() + " lacks resources for " + altar.getName());
            return true; // Handled, but failed due to resources
        }

        // --- Initiate Ritual --- 
        VampireMessages.debug("Player " + player.getName() + " passed checks for " + altar.getName() + ". Initiating ritual...");
        startAltarRitual(altar, vampirePlayer, player); // Start the potentially delayed process

        return true; // Indicate the interaction was handled as an altar process
    }

    /**
     * Starts the altar ritual process, including delay and movement checks.
     * 
     * @param altar The specific altar being used.
     * @param vp The VampirePlayer using the altar.
     * @param player The Player using the altar.
     */
    private void startAltarRitual(AltarAbstract altar, VampirePlayer vp, Player player) {
        // Apply initial cosmetic/feedback effects (blindness, glowing, messages, sounds)
        altar.applyStartEffects(vp, player); // Delegate starting effects
        
        // Register location *before* scheduling the delayed task.
        altar.registerPlayerLocation(player); 
        
        // Schedule the final effect application after the configured delay.
        long delayTicks = altar.getChannelingDelayTicks();
        new BukkitRunnable() {
            @Override
            public void run() {
                // Re-fetch player and VP data in case they logged off etc.
                // Crucial: Use the UUID to get the current player instance
                Player currentPlayer = Bukkit.getPlayer(vp.getUuid()); 
                VampirePlayer currentVP = (currentPlayer != null) ? vampireManager.getCachedVampirePlayer(vp.getUuid()) : null;

                // Check if player is still valid and online
                if (currentPlayer == null || !currentPlayer.isValid() || currentVP == null) {
                    VampireMessages.debug("Altar ritual cancelled for " + vp.getName() + ": Player logged off or became invalid.");
                    altar.unregisterPlayerLocation(player); // Use original player ref for unregister if current is null
                    return; 
                }

                // Check: Did the player move? 
                if (altar.hasPlayerMoved(currentPlayer)) {
                    VampireMessages.sendLocalized(currentPlayer, "altar.fail.moved");
                    FxUtil.playAltarFailEffect(currentPlayer.getLocation()); // Use centralized method
                    altar.unregisterPlayerLocation(currentPlayer); // Clean up tracking data
                    return; // Stop the task
                }

                // --- Consume Resources --- 
                // Consume resources ONLY if the player didn't move.
                if (!altar.consumeResources(currentVP, currentPlayer)) {
                    // Specific message should be sent by consumeResources method
                    VampireMessages.debug("Resource consumption failed for " + currentPlayer.getName() + " at the last moment.");
                    FxUtil.playAltarFailEffect(currentPlayer.getLocation()); // Play fail effect on resource consumption failure too
                    altar.unregisterPlayerLocation(currentPlayer);
                    return; 
                }
                // Update client inventory view *after* successful consumption
                currentPlayer.updateInventory(); 

                // --- Apply Final Effects --- 
                // Player didn't move, resources consumed, apply the core altar effects.
                VampireMessages.debug("Applying final effects for " + altar.getName() + " ritual for " + currentPlayer.getName());
                // Pass the core block location or the player's current location? Let's pass player loc block.
                altar.applyEffects(currentVP, currentPlayer, currentPlayer.getLocation().getBlock(), vampireManager);
                
                // --- Cleanup --- 
                altar.unregisterPlayerLocation(currentPlayer); // Clean up tracking data after success
            }
        }.runTaskLater(plugin, delayTicks);
    }
    
    /**
     * Gets an unmodifiable list of all registered altars.
     * Useful for commands or other parts of the plugin that might need to list available altars.
     * 
     * @return An unmodifiable {@link List} of all registered {@link AltarAbstract} instances.
     */
    public List<AltarAbstract> getAltars() {
        // Return an unmodifiable view to prevent external modification of the internal list.
        return Collections.unmodifiableList(altars);
    }
} 