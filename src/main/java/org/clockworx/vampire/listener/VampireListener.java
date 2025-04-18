package org.clockworx.vampire.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.event.EventVampirePlayerBloodChange;
import org.clockworx.vampire.event.EventVampirePlayerInfectionChange;
import org.clockworx.vampire.event.EventVampirePlayerModeChange;
import org.clockworx.vampire.event.EventVampirePlayerShriek;
import org.clockworx.vampire.event.EventVampirePlayerVampireChange;
import org.clockworx.vampire.entity.VampirePlayer;

/**
 * Main listener class for the Vampire plugin that handles all vampire-related events.
 * This class manages player state changes, effects, and interactions related to the vampire system.
 * It handles:
 * - Player join/quit events for data management
 * - Vampire-specific events (mode changes, shrieks, blood/infection changes)
 * - Combat and damage modifications
 * - Status effect applications
 */
public class VampireListener implements Listener {
    private final VampirePlugin plugin;
    
    /**
     * Constructs a new VampireListener with the specified plugin instance.
     * 
     * @param plugin The VampirePlugin instance that this listener belongs to
     */
    public VampireListener(VampirePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles player join events by loading their vampire data and applying appropriate effects.
     * This method is called when a player joins the server and:
     * 1. Loads the player's vampire data from the database
     * 2. Applies vampire effects if the player is a vampire
     * 
     * @param event The PlayerJoinEvent that triggered this handler
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Load player data from database
        plugin.getVampirePlayer(player.getUniqueId()).thenAccept(vampirePlayer -> {
            if (vampirePlayer != null && vampirePlayer.isVampire()) {
                applyVampireEffects(player);
            }
        });
    }
    
