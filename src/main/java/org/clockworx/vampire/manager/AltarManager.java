package org.clockworx.vampire.manager;

import org.clockworx.vampire.VampirePlugin;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.Material;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Sound;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.clockworx.vampire.altar.AltarAbstract;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.util.VampireMessages;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.event.EventAltarUse;
import org.clockworx.vampire.util.FxUtil;

public class AltarManager {
    private final VampirePlugin plugin;
    private final VampireManager vampireManager;
    private final List<AltarAbstract> altars;

    public AltarManager(VampirePlugin plugin) {
        this.plugin = plugin;
        this.vampireManager = plugin.getVampireManager();
        this.altars = Collections.unmodifiableList(plugin.getAltarManager().getAltarList());
    }

    public List<AltarAbstract> getAltarList() {
        return Collections.unmodifiableList(altars);
    }
    
    // --- Altar Determination and Validation ---

    /**
     * Determines if the given block could be the core of any registered altar
     * and validates the surrounding structure based on that altar's configuration.
     * 
     * @param coreBlock The block the player interacted with.
     * @return The specific AltarAbstract instance if the block is a valid core
     *         and the structure matches, otherwise null.
     */
    private AltarAbstract determineAltarType(Block coreBlock) {
        if (coreBlock == null) return null;
        Material coreMaterial = coreBlock.getType();

        for (AltarAbstract altar : altars) {
            // Check if the interacted block matches the core material for this altar type
            if (altar.getCoreMaterial() == coreMaterial) {
                VampireMessages.debug("Potential " + altar.getName() + " altar found based on core material: " + coreMaterial);
                // If core matches, validate the surrounding structure
                if (isValidAltarStructure(coreBlock.getLocation(), altar)) {
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
     * 
     * @param coreLocation The location of the potential core block.
     * @param altar The specific AltarAbstract instance (used to get required materials/counts).
     * @return true if the structure is valid according to the altar's definition, false otherwise.
     */
    private boolean isValidAltarStructure(Location coreLocation, AltarAbstract altar) {
        Map<Material, Integer> requiredCounts = altar.getMaterialCounts();
        if (requiredCounts == null || requiredCounts.isEmpty() || (requiredCounts.size() == 1 && requiredCounts.containsKey(altar.getCoreMaterial()))) {
             VampireMessages.debug("Altar " + altar.getName() + " has no required materials defined (excluding core). Assuming valid structure.");
             return true; // No specific structure required beyond the core block itself.
        }

        int searchRadius = plugin.getVampireConfig().getAltarSearchRadius();
        double minRatio = plugin.getVampireConfig().getAltarMinRatio();

        // 1. Get all blocks within the search radius.
        ArrayList<Block> blocks = AltarAbstract.getCubeBlocks(coreLocation.getBlock(), searchRadius);

        // 2. Count the materials found nearby that are required by this altar.
        Map<Material, Integer> nearbyMaterialCounts = AltarAbstract.countMaterials(blocks, requiredCounts.keySet());

        // 3. Check overall ratio.
        int requiredMaterialCountSum = altar.sumCollection(requiredCounts.values());
        int nearbyMaterialCountSum = altar.sumCollection(nearbyMaterialCounts.values());

        // Handle edge case where only the core block is required (sum=1)
        if (requiredMaterialCountSum <= 1 && nearbyMaterialCountSum >= 1) {
             return true; // Only core needed, and it's present (implicit from initial check)
        }
        
        // Check ratio if more than just the core is needed
        if (requiredMaterialCountSum > 1 && 
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
                  VampireMessages.debug("Altar structure specific counts check for " + altar.getName() + " passed (only core was technically missing in radius scan).");
             } else {
                 VampireMessages.debug("Altar structure failed specific counts check for " + altar.getName() + ". Missing: " + missingCounts);
                 return false;
             }
        }

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
        VampirePlayer vampirePlayer = vampireManager.getCachedVampirePlayer(player.getUniqueId()); 
        if (vampirePlayer == null) {
             VampireMessages.debug("Altar interaction cancelled: VampirePlayer data not cached for " + player.getName());
             return false; 
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
            return true; // Mark as handled (was an altar attempt), but cancelled
        }

        // --- Initial Checks (Post-Event) --- 

        // 1. Permission Check
        if (!VampirePermission.has(player, altar.getUsePermission(), true)) { // Send message on fail
             VampireMessages.debug("Player " + player.getName() + " lacks permission " + altar.getUsePermission());
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
        // Apply initial cosmetic/feedback effects (blindness, glowing, messages)
        altar.applyStartEffects(vp, player); // Delegate starting effects
        
        // Register location *before* scheduling the delayed task.
        altar.registerPlayerLocation(player); 
        
        // Schedule the final effect application after a delay.
        long delayTicks = 60L; // TODO: Make this configurable (e.g., per altar or global)
        new BukkitRunnable() {
            @Override
            public void run() {
                // Re-fetch player and VP data in case they logged off etc.
                Player currentPlayer = Bukkit.getPlayer(vp.getUuid());
                VampirePlayer currentVP = (currentPlayer != null) ? vampireManager.getCachedVampirePlayer(vp.getUuid()) : null;

                // Check if player is still valid and online
                if (currentPlayer == null || currentVP == null) {
                    VampireMessages.debug("Altar ritual cancelled for " + vp.getName() + ": Player logged off.");
                    altar.unregisterPlayerLocation(player); // Clean up tracking data
                    return; 
                }

                // Check: Did the player move? 
                if (altar.hasPlayerMoved(currentPlayer)) {
                    VampireMessages.sendLocalized(currentPlayer, "altar.fail.moved");
                    FxUtil.playSound(currentPlayer.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f); // Fail sound
                    altar.unregisterPlayerLocation(currentPlayer); // Clean up tracking data
                    return; // Stop the task
                }

                // --- Consume Resources --- 
                // Consume resources ONLY if the player didn't move.
                // Let the specific altar handle how resources are consumed.
                if (!altar.consumeResources(currentVP, currentPlayer)) {
                    // This check is slightly redundant if checkResources passed earlier,
                    // but good practice in case inventory changed during the delay.
                    VampireMessages.debug("Resource consumption failed for " + currentPlayer.getName() + " at the last moment.");
                    altar.unregisterPlayerLocation(currentPlayer);
                    return; 
                }
                currentPlayer.updateInventory(); // Update client view

                // --- Apply Final Effects --- 
                // Player didn't move, resources consumed, apply the core altar effects.
                // Note: applyEffects might still contain its own event firing for sub-actions
                // like infection change, which should also be checked for cancellation.
                altar.applyEffects(currentVP, currentPlayer, currentPlayer.getLocation().getBlock(), vampireManager);
                
                // --- Cleanup --- 
                altar.unregisterPlayerLocation(currentPlayer); // Clean up tracking data after success
            }
        }.runTaskLater(plugin, delayTicks);
    }
} 