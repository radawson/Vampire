package org.clockworx.vampire.entity;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.permissions.PermissionAttachment;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.ResourceUtil;
import org.clockworx.vampire.database.DatabaseManager;
import org.clockworx.vampire.event.EventVampirePlayerBloodChange;
import org.clockworx.vampire.event.EventVampirePlayerInfectionChange;
import org.clockworx.vampire.event.EventVampirePlayerModeChange;
import org.clockworx.vampire.event.EventVampirePlayerShriek;
import org.clockworx.vampire.event.EventVampirePlayerVampireChange;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a player's vampire data.
 * This class should not directly interact with the database.
 * All database operations should go through the DatabaseManager interface.
 */
public class VampirePlayer {
    
    private final UUID uuid;
    private final String name;
    private boolean isVampire;
    private double infectionLevel;
    private String infectionReason;
    private long infectionTime;
    private double blood;
    private long lastShriekTime;
    private long lastBloodTradeTime;
    private UUID lastBloodTradePartner;
    private double lastBloodTradeAmount;
    private String lastBloodTradeType;
    private boolean bloodlustMode;
    private boolean nightVisionMode;
    private boolean intentMode;
    private long lastBloodlustTime;
    private UUID makerId;
    private boolean intending;
    private boolean bloodlusting;
    private boolean usingNightVision;
    private double temperature;
    private double radiation;
    private long lastDamageTime;
    private long lastShriekWaitMessageTime;
    private long truceBreakTimeLeft;
    private VampirePlayer tradeOfferedFrom;
    private double tradeOfferedAmount;
    private long tradeOfferedAtTime;
    private PermissionAttachment permissionAttachment;
    private double bloodLevel;
    private String mode;
    private long lastBloodRegen;
    private long lastInfectionUpdate;
    private long lastModeChange;
    private String lastInfectionReason;
    private int vampireLevel;
    private boolean wasInfected;
    private boolean wasVampire;
    
