package org.clockworx.vampire.task;

import org.bukkit.Bukkit; // Need Bukkit for getOnlinePlayers
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.config.VampireConfig; // Need config access

/**
 * Task that runs periodically to regenerate blood for online vampires,
 * applying various conditions and multipliers.
 */
public class BloodRegenerationTask extends BukkitRunnable {
    
    private final VampirePlugin plugin;
    private final VampireManager vampireManager;
    private final VampireConfig config; // Cache config reference

    public BloodRegenerationTask(VampirePlugin plugin) {
        this.plugin = plugin;
        this.vampireManager = plugin.getVampireManager();
        this.config = plugin.getVampireConfig(); // Get config on init
    }

    @Override
    public void run() {
        // Base regeneration rate (blood points per second)
        double baseRegenRate = config.getBloodRegenRate(); 
        if (baseRegenRate <= 0) return; // Skip if base rate is zero or negative

        // Interval in seconds (how often this task runs)
        // Get the interval the task is actually scheduled with for accuracy
        // Assuming the task delay in config is used for scheduling interval
        double intervalSeconds = config.getTaskDelay() / 20.0; // Convert ticks to seconds
        if (intervalSeconds <= 0) intervalSeconds = 1.0; // Prevent division by zero

        // Base potential gain for this interval
        double potentialGain = baseRegenRate * intervalSeconds;

        long currentTime = System.currentTimeMillis();

        // Iterate through cached online players
        for (VampirePlayer vp : vampireManager.getCachedOnlinePlayers()) {
            if (!vp.isVampire()) continue; // Skip non-vampires

            Player player = vp.getPlayer();
            if (player == null || !player.isValid()) continue; // Skip offline/invalid

            // Check if blood is already full (using effective max)
            // Note: manager.addBlood handles clamping, but checking here avoids calculation
            if (vp.getBlood() >= vampireManager.getEffectiveMaxBlood(vp)) continue; 

            // --- Calculate Regeneration Multiplier --- 
            double regenMultiplier = 1.0; // Start with default multiplier

            // Condition: Sunlight Exposure
            if (config.isSunDamage() && config.getRegenDisableInSunlight()) { // Check if regen is disabled in sun
                 World world = player.getWorld();
                 boolean isDayTime = world.getTime() > 0 && world.getTime() < 12300;
                 boolean exposedToSun = isDayTime && 
                                       player.getLocation().getBlock().getLightFromSky() > 10 && 
                                       !world.hasStorm() && !world.isThundering();
                 if (exposedToSun) {
                     regenMultiplier = 0.0; // No regeneration in sunlight
                 }
            }

            // Condition: Recent Damage Cooldown (only if multiplier > 0)
            if (regenMultiplier > 0.0) {
                long timeSinceLastDamage = currentTime - vp.getLastDamageTime();
                long damageCooldownMillis = config.getRegenDamageCooldownSeconds() * 1000L;
                if (timeSinceLastDamage < damageCooldownMillis) {
                    regenMultiplier = 0.0; // No regeneration shortly after damage
                }
            }

            // Apply Modifiers (only if multiplier > 0)
            if (regenMultiplier > 0.0) {
                // Modifier: Hunger Penalty
                if (player.getFoodLevel() < config.getRegenHungerThreshold()) {
                    regenMultiplier *= config.getRegenHungerMultiplier();
                }
                // Modifier: Satiation Bonus (Can stack with resting)
                else if (player.getFoodLevel() >= config.getRegenSatiatedThreshold()) {
                     regenMultiplier *= config.getRegenSatiatedMultiplier();
                }

                // Modifier: Resting Bonus (Can stack with satiation)
                if (player.isSleeping()) {
                    regenMultiplier *= config.getRegenRestingMultiplier();
                }
            }
            
            // --- Apply Regeneration --- 
            if (regenMultiplier > 0.0) {
                double finalGain = potentialGain * regenMultiplier;
                if (finalGain > 0.0) {
                    // Use the manager's method to handle adding blood
                    vampireManager.addBlood(vp.getUuid(), finalGain); 
                }
            }
        }
    }
} 