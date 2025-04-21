package org.clockworx.vampire.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.util.VampireMessages;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.Collection;

public class VampireManager {
    private final VampirePlugin plugin;
    // Use ConcurrentHashMap as loading/saving might happen async
    private final Map<UUID, VampirePlayer> onlinePlayers = new ConcurrentHashMap<>(); 

    public VampireManager(VampirePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles player joining the server.
     * Loads their data from the database and caches it.
     */
    public void handlePlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        VampireMessages.debug("Handling join for player: " + playerName + " (" + uuid + ")");

        // Prevent duplicate loading if already somehow cached
        if (onlinePlayers.containsKey(uuid)) {
            VampireMessages.debug("Player " + playerName + " already in cache during join handling.");
            // Optionally update permissions/effects here if needed
            // onlinePlayers.get(uuid).updatePermissions(); // Method undefined on VampirePlayer
            // onlinePlayers.get(uuid).updatePotionEffects(); // Method undefined on VampirePlayer
            return;
        }

        // Load player data from the database asynchronously
        plugin.getDatabaseManager().getPlayer(uuid).thenAcceptAsync(vampirePlayer -> {
            if (vampirePlayer == null) {
                // Player not found in DB, create a new default VampirePlayer object
                VampireMessages.debug("No existing data found for " + playerName + ", creating new entry.");
                vampirePlayer = new VampirePlayer(uuid, playerName);
                // Optionally save the new player entry immediately or wait for first change/quit
                // plugin.getDatabaseManager().savePlayer(vampirePlayer);
            } else {
                VampireMessages.debug("Loaded data for player: " + playerName);
                // Ensure the player name is up-to-date in case of changes
                vampirePlayer.setName(playerName); 
            }

            // Cache the player data
            onlinePlayers.put(uuid, vampirePlayer);
            
            // Update permissions and effects based on loaded data
            // Remove old direct calls on VampirePlayer
            // vampirePlayer.updatePermissions(); 
            // vampirePlayer.updatePotionEffects();
            
            // Trigger manager-level update based on loaded state
            updatePlayerEffects(vampirePlayer);
            // TODO: Add explicit permission update logic if needed

        }).exceptionally(ex -> {
            VampireMessages.error("Failed to load player data for " + playerName, ex);
            // Create a default player object so the player can still join
            VampirePlayer defaultPlayer = new VampirePlayer(uuid, playerName); // Define variable here
            onlinePlayers.put(uuid, defaultPlayer); // Use the defined variable
            return null;
        });
    }

    /**
     * Handles player quitting the server.
     * Saves their data to the database and removes them from the cache.
     */
    public void handlePlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();
        VampireMessages.debug("Handling quit for player: " + playerName + " (" + uuid + ")");

        // Clean up temporary permissions regardless of save success
        VampirePermission.cleanupTemporaryPermissions(player);

