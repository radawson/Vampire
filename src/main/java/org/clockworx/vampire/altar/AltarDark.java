package org.clockworx.vampire.altar;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.event.EventVampirePlayerInfectionChange;
import org.clockworx.vampire.util.FxUtil;
import org.clockworx.vampire.util.ResourceUtil;
import org.clockworx.vampire.util.VampireMessages;

import java.util.HashMap;
import java.util.List;

/**
 * The Dark Altar allows players to become infected with vampirism or increase their infection.
 * This altar requires specific materials to construct and resources to activate.
 * 
 * Infection Mechanics:
 * 1. If player is not infected (infection = 0.0):
 *    - Initial infection of 0.1
 *    - Triggers EventVampirePlayerInfectionChange
 * 2. If player is infected (0.0 < infection < 1.0):
 *    - Increases infection by 0.2
 *    - If infection reaches 1.0, player becomes a vampire
 *    - Triggers EventVampirePlayerInfectionChange
 * 3. If player is fully infected (infection = 1.0):
 *    - No effect (already a vampire)
 * 
 * When used, it will:
 * 1. Check if the player is already a vampire
 * 2. Apply visual and sound effects
 * 3. Consume required resources
 * 4. If player is not infected: Infect them with initial vampirism
 * 5. If player is already infected: Increase their infection rate
 */
public class AltarDark extends AltarAbstract {
    
    /**
     * Creates a new Dark Altar with the required materials and resources.
     */
    public AltarDark() {
        this.name = "Dark Altar";
        this.desc = "An altar that can infect players with vampirism or increase their infection.";
        
        this.coreMaterial = Material.OBSIDIAN;
        
        this.materialCounts = new HashMap<>();
        this.materialCounts.put(Material.OBSIDIAN, 1);
        this.materialCounts.put(Material.NETHERRACK, 16);
        this.materialCounts.put(Material.SOUL_SAND, 8);
        this.materialCounts.put(Material.DIAMOND_BLOCK, 2);
        this.materialCounts.put(Material.GOLD_BLOCK, 4);
        
        this.resources = List.of(
            new ItemStack(Material.ROTTEN_FLESH, 16),
            new ItemStack(Material.SPIDER_EYE, 8),
            new ItemStack(Material.BONE, 16),
            new ItemStack(Material.BLAZE_POWDER, 8)
        );
    }
    
    /**
     * Uses the Dark Altar to attempt to infect the player or increase their infection.
     * This method:
     * 1. Checks if the player is already a vampire
     * 2. Applies visual and sound effects
     * 3. Consumes required resources
     * 4. If player is not infected: Infects them with initial vampirism (0.1)
     * 5. If player is already infected: Increases their infection by 0.2
     * 
     * The infection change will trigger an EventVampirePlayerInfectionChange event.
     * If the event is cancelled, the infection level will not change.
     * If the infection reaches 1.0, the player will become a vampire.
     * 
     * @param vampirePlayer The VampirePlayer instance of the player
     * @param player The Bukkit Player instance
     * @param block The block that was interacted with
     * @return true if the altar was successfully used, false otherwise
     */
    @Override
    public boolean use(VampirePlayer vampirePlayer, Player player, Block block) {
        VampireMessages.send(vampirePlayer, "");
        VampireMessages.send(vampirePlayer, this.desc);
        
        // Check if the player is already a vampire
        if (vampirePlayer.isVampire()) {
            VampireMessages.send(vampirePlayer, "You are already a vampire.");
            return false;
        }
        
        // Apply effects
        VampireMessages.send(player, "The altar begins to glow with a dark light...");
        FxUtil.ensure(PotionEffectType.BLINDNESS, player, 5 * 20);
        FxUtil.runSmoke(player);
        
        // Check if the player has the required resources
        if (!ResourceUtil.playerRemoveAttempt(player, this.resources, 
                "You have the required resources for the dark ritual.", 
                "You don't have the required resources for the dark ritual.")) {
            return false;
        }
        
        // Schedule the effect
        VampirePlugin.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(
            VampirePlugin.getInstance(),
            () -> super.use(vampirePlayer, player, block),
            20 // 1 second delay
        );
        
        return true;
    }

    @Override
    protected void applyEffects(VampirePlayer vampirePlayer, Player player, Block block) {
        if (vampirePlayer.isInfected()) {
            // Increase infection for already infected players
            VampireMessages.send(vampirePlayer, "You feel the dark energy increasing your infection!");
            player.getWorld().strikeLightningEffect(player.getLocation().add(0, 3, 0));
            FxUtil.runSmokeBurst(player);
            
            // Calculate new infection level (increase by 0.2, cap at 1.0)
            double currentInfection = vampirePlayer.getInfection();
            double newInfection = Math.min(1.0, currentInfection + 0.2);
            
            // Fire infection change event
            EventVampirePlayerInfectionChange event = new EventVampirePlayerInfectionChange(newInfection, vampirePlayer);
            VampirePlugin.getInstance().getServer().getPluginManager().callEvent(event);
            
            // Only apply infection change if event wasn't cancelled
            if (!event.isCancelled()) {
                vampirePlayer.setInfectionLevel(newInfection);
                
                // Check if player became a vampire
                if (newInfection >= 1.0) {
                    VampireMessages.send(vampirePlayer, "You have become a vampire!");
                    vampirePlayer.setVampire(true);
                } else {
                    VampireMessages.send(vampirePlayer, "You feel the dark energy increasing your infection!");
                }
            }
        } else {
            // Initial infection for healthy players
            double newInfection = 0.1;
            
            // Fire infection change event
            EventVampirePlayerInfectionChange event = new EventVampirePlayerInfectionChange(newInfection, vampirePlayer);
            VampirePlugin.getInstance().getServer().getPluginManager().callEvent(event);
            
            // Only apply infection if event wasn't cancelled
            if (!event.isCancelled()) {
                vampirePlayer.setInfectionLevel(newInfection);
                VampireMessages.send(vampirePlayer, "You have been infected with vampirism!");
            }
        }
        
        // Apply effects
        player.getWorld().strikeLightningEffect(player.getLocation().add(0, 3, 0));
        FxUtil.runSmokeBurst(player);
    }
} 