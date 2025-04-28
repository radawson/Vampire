package org.clockworx.vampire.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.level.LevelManager;
import org.clockworx.vampire.level.VampireLevel;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages; // If debug needed

/**
 * Listener to handle damage modifications for vampires, such as fall damage reduction.
 */
public class VampireDamageListener implements Listener {

    private final VampirePlugin plugin;
    private final VampireManager vampireManager;
    private final LevelManager levelManager;

    public VampireDamageListener(VampirePlugin plugin) {
        this.plugin = plugin;
        this.vampireManager = plugin.getVampireManager();
        this.levelManager = plugin.getLevelManager();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerFallDamage(EntityDamageEvent event) {
        // Only handle player damage
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Only handle fall damage
        if (event.getCause() != DamageCause.FALL) {
            return;
        }

        // Check if player is a vampire
        VampirePlayer vampirePlayer = vampireManager.getCachedVampirePlayer(player.getUniqueId());
        if (vampirePlayer == null || !vampirePlayer.isVampire()) {
            return;
        }

        // Get level data
        VampireLevel levelData = levelManager.getLevelData(vampirePlayer.getVampireLevel());
        if (levelData == null) {
            // Log warning if level data is missing for a known vampire
            plugin.getLogger().warning("Missing level data for vampire " + player.getName() + " at level " + vampirePlayer.getVampireLevel());
            return;
        }

        // Get reduction amount
        double reduction = levelData.fallDamageReduction();
        if (reduction <= 0.0) {
            return; // No reduction at this level
        }

        // Calculate and apply reduction
        double originalDamage = event.getDamage();
        double newDamage = originalDamage * (1.0 - reduction);
        newDamage = Math.max(0.0, newDamage); // Ensure damage doesn't go below zero

        VampireMessages.debug(String.format(
            "[FallDamage] Reducing fall damage for %s (Level %d). Original: %.2f, Reduction: %.1f%%, New: %.2f",
            player.getName(),
            vampirePlayer.getVampireLevel(),
            originalDamage,
            reduction * 100.0,
            newDamage
        ));

        event.setDamage(newDamage);
    }
} 