    /**
     * Creates a new VampirePlayer with default values.
     * 
     * @param uuid The player's UUID
     * @param name The player's name
     */
    public VampirePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.isVampire = false;
        this.infectionLevel = 0.0;
        this.blood = 0.0;
        this.lastShriekTime = 0;
        this.lastBloodTradeTime = 0;
        this.bloodlustMode = false;
        this.nightVisionMode = false;
        this.intentMode = false;
        this.lastBloodlustTime = 0;
        this.makerId = null;
        this.intending = false;
        this.bloodlusting = false;
        this.usingNightVision = false;
        this.temperature = 0.0;
        this.radiation = 0.0;
        this.lastDamageTime = 0;
        this.lastShriekWaitMessageTime = 0;
        this.truceBreakTimeLeft = 0;
        this.tradeOfferedFrom = null;
        this.tradeOfferedAmount = 0.0;
        this.tradeOfferedAtTime = 0;
        this.permissionAttachment = null;
        this.bloodLevel = 0.0;
        this.mode = "disabled";
        this.lastBloodRegen = System.currentTimeMillis();
        this.lastInfectionUpdate = System.currentTimeMillis();
        this.lastModeChange = System.currentTimeMillis();
        this.lastInfectionReason = null;
    }
    
    /**
     * Gets the player's UUID.
     * 
     * @return The player's UUID
     */
    public UUID getUuid() {
        return uuid;
    }
    
    /**
     * Gets the player's name.
     * 
     * @return The player's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the Bukkit Player object if the player is online.
     * 
     * @return The Bukkit Player object, or null if the player is offline
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
    
    /**
     * Checks if the player is online.
     * 
     * @return true if the player is online
     */
    public boolean isOnline() {
        return getPlayer() != null;
    }
    
    /**
     * Checks if the player is a vampire.
     * 
     * @return true if the player is a vampire
     */
    public boolean isVampire() {
        return isVampire;
    }
    
    /**
     * Sets whether the player is a vampire.
     * 
     * @param vampire true if the player is a vampire
     */
    public void setVampire(boolean vampire) {
        if (this.isVampire != vampire) {
            this.isVampire = vampire;
        updatePermissions();
        updatePotionEffects();
            
            // Fire event
            VampirePlugin.getInstance().getServer().getPluginManager()
                .callEvent(new EventVampirePlayerVampireChange(vampire, this));
        }
    }
    
    /**
     * Checks if the player is a human (not a vampire and not infected).
     * 
     * @return true if the player is a human
     */
    public boolean isHuman() {
        return !isVampire() && !isInfected();
    }
    
    /**
     * Checks if the player is infected.
     * 
     * @return true if the player is infected
     */
    public boolean isInfected() {
        return infectionLevel > 0.0;
    }

    
    /**
     * Gets the player's infection level.
     * 
     * @return The player's infection level (0.0 to 1.0)
     */
    public double getInfectionLevel() {
        return infectionLevel;
    }
    
    /**
     * Sets the player's infection level.
     * 
     * @param infection The player's infection level (0.0 to 1.0)
     */
    public void setInfectionLevel(double infection) {
        double oldInfection = this.infectionLevel;
        this.infectionLevel = Math.max(0.0, Math.min(1.0, infection));
        updatePotionEffects();
        
        // Fire event if infection changed significantly
        if (Math.abs(oldInfection - this.infectionLevel) > 0.01) {
            VampirePlugin.getInstance().getServer().getPluginManager()
                .callEvent(new EventVampirePlayerInfectionChange(this.infectionLevel, this));
        }
    }
    
    /**
     * Gets the reason for the player's infection.
     * 
     * @return The reason for the player's infection
     */
    public String getInfectionReason() {
        return infectionReason;
    }
    
    /**
     * Sets the reason for the player's infection.
     * 
     * @param reason The reason for the player's infection
     */
    public void setInfectionReason(String reason) {
        infectionReason = reason;
    }

    /**
     * Gets the ID of the player who infected the player.
     * 
     * @return The ID of the player who infected the player
     */
    public UUID getMakerId() {
        return makerId;
    }

    /**
     * Sets the ID of the player who infected the player.
     * 
     * @param makerId The ID of the player who infected the player
     */
    public void setMakerId(UUID makerId) {
        this.makerId = makerId;
    }
    
    /**
     * Gets the time when the player was infected.
     * 
     * @return The time when the player was infected
     */
    public long getInfectionTime() {
        return infectionTime;
    }
    
    /**
     * Sets the time when the player was infected.
     * 
     * @param time The time when the player was infected
     */
    public void setInfectionTime(long time) {
        infectionTime = time;
    }
    
    /**
     * Gets the player's blood level.
     * 
     * Blood level is the amount of blood in a vampire's body. 
     * 
     * @return The player's blood level (0.0 to 10.0)
     */
    public double getBlood() {
        return blood;
    }
    
    /**
     * Sets the player's blood level.
     * 
     * Blood level is the amount of blood in a vampire's body. 
     * @param blood The player's blood level (0.0 to 10.0)
     */
    public void setBlood(double blood) {
        double oldBlood = this.blood;
        this.blood = Math.max(0.0, Math.min(10.0, blood));
        
        // Fire event if blood level changed significantly
        if (Math.abs(oldBlood - this.blood) > 0.5) {
            VampirePlugin.getInstance().getServer().getPluginManager()
                .callEvent(new EventVampirePlayerBloodChange(this.blood, this));
        }
    }
    
    /**
     * Gets the time of the player's last shriek.
     * 
     * @return The time of the player's last shriek
     */
    public long getLastShriekTime() {
        return lastShriekTime;
    }
    
    /**
     * Sets the time of the player's last shriek.
     * 
     * @param time The time of the player's last shriek
     */
    public void setLastShriekTime(long time) {
        lastShriekTime = time;
    }
    
    /**
     * Gets the time of the player's last blood trade.
     * 
     * @return The time of the player's last blood trade
     */
    public long getLastBloodTradeTime() {
        return lastBloodTradeTime;
    }
    
    /**
     * Sets the time of the player's last blood trade.
     * 
     * @param time The time of the player's last blood trade
     */
    public void setLastBloodTradeTime(long time) {
        lastBloodTradeTime = time;
    }
    
    /**
     * Gets the UUID of the player who last traded blood with this player.
     * 
     * @return The UUID of the player who last traded blood with this player, or null if none
     */
    public UUID getLastBloodTradePartner() {
        return lastBloodTradePartner;
    }
    
    /**
     * Sets the UUID of the player who last traded blood with this player.
     * 
     * @param partner The UUID of the player who last traded blood with this player, or null if none
     */
    public void setLastBloodTradePartner(UUID partner) {
        lastBloodTradePartner = partner;
    }
    
    /**
     * Gets the amount of blood traded with the last blood trade partner.
     * 
     * @return The amount of blood traded with the last blood trade partner
     */
    public double getLastBloodTradeAmount() {
        return lastBloodTradeAmount;
    }
    
    /**
     * Sets the amount of blood traded with the last blood trade partner.
     * 
     * @param amount The amount of blood traded with the last blood trade partner
     */
    public void setLastBloodTradeAmount(double amount) {
        lastBloodTradeAmount = amount;
    }
    
    /**
     * Gets the type of the last blood trade.
     * 
     * @return The type of the last blood trade
     */
    public String getLastBloodTradeType() {
        return lastBloodTradeType;
    }
    
    /**
     * Sets the type of the last blood trade.
     * 
     * @param type The type of the last blood trade
     */
    public void setLastBloodTradeType(String type) {
        lastBloodTradeType = type;
    }
    
    /**
     * Checks if the player is in bloodlust mode.
     * 
     * @return true if the player is in bloodlust mode
     */
    public boolean isBloodlustMode() {
        return bloodlustMode;
    }
    
    /**
     * Sets whether the player is in bloodlust mode.
     * 
     * @param mode true if the player is in bloodlust mode
     */
    public void setBloodlustMode(boolean mode) {
        bloodlustMode = mode;
    }
    
    /**
     * Checks if the player is in night vision mode.
     * 
     * @return true if the player is in night vision mode
     */
    public boolean isNightVisionMode() {
        return nightVisionMode;
    }
    
    /**
     * Sets whether the player is in night vision mode.
     * 
     * @param mode true if the player is in night vision mode
     */
    public void setNightVisionMode(boolean mode) {
        nightVisionMode = mode;
    }
    
    /**
     * Checks if the player is in intent mode.
     * 
     * @return true if the player is in intent mode
     */
    public boolean isIntentMode() {
        return intentMode;
    }
    
    /**
     * Sets whether the player is in intent mode.
     * 
     * @param mode true if the player is in intent mode
     */
    public void setIntentMode(boolean mode) {
        intentMode = mode;
    }
    
    /**
     * Gets the time when the player last entered bloodlust mode.
     * 
     * @return The time when the player last entered bloodlust mode
     */
    public long getLastBloodlustTime() {
        return lastBloodlustTime;
    }
    
    /**
     * Sets the time when the player last entered bloodlust mode.
     * 
     * @param time The time when the player last entered bloodlust mode
     */
    public void setLastBloodlustTime(long time) {
        lastBloodlustTime = time;
    }
    
    /**
     * Gets the player's temperature.
     * 
     * Temperature is a value between 0.0 and 1.0 that represents the player's temperature caused by sun exposure
     * 
     * @return The player's temperature (0.0 to 1.0)
     */
    public double getTemperature() {
        return temperature;
    }
    
    /**
     * Sets the player's temperature.
     * 
     * Temperature is a value between 0.0 and 1.0 that represents the player's temperature caused by sun exposure
     * 
     * @param temperature The player's temperature (0.0 to 1.0)
     */
    public void setTemperature(double temperature) {
        this.temperature = Math.max(0.0, Math.min(1.0, temperature));
    }
    
    /**
     * Gets the player's radiation level.
     * 
     * Radiation is a value between 0.0 and 1.0 that represents the player's radiation level caused by sun exposure
     * 
     * @return The player's radiation level (0.0 to 1.0)
     */
    public double getRadiation() {
        return radiation;
    }
    
    /**
     * Sets the player's radiation level.
     *  
     * Radiation is a value between 0.0 and 1.0 that represents the player's radiation level caused by sun exposure
     * 
     * @param radiation The player's radiation level (0.0 to 1.0)
     */
    public void setRadiation(double radiation) {
        this.radiation = Math.max(0.0, Math.min(1.0, radiation));
    }
    
    /**
     * Gets the time of the player's last damage.
     * 
     * @return The time of the player's last damage
     */
    public long getLastDamageTime() {
        return lastDamageTime;
    }
    
    /**
     * Sets the time of the player's last damage.
     * 
     * @param lastDamageTime The time of the player's last damage
     */
    public void setLastDamageTime(long lastDamageTime) {
        this.lastDamageTime = lastDamageTime;
    }
    
    /**
     * Gets the time of the player's last shriek wait message.
     * 
     * @return The time of the player's last shriek wait message
     */
    public long getLastShriekWaitMessageTime() {
        return lastShriekWaitMessageTime;
    }
    
    /**
     * Sets the time of the player's last shriek wait message.
     * 
     * @param lastShriekWaitMessageTime The time of the player's last shriek wait message
     */
    public void setLastShriekWaitMessageTime(long lastShriekWaitMessageTime) {
        this.lastShriekWaitMessageTime = lastShriekWaitMessageTime;
    }
    
    /**
     * Gets the time left until the player's truce is restored.
     * 
     * @return The time left until the player's truce is restored
     */
    public long getTruceBreakTimeLeft() {
        return truceBreakTimeLeft;
    }
    
    /**
     * Sets the time left until the player's truce is restored.
     * 
     * @param truceBreakTimeLeft The time left until the player's truce is restored
     */
    public void setTruceBreakTimeLeft(long truceBreakTimeLeft) {
        this.truceBreakTimeLeft = truceBreakTimeLeft;
    }
    
    /**
     * Gets the player who offered a trade to this player.
     * 
     * @return The player who offered a trade to this player, or null if none
     */
    public VampirePlayer getTradeOfferedFrom() {
        return tradeOfferedFrom;
    }
    
    /**
     * Sets the player who offered a trade to this player.
     * 
     * @param tradeOfferedFrom The player who offered a trade to this player, or null if none
     */
    public void setTradeOfferedFrom(VampirePlayer tradeOfferedFrom) {
        this.tradeOfferedFrom = tradeOfferedFrom;
    }
    
    /**
     * Gets the amount offered in a trade.
     * 
     * @return The amount offered in a trade
     */
    public double getTradeOfferedAmount() {
        return tradeOfferedAmount;
    }
    
    /**
     * Sets the amount offered in a trade.
     * 
     * @param tradeOfferedAmount The amount offered in a trade
     */
    public void setTradeOfferedAmount(double tradeOfferedAmount) {
        this.tradeOfferedAmount = tradeOfferedAmount;
    }
    
    /**
     * Gets the time when a trade was offered.
     * 
     * @return The time when a trade was offered
     */
    public long getTradeOfferedAtTime() {
        return tradeOfferedAtTime;
    }
    
    /**
     * Sets the time when a trade was offered.
     * 
     * @param tradeOfferedAtTime The time when a trade was offered
     */
    public void setTradeOfferedAtTime(long tradeOfferedAtTime) {
        this.tradeOfferedAtTime = tradeOfferedAtTime;
    }
    
    /**
     * Gets the player's permission attachment.
     * 
     * @return The player's permission attachment
     */
    public PermissionAttachment getPermissionAttachment() {
        return permissionAttachment;
    }
    
    /**
     * Sets the player's permission attachment.
     * 
     * @param permissionAttachment The player's permission attachment
     */
    public void setPermissionAttachment(PermissionAttachment permissionAttachment) {
        this.permissionAttachment = permissionAttachment;
    }
    
    /**
     * Updates the player's permissions based on their vampire status.
     */
    public void updatePermissions() {
        Player player = getPlayer();
        if (player == null) {
            return;
        }
        
        // Remove old permission attachment if it exists
        if (permissionAttachment != null) {
            player.removeAttachment(permissionAttachment);
            permissionAttachment = null;
        }
        
        // Create new permission attachment
        permissionAttachment = player.addAttachment(VampirePlugin.getInstance());
        
        // Set permissions based on vampire status
        if (isVampire()) {
            permissionAttachment.setPermission("vampire.is.vampire", true);
            permissionAttachment.setPermission("vampire.is.human", false);
        } else {
            permissionAttachment.setPermission("vampire.is.vampire", false);
            permissionAttachment.setPermission("vampire.is.human", true);
        }
    }
    
    /**
     * Updates the player's potion effects based on their vampire status.
     */
    public void updatePotionEffects() {
        Player player = getPlayer();
        if (player == null) {
            return;
        }
        
        // Apply effects based on vampire status
        if (isVampire()) {
            // Vampire effects
            player.addPotionEffect(VampirePlugin.getInstance().getVampireConfig().getVampireEffect());
            
            // Bloodlust effects
            if (isBloodlusting()) {
                player.addPotionEffect(VampirePlugin.getInstance().getVampireConfig().getBloodlustEffect());
            } else {
                player.removePotionEffect(VampirePlugin.getInstance().getVampireConfig().getBloodlustEffect().getType());
            }
            
            // Night vision effects
            if (isUsingNightVision()) {
                player.addPotionEffect(VampirePlugin.getInstance().getVampireConfig().getNightVisionEffect());
            } else {
                player.removePotionEffect(VampirePlugin.getInstance().getVampireConfig().getNightVisionEffect().getType());
            }
        } else if (isInfected()) {
            // Infected effects
            player.addPotionEffect(VampirePlugin.getInstance().getVampireConfig().getInfectedEffect());
        } else {
            // Human effects
            player.addPotionEffect(VampirePlugin.getInstance().getVampireConfig().getHumanEffect());
        }
    }
    
    /**
     * Performs a shriek action.
     */
    public void shriek() {
        Player player = getPlayer();
        if (player == null) {
            return;
        }
        
        long now = System.currentTimeMillis();
        long cooldown = VampirePlugin.getInstance().getVampireConfig().getShriekCooldown();
        
        if (now - lastShriekTime < cooldown) {
            long waitTime = (cooldown - (now - lastShriekTime)) / 1000;
            player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage(
                "shriek.wait", 
                String.valueOf(waitTime)
            ));
            return;
        }
        
        // Perform shriek
        lastShriekTime = now;
        
        // Apply effects
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.getWorld().playSound(player.getLocation(), VampirePlugin.getInstance().getVampireConfig().getShriekSound(), 1.0f, 1.0f);
        
        // Send message
        player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage("shriek.perform"));
    }
    
    /**
     * Accepts a trade offer.
     */
    public void acceptTrade() {
        if (tradeOfferedFrom == null) {
            Player player = getPlayer();
            if (player != null) {
                player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage("trade.none"));
            }
            return;
        }
        
        // Check if the trade is still valid
        long now = System.currentTimeMillis();
        long tolerance = VampirePlugin.getInstance().getVampireConfig().getTradeOfferTolerance();
        
        if (now - tradeOfferedAtTime > tolerance) {
            Player player = getPlayer();
            if (player != null) {
                player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage("trade.expired"));
            }
            tradeOfferedFrom = null;
            return;
        }
        
        // Check if the players are close enough
        Player player = getPlayer();
        Player maker = tradeOfferedFrom.getPlayer();
        
        if (player == null || maker == null) {
            return;
        }
        
        double maxDistance = VampirePlugin.getInstance().getVampireConfig().getTradeOfferMaxDistance();
        
        if (player.getLocation().distance(maker.getLocation()) > maxDistance) {
            player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage(
                "trade.distance", 
                maker.getName()
            ));
            return;
        }
        
        // Check if the maker has enough blood
        if (tradeOfferedFrom.getBlood() < tradeOfferedAmount) {
            player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage(
                "trade.insufficient", 
                maker.getName()
            ));
            return;
        }
        
        // Transfer blood
        tradeOfferedFrom.setBlood(tradeOfferedFrom.getBlood() - tradeOfferedAmount);
        setBlood(getBlood() + tradeOfferedAmount);
        
        // Send messages
        player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage(
            "trade.accept", 
            String.format("%.1f", tradeOfferedAmount),
            maker.getName()
        ));
        
        maker.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage(
            "trade.transfer", 
            player.getName(),
            String.format("%.1f", tradeOfferedAmount)
        ));
        
        // Reset trade offer
        tradeOfferedFrom = null;
    }
    
    /**
     * Offers a trade to another player.
     * 
     * @param target The player to offer the trade to
     * @param amount The amount of blood to offer
     */
    public void offerTrade(VampirePlayer target, double amount) {
        Player player = getPlayer();
        Player targetPlayer = target.getPlayer();
        
        if (player == null || targetPlayer == null) {
            return;
        }
        
        // Check if the players are close enough
        double maxDistance = VampirePlugin.getInstance().getVampireConfig().getTradeOfferMaxDistance();
        
        if (player.getLocation().distance(targetPlayer.getLocation()) > maxDistance) {
            player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage(
                "trade.distance", 
                target.getName()
            ));
            return;
        }
        
        // Check if the player has enough blood
        if (getBlood() < amount) {
            player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage("trade.insufficient.self"));
            return;
        }
        
        // Set up the trade offer
        target.setTradeOfferedFrom(this);
        target.setTradeOfferedAmount(amount);
        target.setTradeOfferedAtTime(System.currentTimeMillis());
        
        // Send messages
        player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage(
            "trade.offer.self", 
            String.format("%.1f", amount),
            target.getName()
        ));
        
        targetPlayer.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage(
            "trade.offer.target", 
            player.getName(),
            String.format("%.1f", amount)
        ));
        
        targetPlayer.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage(
            "trade.accept.help", 
            "/vampire accept"
        ));
    }
    
    /**
     * Checks if the player is within a certain distance of another player.
     * 
     * @param other The other player
     * @param maxDistance The maximum distance
     * @return true if the players are within the maximum distance
     */
    public boolean isWithinDistanceOf(VampirePlayer other, double maxDistance) {
        Player player = getPlayer();
        Player otherPlayer = other.getPlayer();
        
        if (player == null || otherPlayer == null) {
            return false;
        }
        
        return player.getLocation().distance(otherPlayer.getLocation()) <= maxDistance;
    }
    
    /**
     * Updates the player's truce status.
     * 
     * @param millis The number of milliseconds since the last update
     */
    public void updateTruce(long millis) {
        if (truceBreakTimeLeft > 0) {
            truceBreakTimeLeft = Math.max(0, truceBreakTimeLeft - millis);
            
            if (truceBreakTimeLeft == 0) {
                truceRestore();
            }
        }
    }
    
    /**
     * Checks if the player's truce is broken.
     * 
     * @return true if the player's truce is broken
     */
    public boolean isTruceBroken() {
        return truceBreakTimeLeft > 0;
    }
    
    /**
     * Breaks the player's truce.
     */
    public void truceBreak() {
        Player player = getPlayer();
        if (player == null) {
            return;
        }
        
        truceBreakTimeLeft = VampirePlugin.getInstance().getVampireConfig().getTruceBreakTime();
        
        player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage("truce.broken"));
    }
    
    /**
     * Restores the player's truce.
     */
    public void truceRestore() {
        Player player = getPlayer();
        if (player == null) {
            return;
        }
        
        player.sendMessage(VampirePlugin.getInstance().getLanguageConfig().getMessage("truce.restored"));
    }
    
    /**
     * Gets the player's combat damage factor.
     * 
     * @return The player's combat damage factor
     */
    public double getCombatDamageFactor() {
        if (isBloodlusting()) {
            return VampirePlugin.getInstance().getVampireConfig().getCombatDamageFactorWithBloodlust();
        } else {
            return VampirePlugin.getInstance().getVampireConfig().getCombatDamageFactorWithoutBloodlust();
        }
    }
    
    /**
     * Gets the player's combat infection risk.
     * 
     * @return The player's combat infection risk
     */
    public double getCombatInfectRisk() {
        if (isIntending()) {
            return VampirePlugin.getInstance().getVampireConfig().getCombatInfectRiskWithIntent();
        } else {
            return VampirePlugin.getInstance().getVampireConfig().getCombatInfectRiskWithoutIntent();
        }
    }
    
    /**
     * Checks if the player is bloodlusting.
     * 
     * @return true if the player is bloodlusting
     */
    public boolean isBloodlusting() {
        return bloodlusting;
    }
    
    /**
     * Sets whether the player is bloodlusting.
     * 
     * @param bloodlusting true if the player is bloodlusting
     */
    public void setBloodlusting(boolean bloodlusting) {
        this.bloodlusting = bloodlusting;
        updatePotionEffects();
    }
    
    /**
     * Gets a message describing the player's bloodlust status.
     * 
     * @return A message describing the player's bloodlust status
     */
    public String getBloodlustMessage() {
        return VampirePlugin.getInstance().getLanguageConfig().getMessage(
            "bloodlust.status", 
            isBloodlusting() ? "ON" : "OFF",
            String.format("%.1f%%", getCombatDamageFactor() * 100)
        );
    }
    
    /**
     * Checks if the player is using night vision.
     * 
     * @return true if the player is using night vision
     */
    public boolean isUsingNightVision() {
        return usingNightVision;
    }
    
    /**
     * Sets whether the player is using night vision.
     * 
     * @param usingNightVision true if the player is using night vision
     */
    public void setUsingNightVision(boolean usingNightVision) {
        this.usingNightVision = usingNightVision;
        updatePotionEffects();
    }
    
    /**
     * Gets a message describing the player's night vision status.
     * 
     * @return A message describing the player's night vision status
     */
    public String getNightVisionMessage() {
        return VampirePlugin.getInstance().getLanguageConfig().getMessage(
            "nightvision.status", 
            isUsingNightVision() ? "ON" : "OFF"
        );
    }
    
    /**
     * Checks if the player is intending to infect others.
     * 
     * @return true if the player is intending to infect others
     */
    public boolean isIntending() {
        return intending;
    }
    
    /**
     * Sets whether the player is intending to infect others.
     * 
     * @param intending true if the player is intending to infect others
     */
    public void setIntending(boolean intending) {
        this.intending = intending;
    }
    
    /**
     * Gets a message describing the player's infection intent.
     * 
     * @return A message describing the player's infection intent
     */
    public String getIntendMessage() {
        return VampirePlugin.getInstance().getLanguageConfig().getMessage(
            "intend.status", 
            isIntending() ? "ON" : "OFF",
            String.format("%.1f%%", getCombatInfectRisk() * 100)
        );
    }
    
    /**
     * Sends a message to the player.
     * 
     * @param message The message to send
     */
    public void sendMessage(String message) {
        Player player = getPlayer();
        if (player != null && player.isOnline()) {
            player.sendMessage(message);
        }
    }
    
    /**
     * Sends a message to the player.
     * This is an alias for sendMessage for compatibility with existing code.
     * 
     * @param message The message to send
     */
    public void msg(String message) {
        sendMessage(message);
    }
    
    /**
     * Saves the player's data to the database.
     * 
     * @return A CompletableFuture that will complete when the save is done
     */
    public CompletableFuture<Void> save() {
        return VampirePlugin.getInstance().saveVampirePlayer(this);
    }

    public double getBloodLevel() {
        return bloodLevel;
    }

    public void setBloodLevel(double bloodLevel) {
        double oldBloodLevel = this.bloodLevel;
        this.bloodLevel = bloodLevel;
        
        // Fire event if blood level changed significantly
        if (Math.abs(oldBloodLevel - this.bloodLevel) > 0.01) {
            VampirePlugin.getInstance().getServer().getPluginManager()
                .callEvent(new EventVampirePlayerBloodChange(this.bloodLevel, this));
        }
    }

    public double getInfection() {
        return infectionLevel;
    }

    public void setInfection(double infection) {
        double oldInfection = this.infectionLevel;
        this.infectionLevel = infection;
        
        // Fire event if infection changed significantly
        if (Math.abs(oldInfection - this.infectionLevel) > 0.01) {
            VampirePlugin.getInstance().getServer().getPluginManager()
                .callEvent(new EventVampirePlayerInfectionChange(this.infectionLevel, this));
        }
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
        this.lastModeChange = System.currentTimeMillis();
    }

    public long getLastBloodRegen() {
        return lastBloodRegen;
    }

    public void setLastBloodRegen(long lastBloodRegen) {
        this.lastBloodRegen = lastBloodRegen;
    }

    public long getLastInfectionUpdate() {
        return lastInfectionUpdate;
    }

    public void setLastInfectionUpdate(long lastInfectionUpdate) {
        this.lastInfectionUpdate = lastInfectionUpdate;
    }

    public long getLastModeChange() {
        return lastModeChange;
    }

    public String getLastInfectionReason() {
        return lastInfectionReason;
    }

    public void setLastInfectionReason(String reason) {
        this.lastInfectionReason = reason;
    }

    public void addInfection(double amount, String reason) {
        this.infectionLevel += amount;
        this.lastInfectionUpdate = System.currentTimeMillis();
        this.lastInfectionReason = reason;
    }

    public boolean useBlood(double amount) {
        if (bloodLevel >= amount) {
            bloodLevel -= amount;
            return true;
        }
        return false;
    }

    public void addBlood(double amount) {
        this.bloodLevel += amount;
        this.lastBloodRegen = System.currentTimeMillis();
    }

    public static VampirePlayer get(Player player) {
        return VampirePlugin.getInstance().getVampirePlayer(player.getUniqueId()).join();
    }

    public void setLastModeChange(long lastModeChange) {
        this.lastModeChange = lastModeChange;
    }

    public boolean wasInfected() {
        return wasInfected;
    }

    public boolean wasVampire() {
        return wasVampire;
    }
} 