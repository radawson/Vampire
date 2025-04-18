package org.clockworx.vampire.altar;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
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
 * The Light Altar allows players to heal their vampirism infection.
 * This altar requires specific materials to construct and resources to activate.
 * 
 * Infection Healing Mechanics:
 * 1. If player is not infected (infection = 0.0): No effect
 * 2. If player is infected (0.0 < infection < 1.0):
 *    - Reduces infection by 0.2
 *    - If infection drops to 0.0 or below, player is cured
 *    - Triggers EventVampirePlayerInfectionChange
 * 3. If player is fully infected (infection = 1.0):
 *    - No effect (must use other means to cure full vampirism)
 * 
 * When used, it will:
 * 1. Check if the player is infected
 * 2. Apply visual and sound effects
 * 3. Consume required resources
 * 4. Reduce the player's infection level
 * 5. Heal the player
 */
public class AltarLight extends AltarAbstract {
    
    /**
     * Creates a new Light Altar with the required materials and resources.
     */
    public AltarLight() {
        this.name = "Light Altar";
        this.desc = "An altar that can heal vampirism infection.";
        
        this.coreMaterial = Material.DIAMOND_BLOCK;
        
        this.materialCounts = new HashMap<>();
        this.materialCounts.put(Material.GLOWSTONE, 1);
        this.materialCounts.put(Material.QUARTZ_BLOCK, 16);
        this.materialCounts.put(Material.SEA_LANTERN, 8);
        this.materialCounts.put(Material.DIAMOND_BLOCK, 2);
        this.materialCounts.put(Material.GOLD_BLOCK, 4);
        
        this.resources = List.of(
            new ItemStack(Material.GOLDEN_APPLE, 1),
            new ItemStack(Material.GLOWSTONE_DUST, 16),
            new ItemStack(Material.SUGAR, 32),
            new ItemStack(Material.BLAZE_POWDER, 8)
        );
    }
    
    /**
     * Uses the Light Altar to attempt to heal the player's vampirism infection.
     * This method:
     * 1. Checks if the player is infected
     * 2. Applies visual and sound effects
     * 3. Consumes required resources
     * 4. Reduces the player's infection level by 0.2
     * 5. Heal the player
     * 
     * The infection change will trigger an EventVampirePlayerInfectionChange event.
     * If the event is cancelled, the infection level will not change.
     * 
     * @param vampirePlayer The VampirePlayer instance of the player
     * @param player The Bukkit Player instance
     * @return true if the altar was successfully used, false otherwise
     */
    @Override
    public boolean use(VampirePlayer vampirePlayer, Player player, Block block) {
        VampireMessages.send(vampirePlayer, "");
        VampireMessages.send(vampirePlayer, this.desc);
        
        // Check if the player is infected
        if (!vampirePlayer.isInfected()) {
            VampireMessages.send(vampirePlayer, "You are not infected with vampirism.");
            return false;
        }
        
        // Apply effects
        VampireMessages.send(player, "The altar begins to glow with a bright light...");
        FxUtil.ensure(PotionEffectType.GLOWING, player, 5 * 20);
        FxUtil.runSmoke(player);
        
        // Check if the player has the required resources
        if (!ResourceUtil.playerRemoveAttempt(player, this.resources, 
                "You have the required resources for the healing ritual.", 
                "You don't have the required resources for the healing ritual.")) {
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
        // Reduce infection
        double currentInfection = vampirePlayer.getInfection();
        double newInfection = Math.max(0.0, currentInfection - 0.2);
        
        // Fire infection change event
        EventVampirePlayerInfectionChange event = new EventVampirePlayerInfectionChange(newInfection, vampirePlayer);
        VampirePlugin.getInstance().getServer().getPluginManager().callEvent(event);
        
        // Only apply infection change if event wasn't cancelled
        if (!event.isCancelled()) {
            vampirePlayer.setInfectionLevel(newInfection);
            
            // Check if player was cured
            if (newInfection <= 0.0) {
                VampireMessages.send(vampirePlayer, "You have been cured of vampirism!");
            } else {
                VampireMessages.send(vampirePlayer, "You feel the light energy healing your body!");
            }
        }
        
        // Heal the player
        player.setHealth(Math.min(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(), player.getHealth() + 4.0));
        
        // Apply effects
        VampireMessages.send(vampirePlayer, "You feel the light energy healing your body!");
        player.getWorld().strikeLightningEffect(player.getLocation().add(0, 3, 0));
        FxUtil.runSmokeBurst(player);
    }
} 