package org.clockworx.vampire.task;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.util.FxUtil;
import org.clockworx.vampire.util.ResourceUtil;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Task that runs periodically to update vampire players.
 * This class handles bloodlust, night vision, infection progression, and environmental damage.
 */
public class VampireTask extends BukkitRunnable {
    
    private final VampirePlugin plugin;
    private int taskId = -1;
    private long lastRun = 0;
    
    /**
     * Creates a new VampireTask.
     * 
     * @param plugin The plugin instance
     */
    public VampireTask(VampirePlugin plugin) {
        this.plugin = plugin;
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
        lastRun = now;
        
        // Update all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            
            // Load player data asynchronously
            plugin.getVampirePlayer(uuid).thenAccept(vampirePlayer -> {
                if (vampirePlayer == null) {
                    return;
                }
                
                // Update player state
                updatePlayer(vampirePlayer, delta);
            });
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
        
        // Skip if player is in creative mode or has permission to bypass
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || 
            player.hasPermission("vampire.bypass")) {
            return;
        }
        
        // Convert delta to seconds
        double deltaSeconds = delta / 1000.0;
        
        // Update blood management
        updateBlood(vampirePlayer, deltaSeconds);
        
        // Update bloodlust
        updateBloodlust(vampirePlayer, deltaSeconds);
        
        // Update night vision
        updateNightVision(vampirePlayer, deltaSeconds);
        
        // Update infection progression
        updateInfection(vampirePlayer, deltaSeconds);
        
        // Update environmental damage
        updateEnvironmentalDamage(vampirePlayer, deltaSeconds);
        
