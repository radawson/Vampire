package org.clockworx.vampire.task;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.FxUtil;
import org.clockworx.vampire.util.SunUtil;
import org.clockworx.vampire.util.VampireMessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Task that runs periodically to update vampire players.
 * This class handles bloodlust, night vision, infection progression, and environmental damage.
 */
public class VampireTask extends BukkitRunnable {
    
    private final VampirePlugin plugin;
    private final VampireManager vampireManager;
    private int taskId = -1;
    private long lastRun = 0;
    
    /**
     * Creates a new VampireTask.
     * 
     * @param plugin The plugin instance
     */
    public VampireTask(VampirePlugin plugin) {
        this.plugin = plugin;
        this.vampireManager = plugin.getVampireManager();
    }
    
    /**
     * Starts the task.
     */
    public void start() {
        if (taskId != -1) {
            return;
        }
        
        int delay = plugin.getVampireConfig().getTaskDelay();
        taskId = runTaskTimer(plugin, delay, delay).getTaskId();
        lastRun = System.currentTimeMillis();
        plugin.getLogger().info("Vampire task started with delay of " + delay + " ticks");
    }
    
    /**
     * Shuts down the task.
     */
    public void shutdown() {
        if (taskId != -1) {
            cancel();
            taskId = -1;
            plugin.getLogger().info("Vampire task shut down");
        }
    }
    
