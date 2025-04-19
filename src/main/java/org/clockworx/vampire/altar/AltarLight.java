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
import java.util.Map;

/**
 * The Light Altar allows players to decrease their infection or cure themselves of vampirism.
 * This altar requires specific materials to construct and resources to activate.
 * 
 * Infection Mechanics:
 * 1. If player is not infected (infection = 0.0):
 *    - No effect (already cured)
 * 2. If player is infected (0.0 < infection < 1.0):
 *    - Decreases infection by 0.2
 *    - If infection reaches 0.0, player is cured
 *    - Triggers EventVampirePlayerInfectionChange
 * 3. If player is fully infected (infection = 1.0):
 *    - Decreases infection to 0.8
 *    - Player is no longer a vampire
 *    - Triggers EventVampirePlayerInfectionChange
 * 
 * When used, it will:
 * 1. Check if the player is infected
 * 2. Apply visual and sound effects
 * 3. Consume required resources
 * 4. Decrease the player's infection rate
 * 5. If infection reaches 0.0, cure the player
 */
public class AltarLight extends AltarAbstract {
    
    /**
     * Creates a new Light Altar with the required materials and resources.
     */
    public AltarLight() {
        this.name = "Light Altar";
        this.desc = "An altar that can decrease a player's infection or cure them of vampirism.";
        
        // Get configuration
        VampirePlugin plugin = VampirePlugin.getInstance();
        Map<String, Object> config = plugin.getVampireConfig().getLightAltarConfig();
        
        // Set core material
        String coreMaterialStr = (String) config.getOrDefault("core-material", "DIAMOND_BLOCK");
        this.coreMaterial = Material.valueOf(coreMaterialStr);
        
        // Set materials
        this.materialCounts = new HashMap<>();
        @SuppressWarnings("unchecked")
        Map<String, Integer> materials = (Map<String, Integer>) config.getOrDefault("materials", new HashMap<>());
        for (Map.Entry<String, Integer> entry : materials.entrySet()) {
            try {
                Material material = Material.valueOf(entry.getKey().toUpperCase());
                this.materialCounts.put(material, entry.getValue());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in light altar config: " + entry.getKey());
            }
        }
        
        // Set resources
        @SuppressWarnings("unchecked")
        List<String> resourceStrings = (List<String>) config.getOrDefault("resources", List.of());
        this.resources = resourceStrings.stream()
            .map(str -> {
                String[] parts = str.split(":");
                if (parts.length != 2) return null;
                try {
                    Material material = Material.valueOf(parts[0].toUpperCase());
                    int amount = Integer.parseInt(parts[1]);
                    return new ItemStack(material, amount);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid resource in light altar config: " + str);
                    return null;
                }
            })
            .filter(item -> item != null)
            .toList();
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