package org.clockworx.vampire.task;

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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
        
        updateBlood(vampirePlayer, player, deltaSeconds);
        updateBloodlust(vampirePlayer, player, deltaSeconds);
        updateNightVision(vampirePlayer, player, deltaSeconds);
        updateInfection(vampirePlayer, player, deltaSeconds);
        updateEnvironmentalDamage(vampirePlayer, player, deltaSeconds);

        // NEW: Update food bar visual
        updateFoodBarVisual(vampirePlayer, player);
    }
    
    /**
     * Updates a player's blood management.
     * 
     * @param vampirePlayer The player to update
     * @param player The player object
     * @param deltaSeconds The time since the last update in seconds
     */
    private void updateBlood(VampirePlayer vampirePlayer, Player player, double deltaSeconds) {
        if (!vampirePlayer.isVampire()) {
            return;
        }
        
        double bloodDecrease = plugin.getVampireConfig().getBloodDecreaseRate() * deltaSeconds;
        vampirePlayer.setBloodInternal(vampirePlayer.getBlood() - bloodDecrease);
        
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
            
            VampireMessages.broadcast(plugin.getLanguageConfig().getMessage("infection.broadcast")
                .replace("%player%", player.getName()));
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
            return;
        }
        
        if (!plugin.getVampireConfig().isSunDamage()) {
            return;
        }
        
        double irradiation = SunUtil.calcPlayerIrradiation(player);

        if (irradiation <= 0) {
            return;
        }
        
        double baseDamagePerSecond = plugin.getVampireConfig().getSunlightBaseDamage();

        double damage = baseDamagePerSecond * irradiation * deltaSeconds;

        if (damage > 0) {
            VampireMessages.debug("Applying sun damage to " + player.getName() + ": " + damage + ", Irradiation: " + irradiation);
            player.damage(damage); 
            
            if (irradiation > 0.7) {
                VampireMessages.sendLocalized(player, "sunlight.burning");
            } else if (irradiation > 0.2) {
                VampireMessages.sendLocalized(player, "sunlight.uncomfortable");
            }
                
            if (irradiation > 0.8) { 
                FxUtil.ensureBurn(player, Math.max(1, (int)(20 * deltaSeconds))); 
                }
            
            int particleCount = (int) Math.max(1, Math.min(20, irradiation * 15));
            FxUtil.playParticle(player.getEyeLocation(), org.bukkit.Particle.FLAME, particleCount, 0.3, 0.3, 0.3, 0.05);
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