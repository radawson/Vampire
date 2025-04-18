package org.clockworx.vampire.altar;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.event.EventAltarUse;
import org.clockworx.vampire.util.TextUtil;
import org.clockworx.vampire.util.VampireMessages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

/**
 * Abstract base class for all altars in the Vampire plugin.
 * Altars are special structures that players can build and use for various effects.
 * This class provides the core functionality for altar construction validation and usage.
 */
public abstract class AltarAbstract {
    
    /**
     * The name of the altar.
     */
    protected String name;
    
    /**
     * The description of the altar.
     */
    protected String desc;
    
    /**
     * The core material of the altar (the block that players interact with).
     */
    protected Material coreMaterial;
    
    /**
     * The materials required to build the altar and their quantities.
     */
    protected Map<Material, Integer> materialCounts;
    
    /**
     * The resources required to use the altar.
     */
    protected List<ItemStack> resources;
    
    /**
     * Map of player UUIDs to their initial locations when using altars.
     */
    private static final Map<UUID, Location> playerLocations = new HashMap<>();
    
    /**
     * Registers a player's location for movement tracking.
     * 
     * @param player The player to track
     */
    protected void registerPlayerLocation(Player player) {
        playerLocations.put(player.getUniqueId(), player.getLocation());
    }
    
    /**
     * Unregisters a player's location from movement tracking.
     * 
     * @param player The player to stop tracking
     */
    protected void unregisterPlayerLocation(Player player) {
        playerLocations.remove(player.getUniqueId());
    }
    
    /**
     * Checks if a player has moved from their initial location.
     * Only checks for position changes, not rotation changes.
     * 
     * @param player The player to check
     * @return true if the player has moved, false otherwise
     */
    protected boolean hasPlayerMoved(Player player) {
        Location initialLocation = playerLocations.get(player.getUniqueId());
        if (initialLocation == null) return false;
        
        Location currentLocation = player.getLocation();
        return !initialLocation.getWorld().equals(currentLocation.getWorld()) ||
               initialLocation.getX() != currentLocation.getX() ||
               initialLocation.getY() != currentLocation.getY() ||
               initialLocation.getZ() != currentLocation.getZ();
    }
    
    /**
     * Evaluates if a player can use the altar at the given block.
     * This method performs the following checks:
     * 1. Validates the player and block
     * 2. Verifies the core material
     * 3. Checks the altar structure completeness
     * 4. Triggers the altar use event if all checks pass
     * 
     * @param coreBlock The block the player is interacting with
     * @param player The player attempting to use the altar
     * @return true if the player can use the altar, false otherwise
     */
    public boolean evalBlockUse(Block coreBlock, Player player) {
        // Check if the player is valid
        if (player == null) return false;
        
        // Check if the core block is the correct material
        if (coreBlock.getType() != coreMaterial) return false;
        
        // Get the player's configuration
        VampirePlayer vampirePlayer = VampirePlayer.get(player);
        VampirePlugin plugin = VampirePlugin.getInstance();
        
        // Make sure we include the coreBlock material in the wanted ones
        if (!this.materialCounts.containsKey(this.coreMaterial)) {
            this.materialCounts.put(this.coreMaterial, 1);
        }
        
        // Get all blocks in the altar's area
        ArrayList<Block> blocks = getCubeBlocks(coreBlock, plugin.getVampireConfig().getAltarSearchRadius());
        
        // Count the materials in the altar's area
        Map<Material, Integer> nearbyMaterialCounts = countMaterials(blocks, this.materialCounts.keySet());
        
        // Calculate the total required and nearby material counts
        int requiredMaterialCountSum = sumCollection(this.materialCounts.values());
        int nearbyMaterialCountSum = sumCollection(nearbyMaterialCounts.values());
        
        // If the blocks are too far from looking anything like an altar, skip
        if (nearbyMaterialCountSum < requiredMaterialCountSum * plugin.getVampireConfig().getAltarMinRatio()) return false;
        
        // What altar blocks are missing?
        Map<Material, Integer> missingMaterialCounts = getMissingMaterialCounts(nearbyMaterialCounts);
        
        // Is the altar complete?
        if (sumCollection(missingMaterialCounts.values()) > 0) {
            // Send info on what to do to finish the altar
            player.sendMessage(TextUtil.parse("The altar is incomplete. You need:"));
            
            for (Entry<Material, Integer> entry : missingMaterialCounts.entrySet()) {
                Material material = entry.getKey();
                int count = entry.getValue();
                player.sendMessage(TextUtil.parse("%d %s", count, TextUtil.getMaterialName(material)));
            }
            
            return false;
        }
        
        // Fire the altar use event
        EventAltarUse event = new EventAltarUse(this, vampirePlayer, player);
        plugin.getServer().getPluginManager().callEvent(event);
        
        // If the event was cancelled, don't use the altar
        if (event.isCancelled()) {
            return false;
        }
        
        // Use the altar
        return use(vampirePlayer, player, coreBlock);
    }
    
