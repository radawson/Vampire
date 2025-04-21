package org.clockworx.vampire.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.manager.ItemManager;
import org.clockworx.vampire.event.EventVampirePlayerBloodChange;
import org.clockworx.vampire.event.EventVampirePlayerInfectionChange;
import org.clockworx.vampire.event.EventVampirePlayerModeChange;
import org.clockworx.vampire.event.EventVampirePlayerShriek;
import org.clockworx.vampire.event.EventVampirePlayerVampireChange;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.util.VampireMessages;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Color;
import org.bukkit.event.EventPriority;

import java.util.List;
import java.util.Random;

/**
 * Main listener class for the Vampire plugin.
 * Handles Bukkit events and delegates logic to VampireManager.
 */
public class VampireListener implements Listener {
    private final VampirePlugin plugin;
    private final VampireManager vampireManager;
    
    /**
     * Constructs a new VampireListener.
     * 
     * @param plugin The VampirePlugin instance.
     * @param vampireManager The VampireManager instance.
     */
    public VampireListener(VampirePlugin plugin, VampireManager vampireManager) {
        this.plugin = plugin;
        this.vampireManager = vampireManager;
    }
    
    /**
     * Handles player join events by notifying the VampireManager.
     * 
     * @param event The PlayerJoinEvent.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        vampireManager.handlePlayerJoin(player);
    }
    
    /**
     * Handles player quit events by notifying the VampireManager.
     * 
     * @param event The PlayerQuitEvent.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        vampireManager.handlePlayerQuit(player);
    }
    
    /**
     * Handles vampire shriek events (assuming this event is still fired).
     * Applies visual and gameplay effects.
     * 
     * @param event The EventVampirePlayerShriek.
     */
    @EventHandler
    public void onVampireShriek(EventVampirePlayerShriek event) {
        VampirePlayer vp = event.getVampirePlayer();
        Player player = vp.getPlayer();
        if (player == null) return;
        
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.getWorld().spawnParticle(org.bukkit.Particle.LARGE_SMOKE, player.getLocation(), 50, 1, 1, 1, 0.1);
        
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.getLocation().distance(player.getLocation()) <= 10) {
                nearby.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                nearby.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            }
        }
    }
    
    /**
     * Handles vampire mode change events (assuming event is still fired).
     * Note: Mode effects are likely applied by VampireManager when state changes.
     * This listener might just be for reacting *after* the change.
     * 
     * @param event The EventVampirePlayerModeChange.
     */
    @EventHandler
    public void onVampireMode(EventVampirePlayerModeChange event) {
        // Logic here might be deprecated if manager handles effects directly
        // Player player = event.getVampirePlayer().getPlayer();
        // if (player == null) return;
        // String mode = event.getMode();
        // VampireMessages.debug("Player " + player.getName() + " mode changed to: " + mode);
        // No need to call apply*Mode methods here anymore.
    }
    
    /**
     * Handles entity damage events to modify damage dealt BY vampire players.
     * 
     * @param event The EntityDamageByEntityEvent.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        Player attacker = (Player) event.getDamager();
        VampirePlayer vpAttacker = vampireManager.getCachedVampirePlayer(attacker.getUniqueId());

        // Apply Vampire Damage Bonus
        if (vpAttacker != null && vpAttacker.isVampire()) {
            double damage = event.getDamage();
            double bloodLevel = vpAttacker.getBlood();
            // TODO: Refine damage calculation based on config/modes if needed
            double multiplier = 1.0 + (bloodLevel * 0.05); // Example: 5% bonus per blood point
            event.setDamage(damage * multiplier);
            VampireMessages.debug("Applying damage multiplier (" + multiplier + ") for vampire " + attacker.getName());

            // Check for Infection Intent
            if (vpAttacker.isIntending() && event.getEntity() instanceof Player) {
                Player target = (Player) event.getEntity();
                VampirePlayer vpTarget = vampireManager.getCachedVampirePlayer(target.getUniqueId());

                // Check if target is valid (not null, not already vampire, etc.)
                if (vpTarget != null && !vpTarget.isVampire()) {
                    double bloodCost = plugin.getVampireConfig().getInfectBloodCost();
                    double infectionAmount = plugin.getVampireConfig().getInfectInfectionAmount();
                    
                    // Check if attacker has enough blood
                    if (vpAttacker.getBlood() >= bloodCost) {
                        // Use manager methods to apply changes
                        boolean bloodUsed = vampireManager.useBlood(vpAttacker.getUuid(), bloodCost);
                        if (bloodUsed) {
                            String reason = "Attacked by vampire " + attacker.getName();
                            vampireManager.addInfection(vpTarget.getUuid(), infectionAmount, reason);
                            VampireMessages.debug(attacker.getName() + " infected " + target.getName() + " slightly (cost: "+bloodCost+" blood)");
                            // TODO: Add visual/sound effect for infection hit?
                        } else {
                            // Should ideally not happen if check passed, but log if it does
                            VampireMessages.debug(attacker.getName() + " failed to use blood for infection despite check passing.");
                        }
                    } else {
                        // Optional: Send message to attacker that they are too low on blood to infect
                        VampireMessages.sendLocalized(attacker, "infection.fail.low_blood");
                    }
                }
            }
        }
        
        // --- NEW: Handle Blood Gain --- 
        if (vpAttacker != null && vpAttacker.isVampire() && event.getEntity() instanceof LivingEntity) {
            LivingEntity victim = (LivingEntity) event.getEntity();
            EntityType victimType = victim.getType();
            
            double bloodGain = plugin.getVampireConfig().getBloodGain(victimType);

            if (bloodGain > 0) {
                 VampireMessages.debug(attacker.getName() + " attempting to gain blood from " + victimType + " ("+bloodGain+" potential)");
                // Gain blood
                vampireManager.addBlood(vpAttacker.getUuid(), bloodGain);
                // TODO: Add visual/sound effect for successful blood drain?
                // Optional: Apply health cost if the victim is a player
                if (victim instanceof Player) {
                    double healthCost = plugin.getVampireConfig().getPlayerHealthCostOnHit();
                    if (healthCost > 0) {
                        // Ensure victim doesn't die from this specific damage if health is low
                        double newHealth = Math.max(0.1, victim.getHealth() - healthCost);
                        victim.setHealth(newHealth);
                         VampireMessages.debug("Applied health cost ("+healthCost+") to player " + victim.getName());
                    }
                }
            }
        }
        
        // Removed previous TODO as infection logic is now added
    }
    
    /**
     * Handles entity damage events to modify damage RECEIVED BY vampire players.
     * 
     * @param event The EntityDamageEvent.
     */
    @EventHandler
    public void onEntityDamageReceive(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();
        VampirePlayer vpVictim = vampireManager.getCachedVampirePlayer(victim.getUniqueId());

        if (vpVictim != null && vpVictim.isVampire()) {
            // Record the time of damage for regeneration cooldown
            vpVictim.setLastDamageTime(System.currentTimeMillis());

            // Apply vampire damage resistance
            double damage = event.getDamage();
            double bloodLevel = vpVictim.getBlood();
            double resistance = Math.min(0.5, bloodLevel * 0.05);
            
            double finalDamage = damage * (1.0 - resistance);
            event.setDamage(finalDamage);
            VampireMessages.debug("Applying damage resistance (" + resistance*100 + "%) for vampire " + victim.getName() + ". Original: " + damage + ", Final: " + finalDamage);
        }
    }
    
    /**
     * Handles blood level change events (if still needed/fired).
     * Low blood effects are likely handled by VampireTask or manager state changes.
     * 
     * @param event The EventVampirePlayerBloodChange.
     */
    @EventHandler
    public void onVampirePlayerBloodChange(EventVampirePlayerBloodChange event) {
         // Logic likely moved to VampireTask or VampireManager when setBlood is called
         // Player player = event.getVampirePlayer().getPlayer();
         // if (player == null) return;
         // double blood = event.getBlood();
         // if (blood < vampireManager.getLowBloodThreshold()) { // Get threshold via manager
         //    vampireManager.applyLowBloodEffects(player); // Delegate effect application
         // }
    }
    
    /**
     * Handles infection level change events (if still needed/fired).
     * Infection effects are likely handled by VampireTask or manager state changes.
     * 
     * @param event The EventVampirePlayerInfectionChange.
     */
    @EventHandler
    public void onVampirePlayerInfectionChange(EventVampirePlayerInfectionChange event) {
        // Logic likely moved to VampireTask or VampireManager when setInfection is called
        // Player player = event.getVampirePlayer().getPlayer();
        // if (player == null) return;
        // double infection = event.getInfection();
        // if (infection > 0.5) {
        //     vampireManager.applyInfectionEffects(player, infection); // Delegate
        // }
    }
    
    /**
     * Handles vampire status change events (if still needed/fired).
     * Effects/permission changes are handled by VampireManager when state changes.
     * 
     * @param event The EventVampirePlayerVampireChange.
     */
    @EventHandler
    public void onVampirePlayerVampireChange(EventVampirePlayerVampireChange event) {
        // Logic handled by VampireManager.setVampireStatus()
        // VampirePlayer vp = event.getVampirePlayer();
        // Player player = vp.getPlayer();
        // if (player != null) {
        //     VampireMessages.sendLocalized(player, event.isVampire() ? "vampire.become" : "vampire.cure");
        // }
    }

    /**
     * Prevents vampires from consuming regular food.
     *
     * @param event The PlayerItemConsumeEvent.
     */
    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        VampirePlayer vp = vampireManager.getCachedVampirePlayer(player.getUniqueId());

        if (vp != null && vp.isVampire()) {
            // Check if the item being consumed is food (could be potions, etc.)
            if (event.getItem().getType().isEdible()) {
                // Don't cancel the event, allow consumption animation
                // event.setCancelled(true); 
                VampireMessages.sendLocalized(player, "vampire.cannot_eat_food"); // Use the updated message
                // Optionally add a sound effect?
                 player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.5f);
                 // The VampireTask will quickly overwrite any temporary vanilla hunger/saturation gain
                 // by syncing the food bar to the blood level.
            }
        }
    }

    /**
     * Prevents natural hunger drain for vampires.
     *
     * @param event The FoodLevelChangeEvent.
     */
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        VampirePlayer vp = vampireManager.getCachedVampirePlayer(player.getUniqueId());

        if (vp != null && vp.isVampire()) {
            // We only want to allow food level changes initiated by our plugin
            // (which will happen in VampireTask to sync the blood visual).
            // All other causes (natural regen, exhaustion, etc.) should be cancelled.
            // Note: Directly setting food level via player.setFoodLevel() does NOT trigger this event.
            
            // Check if the food level is decreasing due to natural causes
            if (event.getFoodLevel() < player.getFoodLevel()) {
                event.setCancelled(true);
                // No message needed here, just silently prevent hunger loss.
                 VampireMessages.debug("Cancelled vanilla food level decrease for vampire " + player.getName());
            }
            // We don't need to cancel increases, as natural saturation/healing won't happen
            // if hunger isn't draining. Player.setFoodLevel also doesn't trigger this.
        }
    }

    /**
     * Handles splash potions, specifically checking for Blood Vials and Holy Water.
     *
     * @param event The PotionSplashEvent.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        ItemStack potionItem = event.getPotion().getItem();
        ItemManager itemManager = plugin.getItemManager();

        // Check if it's a Blood Vial
        if (itemManager.isBloodVial(potionItem)) {
            VampireMessages.debug("Blood Vial splashed at " + event.getEntity().getLocation().toString());
            // Visual effect for blood vial splash
            event.getEntity().getWorld().spawnParticle(Particle.DRAGON_BREATH, event.getEntity().getLocation(), 30, 0.5, 0.5, 0.5, 0.05);

            for (LivingEntity entity : event.getAffectedEntities()) {
                handleBloodVialSplash(entity);
            }
            event.setCancelled(true); // Cancel vanilla potion effect if any

        // Check if it's Holy Water
        } else if (itemManager.isHolyWater(potionItem)) {
            VampireMessages.debug("Holy Water splashed at " + event.getEntity().getLocation().toString());
            // Visual/Audio effect for holy water splash
            event.getEntity().getWorld().spawnParticle(Particle.END_ROD, event.getEntity().getLocation(), 50, 0.5, 0.5, 0.5, 0.1);
            event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1.0f, 1.0f);

            for (LivingEntity entity : event.getAffectedEntities()) {
                handleHolyWaterSplash(entity);
            }
            event.setCancelled(true); // Cancel vanilla potion effect if any
        }
    }

    /**
     * Handles the effect of a Blood Vial splashing on an entity.
     *
     * @param entity The affected LivingEntity.
     */
    private void handleBloodVialSplash(LivingEntity entity) {
        if (entity instanceof Player player) {
            // Use cached player data as this event is synchronous
            VampirePlayer vampirePlayer = plugin.getVampireManager().getCachedVampirePlayer(player.getUniqueId()); 
            if (vampirePlayer != null && !vampirePlayer.isVampire()) {
                double infectionAmount = plugin.getVampireConfig().getBloodVialInfectionAmount();
                if (infectionAmount > 0) {
                    plugin.getVampireManager().addInfection(player.getUniqueId(), infectionAmount, "Splashed by Blood Vial");
                    String debugMsg = String.format("Applied %.2f infection to %s from Blood Vial splash.", infectionAmount, player.getName());
                    VampireMessages.debug(debugMsg);
                    // Maybe send message to player?
                }
            }
        }
    }

    /**
     * Handles the effect of Holy Water splashing on an entity.
     *
     * @param entity The affected LivingEntity.
     */
    private void handleHolyWaterSplash(LivingEntity entity) {
        if (entity instanceof Player player) {
            // Use cached player data as this event is synchronous
            VampirePlayer vampirePlayer = plugin.getVampireManager().getCachedVampirePlayer(player.getUniqueId()); 
            if (vampirePlayer != null) {
                if (vampirePlayer.isVampire()) {
                    // Damage vampires
                    double damage = plugin.getVampireConfig().getHolyWaterVampireDamage();
                    player.damage(damage);
                    // Maybe add a sizzle sound/particle effect directly on the player?
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.5f);
                    String debugMsg = String.format("Dealt %.2f damage to vampire %s from Holy Water splash.", damage, player.getName());
                    VampireMessages.debug(debugMsg);
                } else if (vampirePlayer.getInfectionLevel() > 0) { // Correct method name
                    // Cure infection
                    double cureAmount = plugin.getVampireConfig().getHolyWaterInfectionCureAmount();
                    double oldInfection = vampirePlayer.getInfectionLevel(); // Correct method name
                    plugin.getVampireManager().addInfection(player.getUniqueId(), -cureAmount, "Splashed by Holy Water"); // Use UUID, negative amount, and add reason
                    // Re-fetch to get the updated value after addInfection potentially modifies it
                    VampirePlayer updatedVp = plugin.getVampireManager().getCachedVampirePlayer(player.getUniqueId());
                    double newInfection = (updatedVp != null) ? updatedVp.getInfectionLevel() : 0.0; // Correct method name & Safely get new infection
                    // Send message to player?
                    String debugMsg = String.format("Cured %.2f infection from %s (%.2f -> %.2f) with Holy Water splash.", cureAmount, player.getName(), oldInfection, newInfection);
                    VampireMessages.debug(debugMsg);
                    // Play cure sound/particle?
                    player.getWorld().spawnParticle(Particle.HEART, player.getEyeLocation(), 5, 0.5, 0.5, 0.5);
                }
            }
        } else if (isUndead(entity.getType())) {
            // Damage standard undead mobs
            double damage = plugin.getVampireConfig().getHolyWaterUndeadDamage();
            entity.damage(damage);
            entity.getWorld().playSound(entity.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.5f);
            String debugMsg = String.format("Dealt %.2f damage to undead %s from Holy Water splash.", damage, entity.getType().name());
            VampireMessages.debug(debugMsg);
        }
    }

    /**
     * Helper method to check if an entity type is considered undead.
     *
     * @param type The EntityType.
     * @return True if undead, false otherwise.
     */
    private boolean isUndead(EntityType type) {
        return switch (type) {
            case ZOMBIE, SKELETON, STRAY, HUSK, ZOMBIE_VILLAGER, ZOMBIFIED_PIGLIN, WITHER_SKELETON, DROWNED -> true;
            default -> false;
        };
    }

    // Removed private helper methods: applyVampireEffects, applyBloodlustMode, applyNightVisionMode
    // Their logic should be integrated into VampireManager's state-changing methods.

} 