        // Save player data periodically
        if (System.currentTimeMillis() % 60000 < delta) { // Save every minute
            vampirePlayer.save();
        }
    }
    
    /**
     * Updates a player's blood management.
     * 
     * @param vampirePlayer The player to update
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateBlood(VampirePlayer vampirePlayer, double deltaSeconds) {
        if (!vampirePlayer.isVampire()) {
            return;
        }
        
        Player player = vampirePlayer.getPlayer();
        if (player == null) {
            return;
        }
        
        // Decrease blood over time
        double bloodDecrease = plugin.getVampireConfig().getBloodDecreaseRate() * deltaSeconds;
        vampirePlayer.setBlood(Math.max(0, vampirePlayer.getBlood() - bloodDecrease));
        
        // Apply effects based on blood level
        if (vampirePlayer.getBlood() < plugin.getVampireConfig().getLowBloodThreshold()) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.WEAKNESS,
                (int)(20 * deltaSeconds),
                1,
                false,
                false
            ));
            
            ResourceUtil.sendWarning(player, plugin.getLanguageConfig().getMessage("blood.low"));
        }
    }
    
    /**
     * Updates a player's bloodlust state.
     * 
     * @param vampirePlayer The player to update
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateBloodlust(VampirePlayer vampirePlayer, double deltaSeconds) {
        if (!vampirePlayer.isVampire()) {
            return;
        }
        
        Player player = vampirePlayer.getPlayer();
        if (player == null) {
            return;
        }
        
        // Check if player should enter bloodlust
        if (!vampirePlayer.isBloodlusting() && vampirePlayer.getBlood() < plugin.getVampireConfig().getBloodlustThreshold()) {
            vampirePlayer.setBloodlusting(true);
            ResourceUtil.sendWarning(player, plugin.getLanguageConfig().getMessage("bloodlust.start"));
            FxUtil.playVampireEffect(player);
        }
        
        // Apply bloodlust effects
        if (vampirePlayer.isBloodlusting()) {
            // Increase speed and strength
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED, 
                (int)(20 * deltaSeconds), 
                1, 
                false, 
                false
            ));
            
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE, 
                (int)(20 * deltaSeconds), 
                0, 
                false, 
                false
            ));
            
            // Decrease blood over time during bloodlust
            double bloodDecrease = plugin.getVampireConfig().getBloodlustBloodDecrease() * deltaSeconds;
            vampirePlayer.setBlood(Math.max(0, vampirePlayer.getBlood() - bloodDecrease));
            
            // Exit bloodlust if blood is restored
            if (vampirePlayer.getBlood() >= plugin.getVampireConfig().getBloodlustThreshold()) {
                vampirePlayer.setBloodlusting(false);
                ResourceUtil.sendSuccess(player, plugin.getLanguageConfig().getMessage("bloodlust.end"));
            }
        }
    }
    
    /**
     * Updates a player's night vision state.
     * 
     * @param vampirePlayer The player to update
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateNightVision(VampirePlayer vampirePlayer, double deltaSeconds) {
        if (!vampirePlayer.isVampire()) {
            return;
        }
        
        Player player = vampirePlayer.getPlayer();
        if (player == null) {
            return;
        }
        
        World world = player.getWorld();
        boolean isNight = world.getTime() >= 13000 || world.getTime() <= 23000;
        
        // Check if player should use night vision
        if (!vampirePlayer.isUsingNightVision() && isNight) {
            vampirePlayer.setUsingNightVision(true);
            ResourceUtil.sendInfo(player, plugin.getLanguageConfig().getMessage("nightvision.start"));
        }
        
        // Apply night vision
        if (vampirePlayer.isUsingNightVision() && isNight) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.NIGHT_VISION, 
                (int)(20 * deltaSeconds), 
                0, 
                false, 
                false
            ));
        }
        
        // Disable night vision during day
        if (vampirePlayer.isUsingNightVision() && !isNight) {
            vampirePlayer.setUsingNightVision(false);
            player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
            ResourceUtil.sendInfo(player, plugin.getLanguageConfig().getMessage("nightvision.end"));
        }
    }
    
    /**
     * Updates a player's infection progression.
     * 
     * @param vampirePlayer The player to update
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateInfection(VampirePlayer vampirePlayer, double deltaSeconds) {
        if (vampirePlayer.isVampire() || !vampirePlayer.isInfected()) {
            return;
        }
        
        Player player = vampirePlayer.getPlayer();
        if (player == null) {
            return;
        }
        
        // Increase infection over time
        double infectionIncrease = plugin.getVampireConfig().getInfectionRate() * deltaSeconds;
        vampirePlayer.setInfectionLevel(Math.min(1.0, vampirePlayer.getInfectionLevel() + infectionIncrease));
        
        // Check infection level
        if (vampirePlayer.getInfectionLevel() > 0.5) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.WEAKNESS, 
                (int)(20 * deltaSeconds), 
                0, 
                false, 
                false
            ));
        }
        
        // Convert to vampire if infection reaches 100%
        if (vampirePlayer.getInfectionLevel() >= 1.0) {
            vampirePlayer.setVampire(true);
            vampirePlayer.setInfectionLevel(0.0);
            ResourceUtil.sendWarning(player, plugin.getLanguageConfig().getMessage("infection.complete"));
            FxUtil.playVampireEffect(player);
            
            // Broadcast to server
            ResourceUtil.broadcastMessage(plugin.getLanguageConfig().getMessage("infection.broadcast")
                .replace("%player%", player.getName()));
        }
    }
    
    /**
     * Updates a player's environmental damage.
     * 
     * @param vampirePlayer The player to update
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateEnvironmentalDamage(VampirePlayer vampirePlayer, double deltaSeconds) {
        if (!vampirePlayer.isVampire()) {
            return;
        }
        
        Player player = vampirePlayer.getPlayer();
        if (player == null) {
            return;
        }
        
        World world = player.getWorld();
        boolean isNight = world.getTime() >= 13000 || world.getTime() <= 23000;
        
        // Apply sunlight damage during day
        if (!isNight && player.getLocation().getBlock().getLightFromSky() > 10) {
            // Check if player is in water or has protection
            if (player.isInWater() || player.getLocation().getBlock().getType().name().contains("WATER")) {
                // Water provides some protection but still causes damage
                double damage = plugin.getVampireConfig().getSunlightDamage() * 0.5 * deltaSeconds;
                player.damage(damage);
                ResourceUtil.sendWarning(player, plugin.getLanguageConfig().getMessage("sunlight.water"));
            } else if (player.getInventory().getHelmet() != null && 
                      player.getInventory().getHelmet().getType().name().contains("HELMET")) {
                // Helmets provide some protection
                double damage = plugin.getVampireConfig().getSunlightDamage() * 0.7 * deltaSeconds;
                player.damage(damage);
                ResourceUtil.sendWarning(player, plugin.getLanguageConfig().getMessage("sunlight.helmet"));
            } else {
                // Full sunlight damage
                double damage = plugin.getVampireConfig().getSunlightDamage() * deltaSeconds;
                player.damage(damage);
                ResourceUtil.sendWarning(player, plugin.getLanguageConfig().getMessage("sunlight.damage"));
                
                // Set player on fire
                FxUtil.ensureBurn(player, (int)(20 * deltaSeconds));
            }
            
            // Play effects
            FxUtil.playParticle(player.getLocation(), org.bukkit.Particle.FLAME, 10, 0.2, 0.2, 0.2, 0.1);
        }
    }
} 