    @Override
    public void run() {
        long now = System.currentTimeMillis();
        long delta = now - lastRun;
        if (delta <= 0) delta = 1;
        lastRun = now;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            
            VampirePlayer vampirePlayer = vampireManager.getCachedVampirePlayer(uuid);
            if (vampirePlayer == null) {
                continue;
            }
            
            updatePlayer(vampirePlayer, delta);
        }
    }
    
    /**
     * Updates a player's vampire state.
     * 
     * @param vampirePlayer The player to update
     * @param delta The time since the last update in milliseconds
     */
    private void updatePlayer(VampirePlayer vampirePlayer, long delta) {
        Player player = vampirePlayer.getPlayer();
        if (player == null || !player.isValid()) {
            return;
        }
        
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || 
            player.hasPermission("vampire.bypass")) {
            return;
        }
        
        double deltaSeconds = delta / 1000.0;
        
        // Handle passive drain and low blood effects
        updateBloodManagement(vampirePlayer, player, deltaSeconds);
        // Handle regeneration based on conditions
        updateBloodRegeneration(vampirePlayer, player, deltaSeconds);
        updateBloodlust(vampirePlayer, player, deltaSeconds);
        updateNightVision(vampirePlayer, player, deltaSeconds);
        updateInfection(vampirePlayer, player, deltaSeconds);
        updateEnvironmentalDamage(vampirePlayer, player, deltaSeconds);

        // NEW: Update food bar visual
        updateFoodBarVisual(vampirePlayer, player);
    }
    
    /**
     * Handles passive blood drain and applies low blood effects if necessary.
     * 
     * @param vampirePlayer The player to update
     * @param player The player object
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateBloodManagement(VampirePlayer vampirePlayer, Player player, double deltaSeconds) {
        if (!vampirePlayer.isVampire()) {
            return;
        }
        
        // Apply passive blood drain
        vampireManager.addBlood(vampirePlayer.getUuid(), -(plugin.getVampireConfig().getBloodDecreaseRate() * deltaSeconds));
        
        // Check for and apply low blood effects
        if (vampirePlayer.getBlood() < plugin.getVampireConfig().getLowBloodThreshold()) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.WEAKNESS,
                (int)(40 * deltaSeconds),
                1,
                true,
                false
            ));
            
            VampireMessages.sendLocalized(player, "blood.low");
        }
    }
    
    /**
     * Handles blood regeneration based on configured conditions (hunger, damage, sunlight, resting).
     *
     * @param vampirePlayer The player to update
     * @param player The player object
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateBloodRegeneration(VampirePlayer vampirePlayer, Player player, double deltaSeconds) {
        if (!vampirePlayer.isVampire()) {
            return;
        }

        // 1. Check Damage Cooldown
        long timeSinceDamage = System.currentTimeMillis() - vampirePlayer.getLastDamageTime();
        if (timeSinceDamage < plugin.getVampireConfig().getRegenDamageCooldownSeconds() * 1000L) {
            VampireMessages.debug("Blood regen halted for " + player.getName() + " due to recent damage.");
            return; // Halt regeneration if recently damaged
        }

        // 2. Check Sunlight
        boolean inSunlight = SunUtil.calcPlayerIrradiation(player) > 0;
        if (inSunlight && plugin.getVampireConfig().getRegenDisableInSunlight()) {
             VampireMessages.debug("Blood regen halted for " + player.getName() + " due to sunlight exposure.");
             return; // Halt regeneration if configured to stop in sunlight
        }

        // 3. Calculate Base Regeneration Rate
        double baseRegenRate = plugin.getVampireConfig().getBloodRegenRate();
        double effectiveRegenRate = baseRegenRate;

        // 4. Apply Hunger Multipliers
        int foodLevel = player.getFoodLevel();
        if (foodLevel < plugin.getVampireConfig().getRegenHungerThreshold()) {
            effectiveRegenRate *= plugin.getVampireConfig().getRegenHungerMultiplier();
             VampireMessages.debug("Applying hunger regen penalty to " + player.getName());
        } else if (foodLevel >= plugin.getVampireConfig().getRegenSatiatedThreshold()) {
            effectiveRegenRate *= plugin.getVampireConfig().getRegenSatiatedMultiplier();
             VampireMessages.debug("Applying satiated regen bonus to " + player.getName());
        }

        // 5. Apply Resting Multiplier (if sleeping)
        if (player.isSleeping()) {
            effectiveRegenRate *= plugin.getVampireConfig().getRegenRestingMultiplier();
             VampireMessages.debug("Applying resting regen bonus to " + player.getName());
        }

        // 6. Calculate Amount and Apply
        double bloodToAdd = effectiveRegenRate * deltaSeconds;
        if (bloodToAdd > 0) {
            vampireManager.addBlood(vampirePlayer.getUuid(), bloodToAdd);
            // Debug message is now inside addBlood/setBloodLevel in VampireManager
        }
    }
    
    /**
     * Updates a player's bloodlust state.
     * 
     * @param vampirePlayer The player to update
     * @param player The player object
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateBloodlust(VampirePlayer vampirePlayer, Player player, double deltaSeconds) {
        if (!vampirePlayer.isVampire()) {
            return;
        }
        
        boolean currentlyBloodlusting = vampirePlayer.isBloodlusting();
        boolean shouldBeBloodlusting = vampirePlayer.getBlood() < plugin.getVampireConfig().getBloodlustThreshold();
        
        if (!currentlyBloodlusting && shouldBeBloodlusting) {
            vampirePlayer.setBloodlusting(true);
            VampireMessages.sendLocalized(player, "bloodlust.start");
            FxUtil.playVampireEffect(player);
            currentlyBloodlusting = true;
        }
        
        if (currentlyBloodlusting) {
            double bloodDecrease = plugin.getVampireConfig().getBloodlustBloodDecrease() * deltaSeconds;
            vampirePlayer.setBloodInternal(vampirePlayer.getBlood() - bloodDecrease);
            
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED,
                (int)(40 * deltaSeconds),
                1,
                true,
                false
            ));
            
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.STRENGTH,
                (int)(40 * deltaSeconds),
                0,
                true,
                false
            ));
            
            if (vampirePlayer.getBlood() >= plugin.getVampireConfig().getBloodlustThreshold()) {
                vampirePlayer.setBloodlusting(false);
                VampireMessages.sendLocalized(player, "bloodlust.end");
            }
        }
    }
    
    /**
     * Updates a player's night vision state.
     * 
     * @param vampirePlayer The player to update
     * @param player The player object
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateNightVision(VampirePlayer vampirePlayer, Player player, double deltaSeconds) {
        if (!vampirePlayer.isVampire()) {
            return;
        }
        
        if (!plugin.getVampireConfig().isNightVisionEnabled()) {
            return;
        }
        
        World world = player.getWorld();
        boolean isNight = world.getTime() >= 13000 || world.getTime() <= 23000;
        boolean currentlyUsing = vampirePlayer.isUsingNightVision();
        
        if (!currentlyUsing && isNight) {
            vampirePlayer.setUsingNightVision(true);
            VampireMessages.sendLocalized(player, "nightvision.start");
            currentlyUsing = true;
        }
        
        if (currentlyUsing && isNight) {
            int level = plugin.getVampireConfig().getNightVisionLevel();
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.NIGHT_VISION,
                (int)(40 * deltaSeconds),
                level - 1,
                true,
                false
            ));
        }
        
        if (currentlyUsing && !isNight) {
            vampirePlayer.setUsingNightVision(false);
            player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
            VampireMessages.sendLocalized(player, "nightvision.end");
        }
    }
    
    /**
     * Updates a player's infection progression.
     * 
     * @param vampirePlayer The player to update
     * @param player The player object
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateInfection(VampirePlayer vampirePlayer, Player player, double deltaSeconds) {
        if (vampirePlayer.isVampire() || !vampirePlayer.isInfected()) {
            return;
        }
        
        double infectionIncrease = plugin.getVampireConfig().getInfectionRate() * deltaSeconds;
        vampirePlayer.setInfectionLevelInternal(vampirePlayer.getInfectionLevel() + infectionIncrease);
        
        if (vampirePlayer.getInfectionLevel() > 0.5) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.WEAKNESS,
                (int)(40 * deltaSeconds),
                0,
                true,
                false
            ));
        }
        
        if (vampirePlayer.getInfectionLevel() > 0.8) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SLOWNESS,
                (int)(40 * deltaSeconds),
                0,
                true,
                false
            ));
        }
        
        if (vampirePlayer.getInfectionLevel() >= 1.0) {
            VampireMessages.debug("Player " + player.getName() + " reached full infection. Converting...");
            vampireManager.setVampireStatus(vampirePlayer.getUuid(), true, "Infection");
            FxUtil.playVampireEffect(player);
            
            // Use VampireMessages to get the localized broadcast message
            // Serialize player's display name Component to string for the message format
            String playerNameString = LegacyComponentSerializer.legacySection().serialize(player.displayName()); 
            String broadcastMessage = VampireMessages.getLocalizedMessage("infection.broadcast", playerNameString);
            // Deserialize the formatted string message into a Component and broadcast
            Component messageComponent = LegacyComponentSerializer.legacySection().deserialize(broadcastMessage);
            Bukkit.broadcast(messageComponent);
        }
    }
    
    /**
     * Updates a player's environmental damage.
     * 
     * @param vampirePlayer The player to update
     * @param player The player object
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateEnvironmentalDamage(VampirePlayer vampirePlayer, Player player, double deltaSeconds) {
        if (!vampirePlayer.isVampire()) {
            VampireMessages.debug("Environmental damage check skipped for " + player.getName() + ": not a vampire.");
            return; // Not a vampire
        }
        
        if (!plugin.getVampireConfig().isSunDamage()) {
            VampireMessages.debug("Sun damage check skipped for " + player.getName() + ": globally disabled in config.");
            return;
        }

        // Calculate Irradiation
        double irradiation = SunUtil.calcPlayerIrradiation(player);
        VampireMessages.debug("[Sun Check] Player: " + player.getName() + ", Irradiation: " + String.format("%.3f", irradiation));
        
        if (irradiation <= 0) {
            VampireMessages.debug("[Sun Check] No irradiation for " + player.getName() + ", skipping damage/effects.");
            return;
        }

        // Calculate Damage
        double baseDamagePerSecond = plugin.getVampireConfig().getSunlightBaseDamage();
        double damage = baseDamagePerSecond * irradiation * deltaSeconds;
        VampireMessages.debug("[Sun Check] Player: " + player.getName() + ", BaseDamage: " + baseDamagePerSecond + ", Delta: " + deltaSeconds + ", Calculated Damage: " + String.format("%.3f", damage));
        
        // Apply Potion Effects based on thresholds
        int effectDurationTicks = (int)(40 * deltaSeconds); // Short duration, ~2 seconds if task delay is 20 ticks
        if (irradiation > plugin.getVampireConfig().getSunlightWeaknessThreshold()) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.WEAKNESS, effectDurationTicks, 0, true, false));
            VampireMessages.debug("[Sun Check] Applied Weakness to " + player.getName());
        }
        if (irradiation > plugin.getVampireConfig().getSunlightSlownessThreshold()) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SLOWNESS, effectDurationTicks, 0, true, false));
            VampireMessages.debug("[Sun Check] Applied Slowness to " + player.getName());
        }
        if (irradiation > plugin.getVampireConfig().getSunlightBlindnessThreshold()) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.BLINDNESS, effectDurationTicks, 0, true, false));
            VampireMessages.debug("[Sun Check] Applied Blindness to " + player.getName());
        }

        // Apply Damage and Messages/Burn Effect
        if (damage > 0.0001) {
			// Schedule the actual damage and effects to run synchronously
			final double finalDamage = damage; // Final variable for lambda
			final double finalIrradiation = irradiation; // Final variable for lambda
			org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
				// ---- START ADDED DEBUG LOGGING ----
				VampireMessages.debug("[Sun Check][Sync Task START] Processing for: " + player.getName());
				// Check if player is still valid inside the scheduled task
				if (!player.isValid() || !vampirePlayer.isVampire()) {
					 VampireMessages.debug("[Sun Check][Sync Task] Player invalid ("+player.isValid()+") or no longer vampire ("+vampirePlayer.isVampire()+"), skipping scheduled damage.");
					 return;
				}
				VampireMessages.debug("[Sun Check][Sync Task] Player valid and is vampire.");
				// ---- END ADDED DEBUG LOGGING ----

				VampireMessages.debug("[Sun Check][Sync] Damage > 0 block entered."); // Log moved inside sync task
				VampireMessages.debug("[Sun Check][Sync] Applying " + String.format("%.3f", finalDamage) + " sun damage to " + player.getName());
				// Use Bukkit's damage method; ensure event handling logic (like regen cooldown) works
				player.damage(finalDamage); // <-- Potential point of silent failure?

				// ---- START ADDED DEBUG LOGGING ----
				VampireMessages.debug("[Sun Check][Sync Task] Damage applied. Current Health: " + player.getHealth());
				// ---- END ADDED DEBUG LOGGING ----

				// Send messages based on intensity
				if (finalIrradiation > 0.7) { // Threshold for \"burning\" message
					VampireMessages.debug("[Sun Check][Sync] Sent 'burning' message to " + player.getName());
					VampireMessages.sendLocalized(player, "sunlight.burning");
				} else if (finalIrradiation > 0.2) { // Threshold for \"uncomfortable\" message
					VampireMessages.debug("[Sun Check][Sync] Sent 'uncomfortable' message to " + player.getName());
					VampireMessages.sendLocalized(player, "sunlight.uncomfortable");
				}

				// Apply visual burn effect at high intensity
				if (finalIrradiation > 0.8) { // Threshold for visual burn
					int burnTicks = Math.max(1, (int)(20 * deltaSeconds)); // Note: deltaSeconds is from the outer scope, might be slightly stale but okay for effect duration
					// ---- START ADDED DEBUG LOGGING ----
					VampireMessages.debug("[Sun Check][Sync Task] Attempting to apply visual burn (" + burnTicks + " ticks) to " + player.getName());
					// ---- END ADDED DEBUG LOGGING ----
					FxUtil.ensureBurn(player, burnTicks);
					VampireMessages.debug("[Sun Check][Sync] Applied visual burn (" + burnTicks + " ticks) to " + player.getName() + ". Fire Ticks: " + player.getFireTicks());
				}

				// Play flame particles - safe to call async usually, but keep here for simplicity
				int particleCount = (int) Math.max(1, Math.min(20, finalIrradiation * 15));
				// ---- START ADDED DEBUG LOGGING ----
				VampireMessages.debug("[Sun Check][Sync Task] Spawning " + particleCount + " flame particles for " + player.getName());
				// ---- END ADDED DEBUG LOGGING ----
				FxUtil.playParticle(player.getEyeLocation(), org.bukkit.Particle.FLAME, particleCount, 0.3, 0.3, 0.3, 0.05);
				// ---- START ADDED DEBUG LOGGING ----
				VampireMessages.debug("[Sun Check][Sync Task END] Finished processing for: " + player.getName());
				// ---- END ADDED DEBUG LOGGING ----
			});
        } else {
            VampireMessages.debug("[Sun Check] Calculated damage was zero or less for " + player.getName() + ", no damage applied.");
        }
    }

    /**
     * Updates the player's food bar to visually represent their blood level.
     *
     * @param vampirePlayer The player to update.
     * @param player The player object.
     */
    private void updateFoodBarVisual(VampirePlayer vampirePlayer, Player player) {
        if (!vampirePlayer.isVampire()) {
            // If somehow called for a non-vampire, do nothing or reset to default?
            // For now, do nothing.
            return;
        }

        double currentBlood = vampirePlayer.getBlood();
        double maxBlood = vampireManager.getEffectiveMaxBlood(vampirePlayer);
        double bloodPercentage = (maxBlood > 0) ? (currentBlood / maxBlood) : 0.0;

        // Scale to the 0-20 food bar range
        int visualFoodLevel = (int) Math.round(bloodPercentage * 20.0);
        visualFoodLevel = Math.max(0, Math.min(20, visualFoodLevel)); // Clamp just in case

        // Only update if the visual level needs changing
        if (player.getFoodLevel() != visualFoodLevel) {
            player.setFoodLevel(visualFoodLevel);
             VampireMessages.debug("Set food bar visual for " + player.getName() + " to " + visualFoodLevel + " based on blood " + currentBlood + "/" + maxBlood);
        }
        
        // Keep saturation high to prevent visual shaking/regen attempts
        // (Set it even if food level didn't change, as saturation might drop otherwise)
        if (player.getSaturation() < 10f) { // Only set if it's low to avoid unnecessary updates
             player.setSaturation(20f);
        }
    }
} 