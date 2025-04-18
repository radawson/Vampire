package org.clockworx.vampire.altar;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages all altars in the Vampire plugin.
 * This class is responsible for registering altars and handling altar interactions.
 */
public class AltarManager {
    
    /**
     * The list of all registered altars.
     */
    private final List<AltarAbstract> altars;
    
    /**
     * Creates a new AltarManager and registers all altars.
     */
    public AltarManager() {
        this.altars = new ArrayList<>();
        
        // Register altars
        registerAltar(new AltarDark());
        registerAltar(new AltarLight());
        
        VampirePlugin.getInstance().getLogger().info("Registered " + altars.size() + " altars");
    }
    
    /**
     * Registers an altar.
     * 
     * @param altar The altar to register
     */
    public void registerAltar(AltarAbstract altar) {
        altars.add(altar);
    }
    
    /**
     * Handles a player interacting with a block.
     * 
     * @param block The block the player interacted with
     * @param player The player who interacted with the block
     * @return true if an altar was used, false otherwise
     */
    public boolean handleBlockInteract(Block block, Player player) {
        VampirePlayer vampirePlayer = VampirePlayer.get(player);
        if (vampirePlayer == null) return false;
        
        // Check if the block is part of an altar
        for (AltarAbstract altar : altars) {
            if (altar.evalBlockUse(block, player)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Gets all registered altars.
     * 
     * @return The list of all registered altars
     */
    public List<AltarAbstract> getAltars() {
        return altars;
    }
} 