    /**
     * Uses the altar with the specified player.
     * This method should be called when a player interacts with the altar's core block.
     * 
     * @param vampirePlayer The VampirePlayer instance of the player
     * @param player The Bukkit Player instance
     * @param block The block that was interacted with
     * @return true if the altar was used successfully, false otherwise
     */
    public boolean use(VampirePlayer vampirePlayer, Player player, Block block) {
        // Create and fire the altar use event
        EventAltarUse event = new EventAltarUse(this, vampirePlayer, player);
        VampirePlugin.getInstance().getServer().getPluginManager().callEvent(event);
        
        // Check if the event was cancelled
        if (event.isCancelled()) {
            return false;
        }
        
        // Register player location for movement tracking
        registerPlayerLocation(player);
        
        try {
            // Check if player has moved
            if (hasPlayerMoved(player)) {
                VampireMessages.send(vampirePlayer, "You moved! The ritual has been interrupted.");
                return false;
            }
            
            // Consume resources
            for (ItemStack resource : resources) {
                player.getInventory().removeItem(resource);
            }
            
            // Apply effects
            applyEffects(vampirePlayer, player, block);
            
            return true;
        } finally {
            // Always unregister the player's location
            unregisterPlayerLocation(player);
        }
    }
    
    /**
     * Applies the altar's effects to the player.
     * This method should be implemented by subclasses to provide specific altar functionality.
     * 
     * @param vampirePlayer The VampirePlayer instance of the player
     * @param player The Bukkit Player instance
     * @param block The block that was interacted with
     */
    protected abstract void applyEffects(VampirePlayer vampirePlayer, Player player, Block block);
    
    /**
     * Sends a message to the player about the altar.
     * 
     * @param vampirePlayer The VampirePlayer instance of the player
     * @param player The Bukkit Player instance
     */
    public void watch(VampirePlayer vampirePlayer, Player player) {
        vampirePlayer.msg(this.desc);
    }
    
    /**
     * Gets the name of the altar.
     * 
     * @return The altar's name
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Gets the description of the altar.
     * 
     * @return The altar's description
     */
    public String getDescription() {
        return this.desc;
    }
    
    /**
     * Gets the core material of the altar.
     * 
     * @return The altar's core material
     */
    public Material getCoreMaterial() {
        return this.coreMaterial;
    }
    
    /**
     * Gets the required materials for the altar.
     * 
     * @return Map of materials and their required quantities
     */
    public Map<Material, Integer> getMaterialCounts() {
        return new HashMap<>(this.materialCounts);
    }
    
    /**
     * Gets the required resources for the altar.
     * 
     * @return List of required ItemStacks
     */
    public List<ItemStack> getResources() {
        return new ArrayList<>(this.resources);
    }
    
    /**
     * Sums the values in a collection of integers.
     * 
     * @param collection The collection to sum
     * @return The sum of the values in the collection
     */
    protected int sumCollection(Collection<Integer> collection) {
        int ret = 0;
        for (Integer i : collection) ret += i;
        return ret;
    }
    
    /**
     * Gets the missing material counts for an altar.
     * 
     * @param nearbyMaterialCounts The material counts nearby
     * @return The missing material counts
     */
    protected Map<Material, Integer> getMissingMaterialCounts(Map<Material, Integer> nearbyMaterialCounts) {
        Map<Material, Integer> ret = new HashMap<>();
        
        for (Entry<Material, Integer> entry : materialCounts.entrySet()) {
            Material material = entry.getKey();
            int required = entry.getValue();
            int nearby = nearbyMaterialCounts.getOrDefault(material, 0);
            int missing = Math.max(0, required - nearby);
            ret.put(material, missing);
        }
        
        return ret;
    }
    
    /**
     * Counts the materials in a collection of blocks.
     * 
     * @param blocks The blocks to count
     * @param materialsToCount The materials to count
     * @return The material counts
     */
    protected static Map<Material, Integer> countMaterials(Collection<Block> blocks, Set<Material> materialsToCount) {
        Map<Material, Integer> ret = new HashMap<>();
        
        for (Block block : blocks) {
            Material material = block.getType();
            if (!materialsToCount.contains(material)) continue;
            
            ret.merge(material, 1, Integer::sum);
        }
        
        return ret;
    }
    
    /**
     * Gets all blocks in a cube around a center block.
     * 
     * @param centerBlock The center block
     * @param radius The radius of the cube
     * @return The blocks in the cube
     */
    protected static ArrayList<Block> getCubeBlocks(Block centerBlock, int radius) {
        ArrayList<Block> blocks = new ArrayList<>();
        
        for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
                for (int x = -radius; x <= radius; x++) {
                    blocks.add(centerBlock.getRelative(x, y, z));
                }
            }
        }
        
        return blocks;
    }
} 