        VampirePlayer vampirePlayer = onlinePlayers.remove(uuid); // Remove from cache
        if (vampirePlayer != null) {
            // Save the player data asynchronously
            plugin.getDatabaseManager().savePlayer(vampirePlayer).thenRunAsync(() -> {
                VampireMessages.debug("Successfully saved data for player: " + playerName);
            }).exceptionally(ex -> {
                VampireMessages.error("Failed to save data for player " + playerName + ": " + ex.getMessage(), ex);
                // Maybe try to re-cache or flag for retry?
                return null;
            });
        } else {
            VampireMessages.debug("Player " + playerName + " not found in cache during quit handling.");
        }
    }

    /**
     * Retrieves the cached VampirePlayer data for a given UUID.
     * 
     * @param uuid The UUID of the player.
     * @return The cached VampirePlayer, or null if the player is not online/cached.
     */
    public VampirePlayer getCachedVampirePlayer(UUID uuid) {
        return onlinePlayers.get(uuid);
    }

    /**
     * Returns a view of the currently cached online players.
     * Note: Modifications to the returned collection may not be supported
     * or could lead to issues depending on the underlying map implementation.
     * 
     * @return A Collection of cached VampirePlayer objects.
     */
    public Collection<VampirePlayer> getCachedOnlinePlayers() {
        return onlinePlayers.values(); // Return the values (VampirePlayer objects)
    }

    /**
     * Gets the VampirePlayer data, primarily from cache.
     * This is kept for compatibility with existing calls but ideally
     * callers should use getCachedVampirePlayer for online players.
     * 
     * @deprecated Prefer getCachedVampirePlayer for online players.
     * @param uuid The UUID of the player.
     * @return A CompletableFuture containing the cached VampirePlayer, or null.
     */
    @Deprecated
    public CompletableFuture<VampirePlayer> getVampirePlayer(UUID uuid) {
        return CompletableFuture.completedFuture(onlinePlayers.get(uuid));
    }

    public void shutdown() {
        VampireMessages.debug("Shutting down VampireManager, saving remaining players...");
        // Save all players remaining in the cache (e.g., during server stop)
        for (VampirePlayer vp : onlinePlayers.values()) {
             Player player = vp.getPlayer();
             if (player != null) { // Get player object for permission cleanup
                 VampirePermission.cleanupTemporaryPermissions(player);
             }
             plugin.getDatabaseManager().savePlayer(vp); // Consider doing this synchronously on shutdown
        }
        onlinePlayers.clear();
        VampireMessages.debug("VampireManager shutdown complete.");
    }
    
    public void regenerateBlood() {
        // Example: Iterate online players and increase blood
        onlinePlayers.values().stream()
            .filter(VampirePlayer::isVampire)
            .forEach(vp -> {
                // TODO: Get regen rate from config
                // vp.addBlood(regenAmount);
            });
    }
    
    public void handleDaylightDamage() {
        // This logic is likely handled by VampireTask now
        // VampireTask iterates online players directly
    }

    // --- Methods for Modifying Player State ---

    /**
     * Sets a player's vampire status.
     * 
     * @param uuid The player's UUID.
     * @param isVampire True to make vampire, false to cure.
     * @param reason Optional reason for the change (for logging/messaging).
     */
    public void setVampireStatus(UUID uuid, boolean isVampire, String reason) {
        VampirePlayer vp = getCachedVampirePlayer(uuid);
        if (vp != null) {
            boolean changed = vp.isVampire() != isVampire;
            if (changed) {
                // Call the internal setter on the POJO
                vp.setVampireInternal(isVampire); 
                // TODO: Update permissions (using VampirePermission)
                // TODO: Update potion effects (apply/remove appropriate effects)
                
                Player player = vp.getPlayer();
                if (player != null) {
                    VampireMessages.sendLocalized(player, isVampire ? "vampire.become" : "vampire.cure", reason != null ? reason : "Unknown");
                }
                VampireMessages.debug("Set player " + vp.getName() + " vampire status to " + isVampire);
                // TODO: Fire EventVampirePlayerVampireChange
                // Save change to DB
                plugin.getDatabaseManager().savePlayer(vp);
            } else {
                VampireMessages.debug("Player " + vp.getName() + " already has vampire status " + isVampire);
            }
        } else {
            VampireMessages.debug("Cannot set vampire status for offline/uncached player: " + uuid);
        }
    }

    /**
     * Calculates the effective maximum blood for a player based on config and level.
     * 
     * @param vp The VampirePlayer.
     * @return The calculated maximum blood capacity.
     */
    public double getEffectiveMaxBlood(VampirePlayer vp) {
        double baseMax = plugin.getVampireConfig().getMaxBlood();
        double bonusPerLevel = plugin.getVampireConfig().getMaxBloodBonusPerLevel();
        int level = vp.getVampireLevel();
        // Ensure level is not negative if somehow set incorrectly
        return baseMax + (Math.max(0, level) * bonusPerLevel);
    }

    /**
     * Sets a player's blood level, clamping between 0 and their effective max blood.
     * 
     * @param uuid The player's UUID.
     * @param bloodLevel The new blood level.
     */
    public void setBloodLevel(UUID uuid, double bloodLevel) {
        VampirePlayer vp = getCachedVampirePlayer(uuid);
        if (vp != null) {
            // Calculate max blood based on player's level
            double maxBlood = getEffectiveMaxBlood(vp); 
            double oldBlood = vp.getBlood();
            // Clamp between 0 and effective max
            double newBlood = Math.max(0.0, Math.min(maxBlood, bloodLevel)); 
            
            if (Math.abs(oldBlood - newBlood) > 0.01) { 
                vp.setBloodInternal(newBlood);
                VampireMessages.debug("Set player " + vp.getName() + " blood level to " + vp.getBlood() + " / " + maxBlood);
                // TODO: Fire EventVampirePlayerBloodChange?
                // TODO: Decide on save strategy 
            }
        } else {
            VampireMessages.debug("Cannot set blood level for offline/uncached player: " + uuid);
        }
    }

    /**
     * Adds a specified amount to a player's blood level.
     * Uses setBloodLevel to handle clamping and side effects.
     * 
     * @param uuid The player's UUID.
     * @param amount The amount of blood to add (can be negative).
     */
    public void addBlood(UUID uuid, double amount) {
        VampirePlayer vp = getCachedVampirePlayer(uuid);
        if (vp != null) {
            setBloodLevel(uuid, vp.getBlood() + amount); 
        } else {
             VampireMessages.debug("Cannot add blood for offline/uncached player: " + uuid);
        }
    }

     /**
     * Sets a player's infection level.
     * 
     * @param uuid The player's UUID.
     * @param infectionLevel The new infection level.
     * @param reason Optional reason for infection change.
     */
    public void setInfectionLevel(UUID uuid, double infectionLevel, String reason) {
        VampirePlayer vp = getCachedVampirePlayer(uuid);
        if (vp != null) {
             if (!vp.isVampire()) { // Don't infect vampires
                double oldInfection = vp.getInfectionLevel();
                // Call the internal setter on the POJO
                vp.setInfectionLevelInternal(infectionLevel); 
                vp.setInfectionReason(reason);

                // Check if changed significantly before logging/saving/event
                if (Math.abs(oldInfection - vp.getInfectionLevel()) > 0.01) {
                    VampireMessages.debug("Set player " + vp.getName() + " infection level to " + vp.getInfectionLevel());
                    // TODO: Update potion effects based on new infection level
                    // TODO: Fire EventVampirePlayerInfectionChange
                    // TODO: Decide save strategy
                    // plugin.getDatabaseManager().savePlayer(vp); 
                }
            } else {
                 VampireMessages.debug("Cannot infect player " + vp.getName() + " because they are already a vampire.");
            }
        } else {
            VampireMessages.debug("Cannot set infection level for offline/uncached player: " + uuid);
        }
    }

    /**
     * Adds a specified amount to a player's infection level.
     * Uses setInfectionLevel to handle clamping and potential side effects.
     * 
     * @param uuid The player's UUID.
     * @param amount The amount of infection to add.
     * @param reason The reason for this infection increase.
     */
    public void addInfection(UUID uuid, double amount, String reason) {
        VampirePlayer vp = getCachedVampirePlayer(uuid);
        if (vp != null) {
            // Delegate to setInfectionLevel
            setInfectionLevel(uuid, vp.getInfectionLevel() + amount, reason);
        } else {
            VampireMessages.debug("Cannot add infection for offline/uncached player: " + uuid);
        }
    }

    /**
     * Attempts to use (subtract) blood from a player.
     * 
     * @param uuid The player's UUID.
     * @param amount The positive amount of blood to use.
     * @return True if the blood was successfully used, false if the player didn't have enough blood.
     */
    public boolean useBlood(UUID uuid, double amount) {
        if (amount <= 0) return true; // Using 0 or negative blood always succeeds
        
        VampirePlayer vp = getCachedVampirePlayer(uuid);
        if (vp != null) {
            if (vp.getBlood() >= amount) {
                // Delegate to setBloodLevel to handle clamping and potential side effects
                setBloodLevel(uuid, vp.getBlood() - amount);
                return true; // Success
            } else {
                return false; // Not enough blood
            }
        } else {
             VampireMessages.debug("Cannot use blood for offline/uncached player: " + uuid);
             return false; // Cannot use blood if player isn't loaded
        }
    }

    // TODO: Add methods for other state changes (modes, etc.)

    /**
     * Sets the bloodlust mode for a player.
     * 
     * @param uuid The player's UUID.
     * @param bloodlusting The desired bloodlust state.
     */
    public void setModeBloodlust(UUID uuid, boolean bloodlusting) {
        VampirePlayer vp = getCachedVampirePlayer(uuid);
        if (vp != null && vp.isVampire()) { // Only vampires can use modes
            boolean changed = vp.isBloodlusting() != bloodlusting;
            if (changed) {
                vp.setBloodlusting(bloodlusting); // Update POJO state
                VampireMessages.debug("Set player " + vp.getName() + " bloodlust mode to " + bloodlusting);
                
                // Apply/Remove Potion Effects directly or trigger an update check
                updatePlayerEffects(vp); // Example: Call a helper to re-apply all relevant effects

                // TODO: Fire EventVampirePlayerModeChange?
                // TODO: Decide save strategy (save on mode change?)
                // plugin.getDatabaseManager().savePlayer(vp);
            } else {
                 VampireMessages.debug("Player " + vp.getName() + " bloodlust mode already " + bloodlusting);
            }
        } else if (vp != null) {
             VampireMessages.debug("Cannot set bloodlust mode for non-vampire: " + vp.getName());
             // Optionally send message to player? VampireMessages.sendLocalized(vp.getPlayer(), "mode.fail.not_vampire");
        } else {
             VampireMessages.debug("Cannot set bloodlust mode for offline/uncached player: " + uuid);
        }
    }

    /**
     * Sets the infection intent mode for a player.
     * 
     * @param uuid The player's UUID.
     * @param intending The desired intent state.
     */
    public void setModeIntent(UUID uuid, boolean intending) {
        VampirePlayer vp = getCachedVampirePlayer(uuid);
        if (vp != null && vp.isVampire()) { // Only vampires can use modes
            boolean changed = vp.isIntending() != intending;
            if (changed) {
                vp.setIntending(intending); // Update POJO state
                VampireMessages.debug("Set player " + vp.getName() + " intent mode to " + intending);
                
                // No direct potion effects for intent mode currently
                // updatePlayerEffects(vp); // Call if intent had effects

                // TODO: Fire EventVampirePlayerModeChange?
                // TODO: Save?
                // plugin.getDatabaseManager().savePlayer(vp);
            } else {
                 VampireMessages.debug("Player " + vp.getName() + " intent mode already " + intending);
            }
        } else if (vp != null) {
             VampireMessages.debug("Cannot set intent mode for non-vampire: " + vp.getName());
        } else {
             VampireMessages.debug("Cannot set intent mode for offline/uncached player: " + uuid);
        }
    }

    /**
     * Helper method to apply/remove relevant potion effects based on player state.
     * (This might grow to handle all managed effects)
     */
    private void updatePlayerEffects(VampirePlayer vp) {
        Player player = vp.getPlayer();
        if (player == null || !player.isValid()) return;

        // Remove potentially conflicting effects first?

        // Apply effects based on state
        if (vp.isVampire()) {
            // Standard vampire passives (maybe handled elsewhere or here)
            
            // Bloodlust
            if (vp.isBloodlusting()) {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, false));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, true, false));
            } else {
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
                player.removePotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH);
            }

            // Night Vision
            if (vp.isUsingNightVision() && plugin.getVampireConfig().isNightVisionEnabled()) {
                 int level = plugin.getVampireConfig().getNightVisionLevel();
                 player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, level - 1, true, false));
            } else {
                 player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
            }
            
            // Remove non-vampire effects
             player.removePotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS); // From low blood/infection
             player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS); // From infection

        } else if (vp.isInfected()) {
             // Infection effects (potentially scaling with level)
             player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 0, true, false));
             if(vp.getInfectionLevel() > 0.8) {
                 player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 0, true, false));
             }
             // Remove vampire effects
             player.removePotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
             player.removePotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH);
             player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
        } else { // Human
            // Ensure all vampire/infection effects are removed
             player.removePotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
             player.removePotionEffect(org.bukkit.potion.PotionEffectType.STRENGTH);
             player.removePotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION);
             player.removePotionEffect(org.bukkit.potion.PotionEffectType.WEAKNESS);
             player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS);
        }
    }

    // Add getter for plugin instance
    public VampirePlugin getPlugin() {
        return plugin;
    }

    /**
     * Sets the night vision mode for a player.
     * 
     * @param uuid The player's UUID.
     * @param nightVision The desired night vision state.
     */
    public void setModeNightvision(UUID uuid, boolean nightVision) {
        VampirePlayer vp = getCachedVampirePlayer(uuid);
        // Check config *before* changing state
        if (!plugin.getVampireConfig().isNightVisionEnabled()) {
            // Optionally send message that it's disabled globally?
            VampireMessages.debug("Attempted to toggle night vision, but it's disabled globally.");
            // Ensure state is off if globally disabled
            if (vp != null && vp.isUsingNightVision()) {
                vp.setUsingNightVision(false);
                updatePlayerEffects(vp);
            }
            return; // Stop processing if globally disabled
        }

        if (vp != null && vp.isVampire()) { // Only vampires can use modes
            boolean changed = vp.isUsingNightVision() != nightVision;
            if (changed) {
                vp.setUsingNightVision(nightVision); // Update POJO state
                VampireMessages.debug("Set player " + vp.getName() + " night vision mode to " + nightVision);
                
                // Trigger effect update
                updatePlayerEffects(vp); 

                // TODO: Fire EventVampirePlayerModeChange?
                // TODO: Save?
                // plugin.getDatabaseManager().savePlayer(vp);
            } else {
                 VampireMessages.debug("Player " + vp.getName() + " night vision mode already " + nightVision);
            }
        } else if (vp != null) {
             VampireMessages.debug("Cannot set night vision mode for non-vampire: " + vp.getName());
        } else {
             VampireMessages.debug("Cannot set night vision mode for offline/uncached player: " + uuid);
        }
    }

    /**
     * Attempts to perform a shriek action for a player.
     * Checks cooldown, applies effects, and updates state.
     * 
     * @param uuid The UUID of the shrieking player.
     * @return True if the shriek was successful, false if on cooldown or player invalid.
     */
    public boolean performShriek(UUID uuid) {
        VampirePlayer vp = getCachedVampirePlayer(uuid);
        Player player = (vp != null) ? vp.getPlayer() : null;

        // Check if player is valid and a vampire
        if (vp == null || player == null || !player.isValid() || !vp.isVampire()) {
            // Don't send error messages here, command should handle sender feedback
            VampireMessages.debug("performShriek failed: Invalid player/state for " + uuid);
            return false;
        }

        // Check Cooldown
        long now = System.currentTimeMillis();
        long cooldownMillis = plugin.getVampireConfig().getShriekCooldown();
        long timeSinceLast = now - vp.getLastShriekTime();

        if (timeSinceLast < cooldownMillis) {
            long waitTimeSeconds = (cooldownMillis - timeSinceLast + 999) / 1000; // Calculate remaining seconds
            VampireMessages.sendLocalized(player, "shriek.wait", String.valueOf(waitTimeSeconds)); // Need lang key
            return false; // On cooldown
        }

        // --- Perform Shriek --- 
        VampireMessages.debug("Player " + player.getName() + " performing shriek.");
        vp.setLastShriekTime(now); // Update timestamp

        // Apply Effects (Sound, Particles, Debuffs)
        // Maybe move this to a separate FxUtil or keep in manager?
        player.getWorld().playSound(player.getLocation(), plugin.getVampireConfig().getShriekSound(), 1.0f, 1.0f);
        player.getWorld().strikeLightningEffect(player.getLocation()); // Optional flashy effect
        player.getWorld().spawnParticle(org.bukkit.Particle.LARGE_SMOKE, player.getLocation(), 50, 1, 1, 1, 0.1);
        
        // Apply effects to nearby players (configurable radius?)
        int radius = 10; // Example radius
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby == player) continue; // Don't affect self
            if (nearby.getLocation().distanceSquared(player.getLocation()) <= radius * radius) {
                // TODO: Check target conditions (e.g., non-vampire? gamemode?)
                nearby.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 0));
                nearby.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, 100, 1));
            }
        }
        
        // TODO: Fire EventVampirePlayerShriek?
        // plugin.getServer().getPluginManager().callEvent(new EventVampirePlayerShriek(vp));
        
        // Save player state (because lastShriekTime changed)
        plugin.getDatabaseManager().savePlayer(vp);

        // Send success message to shrieking player
        VampireMessages.sendLocalized(player, "shriek.perform"); // Need lang key

        return true; // Success
    }

    // --- Gift Offer Logic --- 

    /**
     * Makes an offer for the Dark Gift from a sender to a target.
     * Performs necessary checks (online, distance, state, cost, etc.).
     *
     * @param senderUUID The UUID of the offering vampire.
     * @param targetUUID The UUID of the target human.
     * @return True if the offer was successfully made, false otherwise.
     */
    public boolean makeGiftOffer(UUID senderUUID, UUID targetUUID) {
        VampirePlayer vpSender = getCachedVampirePlayer(senderUUID);
        VampirePlayer vpTarget = getCachedVampirePlayer(targetUUID);
        Player pSender = (vpSender != null) ? vpSender.getPlayer() : null;
        Player pTarget = (vpTarget != null) ? vpTarget.getPlayer() : null;

        // Initial Checks
        if (!plugin.getVampireConfig().isGiftEnabled()) {
            if(pSender != null) VampireMessages.sendLocalized(pSender, "gift.error.disabled");
            return false;
        }
        if (vpSender == null || pSender == null || !pSender.isValid()) {
            VampireMessages.debug("makeGiftOffer: Sender invalid.");
            return false; // Sender invalid
        }
        if (vpTarget == null || pTarget == null || !pTarget.isValid()) {
             VampireMessages.sendLocalized(pSender, "gift.error.target_offline", "unknown"); // Need target name if available
             return false;
        }
        if (senderUUID.equals(targetUUID)) {
            VampireMessages.sendLocalized(pSender, "gift.error.self");
            return false;
        }
        if (!vpSender.isVampire()) {
            VampireMessages.sendLocalized(pSender, "gift.error.sender_not_vampire");
            return false;
        }
        if (vpTarget.isVampire() || vpTarget.isInfected()) {
            VampireMessages.sendLocalized(pSender, "gift.error.target_not_human", pTarget.getName());
            return false;
        }
        if (vpTarget.getPendingGiftOfferUuid() != null) {
             VampireMessages.sendLocalized(pSender, "gift.error.target_has_offer", pTarget.getName());
            return false;
        }
        
        // Distance Check
        double maxDist = plugin.getVampireConfig().getGiftMaxDistance();
        if (pSender.getLocation().distanceSquared(pTarget.getLocation()) > maxDist * maxDist) {
            VampireMessages.sendLocalized(pSender, "gift.error.too_far", pTarget.getName());
            return false;
        }

        // Blood Cost Check
        double bloodCost = plugin.getVampireConfig().getGiftBloodCost();
        if (vpSender.getBlood() < bloodCost) {
            VampireMessages.sendLocalized(pSender, "gift.error.low_blood", String.valueOf(bloodCost));
            return false;
        }

        // All checks passed, make the offer
        vpTarget.setPendingGiftOffer(senderUUID, System.currentTimeMillis());
        VampireMessages.sendLocalized(pSender, "gift.offer.sent", pTarget.getName());
        VampireMessages.sendLocalized(pTarget, "gift.offer.received", pSender.getName());
        // Optionally schedule a task to auto-clear the offer after tolerance? Or check on accept.

        VampireMessages.debug("Gift offer made from " + pSender.getName() + " to " + pTarget.getName());
        return true;
    }

    /**
     * Accepts a pending Dark Gift offer for the target player.
     *
     * @param targetUUID The UUID of the player accepting the offer.
     * @return True if the offer was successfully accepted, false otherwise.
     */
    public boolean acceptGiftOffer(UUID targetUUID) {
        VampirePlayer vpTarget = getCachedVampirePlayer(targetUUID);
        Player pTarget = (vpTarget != null) ? vpTarget.getPlayer() : null;

        // Check target validity and offer existence
        if (vpTarget == null || pTarget == null || !pTarget.isValid()) return false;
        UUID senderUUID = vpTarget.getPendingGiftOfferUuid();
        if (senderUUID == null) {
             VampireMessages.sendLocalized(pTarget, "gift.error.no_offer");
            return false;
        }

        VampirePlayer vpSender = getCachedVampirePlayer(senderUUID);
        Player pSender = (vpSender != null) ? vpSender.getPlayer() : null;

        // Check Offer Expiry
        long offerTime = vpTarget.getPendingGiftOfferTime();
        long toleranceMillis = plugin.getVampireConfig().getGiftOfferToleranceSeconds() * 1000L;
        if (System.currentTimeMillis() - offerTime > toleranceMillis) {
            vpTarget.clearPendingGiftOffer(); // Clear expired offer
            VampireMessages.sendLocalized(pTarget, "gift.error.offer_expired");
            // Notify sender? Maybe not if it just expired silently.
            return false;
        }

        // Check Sender Validity
        if (vpSender == null || pSender == null || !pSender.isValid() || !vpSender.isVampire()) {
            vpTarget.clearPendingGiftOffer();
            VampireMessages.sendLocalized(pTarget, "gift.error.sender_invalid");
            return false;
        }

        // Re-check distance
        double maxDist = plugin.getVampireConfig().getGiftMaxDistance();
        if (pSender.getLocation().distanceSquared(pTarget.getLocation()) > maxDist * maxDist) {
             vpTarget.clearPendingGiftOffer();
             VampireMessages.sendLocalized(pTarget, "gift.error.too_far", pSender.getName());
            return false;
        }

        // Re-check sender blood cost
        double bloodCost = plugin.getVampireConfig().getGiftBloodCost();
        if (vpSender.getBlood() < bloodCost) {
            vpTarget.clearPendingGiftOffer();
            VampireMessages.sendLocalized(pTarget, "gift.error.sender_low_blood", pSender.getName());
            // Notify sender they were too low when target accepted
             VampireMessages.sendLocalized(pSender, "gift.error.accept_fail_low_blood", pTarget.getName());
            return false;
        }

        // --- Accept Offer --- 
        if (useBlood(senderUUID, bloodCost)) { // Use blood first
            String reason = "Accepted the Dark Gift from " + pSender.getName();
            setVampireStatus(targetUUID, true, reason); // Turn the target
            vpTarget.clearPendingGiftOffer(); // Clear the offer state

            VampireMessages.sendLocalized(pTarget, "gift.accept.success_target");
            VampireMessages.sendLocalized(pSender, "gift.accept.success_sender", pTarget.getName());
            // Play effects? FxUtil.playVampireEffect(pTarget);
            VampireMessages.debug("Gift offer accepted: " + pSender.getName() + " turned " + pTarget.getName());
            return true;
        } else {
            // Should not happen if check above passed, but handle defensively
             vpTarget.clearPendingGiftOffer();
             VampireMessages.sendLocalized(pTarget, "gift.error.accept_fail_unknown");
             VampireMessages.sendLocalized(pSender, "gift.error.accept_fail_unknown_sender", pTarget.getName());
             VampireMessages.error("Failed to use blood for gift offer acceptance from " + senderUUID + " despite passing check!", null);
             return false;
        }
    }

    /**
     * Rejects a pending Dark Gift offer for the target player.
     *
     * @param targetUUID The UUID of the player rejecting the offer.
     * @return True if an offer was successfully rejected, false otherwise.
     */
    public boolean rejectGiftOffer(UUID targetUUID) {
         VampirePlayer vpTarget = getCachedVampirePlayer(targetUUID);
         Player pTarget = (vpTarget != null) ? vpTarget.getPlayer() : null;

        if (vpTarget == null || pTarget == null || !pTarget.isValid()) return false;
        UUID senderUUID = vpTarget.getPendingGiftOfferUuid();
        if (senderUUID == null) {
             VampireMessages.sendLocalized(pTarget, "gift.error.no_offer");
            return false;
        }

        vpTarget.clearPendingGiftOffer(); // Clear the offer
        VampireMessages.sendLocalized(pTarget, "gift.reject.success_target");

        // Notify the original sender if they are online
        Player pSender = Bukkit.getPlayer(senderUUID);
        if (pSender != null && pSender.isOnline()) {
            VampireMessages.sendLocalized(pSender, "gift.reject.success_sender", pTarget.getName());
        }
        VampireMessages.debug("Gift offer rejected by " + pTarget.getName() + " from " + senderUUID);
        return true;
    }

} 