    /**
     * Handles player quit events by saving their vampire data to the database.
     * This ensures that all player data is persisted when they leave the server.
     * 
     * @param event The PlayerQuitEvent that triggered this handler
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Save player data to database
        plugin.getVampirePlayer(player.getUniqueId()).thenAccept(vampirePlayer -> {
            if (vampirePlayer != null) {
                plugin.saveVampirePlayer(vampirePlayer);
            }
        });
    }
    
    /**
     * Handles vampire shriek events by applying visual and gameplay effects.
     * When a vampire shrieks:
     * 1. Creates lightning effect at player's location
     * 2. Spawns smoke particles
     * 3. Applies blindness and slowness to nearby players
     * 
     * @param event The EventVampirePlayerShriek that triggered this handler
     */
    @EventHandler
    public void onVampireShriek(EventVampirePlayerShriek event) {
        Player player = event.getVampirePlayer().getPlayer();
        if (player == null) return;
        
        // Play shriek sound and particle effects
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.getWorld().spawnParticle(org.bukkit.Particle.SMOKE_LARGE, player.getLocation(), 50, 1, 1, 1, 0.1);
        
        // Apply effects to nearby players
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.getLocation().distance(player.getLocation()) <= 10) {
                nearby.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                nearby.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 1));
            }
        }
    }
    
    /**
     * Handles vampire mode change events by applying appropriate effects based on the new mode.
     * Supports three modes:
     * - bloodlust: Increases combat effectiveness
     * - nightvision: Improves visibility in dark areas
     * - intent: Changes infection behavior
     * 
     * @param event The EventVampirePlayerModeChange that triggered this handler
     */
    @EventHandler
    public void onVampireMode(EventVampirePlayerModeChange event) {
        Player player = event.getVampirePlayer().getPlayer();
        if (player == null) return;
        
        String mode = event.getMode();
        
        switch (mode.toLowerCase()) {
            case "bloodlust":
                applyBloodlustMode(player);
                break;
            case "nightvision":
                applyNightVisionMode(player);
                break;
            case "intent":
                // Intent mode is handled by the player's intent
                break;
        }
    }
    
    /**
     * Handles entity damage events to modify damage dealt by vampire players.
     * Vampires deal increased damage based on their blood level:
     * - Higher blood levels result in more damage
     * - Damage multiplier is calculated as: 1.0 + (bloodLevel * 0.1)
     * 
     * @param event The EntityDamageByEntityEvent that triggered this handler
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        
        Player attacker = (Player) event.getDamager();
        
        // Check if attacker is a vampire
        plugin.getVampirePlayer(attacker.getUniqueId()).thenAccept(vampirePlayer -> {
            if (vampirePlayer != null && vampirePlayer.isVampire()) {
                // Apply vampire damage modifiers
                double damage = event.getDamage();
                double bloodLevel = vampirePlayer.getBlood();
                
                // Increase damage based on blood level
                double multiplier = 1.0 + (bloodLevel * 0.1);
                event.setDamage(damage * multiplier);
            }
        });
    }
    
    /**
     * Handles entity damage events to modify damage received by vampire players.
     * Vampires receive reduced damage based on their blood level:
     * - Higher blood levels provide more damage resistance
     * - Resistance is capped at 50%
     * - Resistance is calculated as: 0.05 * bloodLevel
     * 
     * @param event The EntityDamageEvent that triggered this handler
     */
    @EventHandler
    public void onEntityDamageReceive(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Check if player is a vampire
        plugin.getVampirePlayer(player.getUniqueId()).thenAccept(vampirePlayer -> {
            if (vampirePlayer != null && vampirePlayer.isVampire()) {
                // Apply vampire damage resistance
                double damage = event.getDamage();
                double bloodLevel = vampirePlayer.getBlood();
                
                // Reduce damage based on blood level
                double resistance = 0.05 * bloodLevel;
                if (resistance > 0.5) resistance = 0.5; // Cap at 50% resistance
                event.setDamage(damage * (1.0 - resistance));
            }
        });
    }
    
    /**
     * Handles blood level change events for vampire players.
     * Applies effects when blood level is low:
     * - Weakness effect is applied
     * - Warning message is sent to the player
     * 
     * @param event The EventVampirePlayerBloodChange that triggered this handler
     */
    @EventHandler
    public void onVampirePlayerBloodChange(EventVampirePlayerBloodChange event) {
        Player player = event.getVampirePlayer().getPlayer();
        if (player == null) return;
        
        double blood = event.getBlood();
        
        // Apply effects based on blood level
        if (blood < plugin.getVampireConfig().getLowBloodThreshold()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 1, false, false));
            player.sendMessage("§cYou are low on blood!");
        }
    }
    
    /**
     * Handles infection level change events for players.
     * Applies effects when infection level is high:
     * - Weakness effect is applied
     * - Effects intensify with higher infection levels
     * 
     * @param event The EventVampirePlayerInfectionChange that triggered this handler
     */
    @EventHandler
    public void onVampirePlayerInfectionChange(EventVampirePlayerInfectionChange event) {
        Player player = event.getVampirePlayer().getPlayer();
        if (player == null) return;
        
        double infection = event.getInfection();
        
        // Apply effects based on infection level
        if (infection > 0.5) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, false));
        }
    }
    
    /**
     * Handles vampire status change events for players.
     * Manages the transition between human and vampire states:
     * - Applies vampire effects when becoming a vampire
     * - Removes effects when cured
     * - Notifies the player of their status change
     * 
     * @param event The EventVampirePlayerVampireChange that triggered this handler
     */
    @EventHandler
    public void onVampirePlayerVampireChange(EventVampirePlayerVampireChange event) {
        Player player = event.getVampirePlayer().getPlayer();
        if (player == null) return;
        
        boolean isVampire = event.isVampire();
        
        if (isVampire) {
            // Player became a vampire
            player.sendMessage("§cYou are now a vampire!");
            applyVampireEffects(player);
        } else {
            // Player was cured
            player.sendMessage("§aYou are no longer a vampire.");
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }
    
    /**
     * Applies standard vampire effects to a player.
     * These effects are applied when:
     * - A player becomes a vampire
     * - A vampire player joins the server
     * Effects are configured in the plugin's config file.
     * 
     * @param player The player to apply vampire effects to
     */
    private void applyVampireEffects(Player player) {
        // Apply night vision if enabled in config
        if (plugin.getVampireConfig().isNightVisionEnabled()) {
            int level = plugin.getVampireConfig().getNightVisionLevel();
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, level - 1, true, false));
        }
    }
    
    /**
     * Applies bloodlust mode effects to a player.
     * Bloodlust mode provides combat enhancements:
     * - Increased damage (configurable multiplier)
     * - Increased movement speed
     * - Effects duration is configurable
     * 
     * @param player The player to apply bloodlust effects to
     */
    private void applyBloodlustMode(Player player) {
        // Apply speed and strength effects
        int duration = plugin.getVampireConfig().getBloodlustDuration() * 20; // Convert to ticks
        double damageBoost = plugin.getVampireConfig().getBloodlustDamageBoost();
        double speedBoost = plugin.getVampireConfig().getBloodlustSpeedBoost();
        
        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, duration, (int)(damageBoost - 1), true, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, (int)(speedBoost - 1), true, true));
        
        // Set cooldown
        plugin.getVampirePlayer(player.getUniqueId()).thenAccept(vampirePlayer -> {
            if (vampirePlayer != null) {
                vampirePlayer.setLastBloodlustTime(System.currentTimeMillis());
                plugin.saveVampirePlayer(vampirePlayer);
            }
        });
    }
    
    /**
     * Applies night vision mode effects to a player.
     * Night vision mode can be toggled on/off:
     * - When enabled: Provides night vision effect
     * - When disabled: Removes night vision effect
     * Effect level is configurable in the plugin's config file.
     * 
     * @param player The player to apply night vision effects to
     */
    private void applyNightVisionMode(Player player) {
        // Toggle night vision effect
        if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        } else {
            int level = plugin.getVampireConfig().getNightVisionLevel();
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, level - 1, true, false));
        }
    }
} 