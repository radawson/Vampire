package org.clockworx.vampire.altar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.config.VampireConfig;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.event.EventAltarUse;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Abstract base class defining the structure and common functionality for all Altars 
 * within the Vampire plugin. Altars are multi-block structures players interact with 
 * for specific plugin effects or rituals.
 * 
 * <p>This class handles:</p>
 * <ul>
 *   <li>Storing basic altar properties (name, description, core block).</li>
 *   <li>Defining required materials for construction and resources for usage.</li>
 *   <li>Validating the structural integrity of a potential altar location.</li>
 *   <li>Checking player resources and conditions before allowing usage.</li>
 *   <li>Tracking player movement to cancel rituals if they move.</li>
 *   <li>Calling the {@link EventAltarUse} event.</li>
 *   <li>Providing helper methods for block scanning and material counting.</li>
 * </ul>
 * 
 * Subclasses must implement {@link #applyEffects(VampirePlayer, Player, Block, VampireManager)} 
 * to define the specific actions performed when the altar is successfully used.
 */
public abstract class AltarAbstract {
    
    /** The main VampirePlugin instance, providing access to configuration and managers. */
    protected final VampirePlugin plugin;
    /** Cached reference to the plugin's main configuration. */
    protected final VampireConfig config;
    /** Cached reference to the {@link VampireManager}. */
    protected final VampireManager manager;

    /** The display name of the altar (e.g., "Dark Altar"). */
    protected String name;
    
    /** A short description of the altar's purpose or function. */
    protected String desc;
    
    /** The specific {@link Material} of the central block that players interact with to use the altar. */
    protected Material coreMaterial;
    
    /** 
     * A map defining the required materials and their minimum counts within the search radius 
     * for the altar structure to be considered valid. Key: {@link Material}, Value: Minimum Count.
     */
    protected Map<Material, Integer> materialCounts;
    
    /** 
     * A list of {@link ItemStack}s representing the items consumed when the altar is used. 
     * The check verifies the player has at least the specified amount of each item (matching type and potentially meta).
     */
    protected List<ItemStack> resources;
    
    /** 
     * Tracks the initial location of players when they start using an altar.
     * Used by {@link #hasPlayerMoved(Player)} to detect movement during a ritual.
     * Key: Player UUID, Value: Initial {@link Location}.
     */
    private static final Map<UUID, Location> playerLocations = new HashMap<>();
    
    /** The permission node required to use this specific altar. */
    protected String usePermission;
    
    /** The sound effect played upon successful activation. */
    protected Sound successSound;

    /** The localization key for the message sent on successful activation. */
    protected String successMessageKey;

    /** The delay in ticks before the ritual effect applies after starting. */
    protected int channelingDelayTicks;

    /** The maximum allowed movement distance (squared) before the ritual is cancelled. */
    protected double maxMovementDistanceSquared;

    /**
     * Constructor for concrete Altar implementations.
     * Initializes plugin references and collections. Subclasses should call this and then
     * populate the name, desc, coreMaterial, materialCounts, and resources fields.
     *
     * @param plugin The main {@link VampirePlugin} instance.
     */
    protected AltarAbstract(VampirePlugin plugin) {
        this.plugin = plugin;
        // Cache frequently accessed objects
        this.config = plugin.getVampireConfig();
        this.manager = plugin.getVampireManager();
        // Initialize collections
        this.materialCounts = new HashMap<>();
        this.resources = new ArrayList<>();
    }
    
    /**
     * Registers the player's current location when they begin using the altar.
     * Stored in the static {@link #playerLocations} map.
     * 
     * @param player The {@link Player} starting the altar use.
     */
    public void registerPlayerLocation(Player player) {
        playerLocations.put(player.getUniqueId(), player.getLocation().clone()); // Clone to prevent modification
    }
    
    /**
     * Unregisters the player's location tracking data after altar use completes or is cancelled.
     * Removes the player's entry from the static {@link #playerLocations} map.
     * 
     * @param player The {@link Player} finishing altar use.
     */
    public void unregisterPlayerLocation(Player player) {
        playerLocations.remove(player.getUniqueId());
    }
    
    /**
     * Checks if a player has moved significantly from their registered starting location.
     * Compares world and block coordinates (X, Y, Z). Does not check pitch/yaw.
     * Returns true if the player is not tracked or their location differs.
     * 
     * @param player The {@link Player} to check for movement.
     * @return {@code true} if the player has moved from their registered location or is not tracked, {@code false} otherwise.
     */
    public boolean hasPlayerMoved(Player player) {
        Location initialLocation = playerLocations.get(player.getUniqueId());
        // If player wasn't registered, technically they haven't "moved" from a registered spot in context of the ritual.
        // However, the calling context (`use` method) implies they *should* be registered, so this might indicate an issue.
        // Let's return true if not found, indicating an invalid state or movement.
        if (initialLocation == null) return true; 
        
        Location currentLocation = player.getLocation();
        
        // Check for world change or significant distance change.
        if (!initialLocation.getWorld().equals(currentLocation.getWorld())) {
            return true;
        }
        
        // Check distance squared to avoid square root calculation.
        return initialLocation.distanceSquared(currentLocation) > getMaxMovementDistanceSquared();
    }
    
    /**
     * Abstract method to be implemented by subclasses. Defines the specific actions, 
     * effects, or state changes that occur when the altar is successfully used 
     * (after validation, event call, resource consumption, and delay/movement checks).
     * 
     * @param vampirePlayer The {@link VampirePlayer} instance of the player.
     * @param player The Bukkit {@link Player} instance.
     * @param block The core altar {@link Block} interacted with.
     * @param manager The {@link VampireManager} instance.
     */
    public abstract void applyEffects(VampirePlayer vampirePlayer, Player player, Block block, VampireManager manager);
    
    /**
     * Sends the altar's description message to the player.
     * Typically called when a player simply looks at or right-clicks the altar without meeting usage criteria.
     * 
     * @param vampirePlayer The {@link VampirePlayer} instance (potentially unused, context?).
     * @param player The Bukkit {@link Player} instance to send the message to.
     */
    public void watch(VampirePlayer vampirePlayer, Player player) {
        // Send the stored description, assuming it's already formatted/colorized if needed.
        // Consider making description a localization key.
        VampireMessages.send(player, this.desc); 
    }
    
    // --- Getters ---

    /**
     * Gets the display name of this altar.
     * @return The altar's name.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Gets the description of this altar.
     * @return The altar's description.
     */
    public String getDescription() {
        return this.desc;
    }
    
    /**
     * Gets the core {@link Material} required for this altar's interaction block.
     * @return The core material.
     */
    public Material getCoreMaterial() {
        return this.coreMaterial;
    }
    
    /**
     * Gets the map defining the minimum counts of materials required for the altar structure.
     * @return A Map where Key is {@link Material} and Value is the minimum count required.
     */
    public Map<Material, Integer> getMaterialCounts() {
        return this.materialCounts;
    }
    
    /**
     * Gets the list of {@link ItemStack}s required as resources to use the altar.
     * @return A List of required item stacks.
     */
    public List<ItemStack> getResources() {
        return this.resources;
    }
    
    /**
     * Gets the specific permission node required to use this altar.
     * Should be defined by subclasses.
     * @return The permission node string (e.g., "vampire.altar.dark").
     */
    public String getUsePermission() {
        return this.usePermission;
    }

    /**
     * Gets the Sound enum to be played on successful altar activation.
     * Should be defined by subclasses.
     * @return The Sound enum.
     */
    public Sound getSound() {
        return this.successSound;
    }

    /**
     * Gets the localization key for the message sent upon successful activation.
     * Should be defined by subclasses.
     * @return The localization key string (e.g., "altar.dark.success").
     */
    public String getSuccessMessageKey() {
        return this.successMessageKey;
    }

    /**
     * Gets the configured channeling delay in server ticks for this altar.
     * @return The delay in ticks.
     */
    public int getChannelingDelayTicks() {
        return this.channelingDelayTicks;
    }

    /**
     * Gets the configured maximum allowed movement distance (squared) during channeling.
     * @return The maximum distance squared.
     */
    public double getMaxMovementDistanceSquared() {
        return this.maxMovementDistanceSquared;
    }

    // --- New Abstract/Overridable Methods for Manager Delegation ---

    /**
     * Checks altar-specific preconditions before initiating the ritual.
     * (e.g., Is the player already a vampire for AltarDark? Is the player already cured for AltarLight?)
     * Should send appropriate failure messages to the player if checks fail.
     * 
     * @param vp The VampirePlayer using the altar.
     * @param player The Player using the altar.
     * @return true if preconditions are met, false otherwise.
     */
    public abstract boolean checkPreconditions(VampirePlayer vp, Player player);

    /**
     * Checks if the player has the required resources (items, blood) for this altar.
     * Should send appropriate failure messages to the player if checks fail.
     * 
     * @param vp The VampirePlayer using the altar.
     * @param player The Player using the altar.
     * @return true if the player has the required resources, false otherwise.
     */
    public abstract boolean checkResources(VampirePlayer vp, Player player);
    
    /**
     * Applies initial visual/audio/feedback effects when the ritual starts 
     * (after event check, permission check, resource check).
     * 
     * @param vp The VampirePlayer starting the ritual.
     * @param player The Player starting the ritual.
     */
    public abstract void applyStartEffects(VampirePlayer vp, Player player);

    /**
     * Consumes the required resources (items, blood) from the player.
     * Called only if the player did not move during the ritual delay.
     * 
     * @param vp The VampirePlayer using the altar.
     * @param player The Player using the altar.
     * @return true if resources were consumed successfully, false otherwise (e.g., items disappeared during delay).
     */
    public abstract boolean consumeResources(VampirePlayer vp, Player player);
    
    // --- Helper Methods ---
    
    /**
     * Calculates the sum of all integer values in a collection.
     * 
     * @param collection A collection of integers.
     * @return The sum of the integers in the collection.
     */
    public int sumCollection(Collection<Integer> collection) {
        if (collection == null) {
            return 0;
        }
        return collection.stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Compares the required material counts with the counts found nearby and returns
     * a map detailing the materials that are missing and by how much.
     * 
     * @param nearbyMaterialCounts A map of materials found nearby and their counts.
     * @return A map where keys are missing materials and values are the number still needed.
     *         Returns an empty map if all required materials are present in sufficient quantities.
     */
    public Map<Material, Integer> getMissingMaterialCounts(Map<Material, Integer> nearbyMaterialCounts) {
        Map<Material, Integer> missing = new HashMap<>();
        if (this.materialCounts == null || this.materialCounts.isEmpty()) {
            return missing; // No materials required, so none are missing.
        }

        for (Entry<Material, Integer> requiredEntry : this.materialCounts.entrySet()) {
            Material requiredMaterial = requiredEntry.getKey();
            int requiredAmount = requiredEntry.getValue();
            int foundAmount = nearbyMaterialCounts.getOrDefault(requiredMaterial, 0);
            
            if (foundAmount < requiredAmount) {
                missing.put(requiredMaterial, requiredAmount - foundAmount);
            }
        }
        return missing;
    }
    
    /**
     * Counts the occurrences of specified materials within a collection of blocks.
     * 
     * @param blocks The collection of blocks to scan.
     * @param materialsToCount A set of materials to look for and count.
     * @return A map where keys are the materials found and values are their counts within the block collection.
     * @deprecated Visibility should be public for use in AltarManager.
     */
    @Deprecated
    public static Map<Material, Integer> countMaterials(Collection<Block> blocks, Set<Material> materialsToCount) {
        Map<Material, Integer> counts = new HashMap<>();
        if (blocks == null || materialsToCount == null) {
            return counts;
        }
        
        for (Block block : blocks) {
            Material material = block.getType();
            if (materialsToCount.contains(material)) {
                counts.put(material, counts.getOrDefault(material, 0) + 1);
            }
        }
        return counts;
    }
    
    /**
     * Retrieves a list of all blocks within a cubic radius around a central block.
     * 
     * @param centerBlock The block at the center of the cube.
     * @param radius The distance from the center block to scan in each direction (X, Y, Z).
     * @return An {@link ArrayList} of {@link Block} objects within the specified cube.
     */
    public static ArrayList<Block> getCubeBlocks(Block centerBlock, int radius) {
        ArrayList<Block> blocks = new ArrayList<>();
        Location centerLoc = centerBlock.getLocation();
        int centerX = centerLoc.getBlockX();
        int centerY = centerLoc.getBlockY();
        int centerZ = centerLoc.getBlockZ();
        // Add null check for world
        org.bukkit.World world = centerLoc.getWorld(); 
        if (world == null) {
            VampireMessages.error("Cannot get blocks for altar check, world is null for location: " + centerLoc, null);
            return blocks; 
        }

        // Iterate through the cubic area defined by the radius.
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    // Get the block at the current coordinates.
                    Block block = world.getBlockAt(x, y, z);
                    // Add the block to the list if it's not air.
                    // Also check if the block's material is valid (not null)
                    if (block != null && block.getType() != Material.AIR && block.getBlockData().getMaterial() != null) {
                        blocks.add(block);
                    }
                }
            }
        }
        return blocks;
    }
} 