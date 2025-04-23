package org.clockworx.vampire.entity;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Represents a player's vampire data (POJO - Plain Old Java Object).
 * This class primarily holds state. Logic involving external services
 * (config, db, events, effects, permissions) is handled by VampireManager.
 */
public class VampirePlayer {
    
    // Core identifiers
    private final UUID uuid;
    private String name;

    // Core Vampire State
    private boolean isVampire;
    private double blood;
    private int vampireLevel;

    // Infection State
    private double infectionLevel;
    private String infectionReason;
    private long infectionTime;
    private UUID makerId;

    // Mode States (simple booleans)
    private boolean bloodlusting;
    private boolean usingNightVision;
    private boolean intending;

    // Timestamps for Cooldowns/Checks
    private long lastShriekTime;
    private long lastBloodlustTime;
    private long lastModeChange;
    /** Timestamp of the last time the player took damage, used for regeneration cooldown. */
    private long lastDamageTime; 

    // Trade State
    private long lastBloodTradeTime;
    private UUID lastBloodTradePartner;
    private double lastBloodTradeAmount;
    private String lastBloodTradeType;
    private UUID tradeOfferedFromUuid;
    private double tradeOfferedAmount;
    private long tradeOfferedAtTime;

    // --- New fields for Gift Offer state ---
    /** UUID of the player who offered the Dark Gift. Null if no pending offer. */
    private UUID pendingGiftOfferUuid;
    /** Timestamp when the pending Dark Gift offer was made. 0 if no pending offer. */
    private long pendingGiftOfferTime;

    // Constructor remains simple
    public VampirePlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.isVampire = false;
        this.blood = 0.0;
        this.infectionLevel = 0.0;
        this.vampireLevel = 0;
        this.infectionReason = null;
        this.infectionTime = 0L;
        this.makerId = null;
        this.bloodlusting = false;
        this.usingNightVision = false;
        this.intending = false;
        this.lastShriekTime = 0L;
        this.lastBloodlustTime = 0L;
        this.lastModeChange = 0L;
        this.lastDamageTime = 0L;
        this.lastBloodTradeTime = 0L;
        this.lastBloodTradePartner = null;
        this.lastBloodTradeAmount = 0.0;
        this.lastBloodTradeType = null;
        this.tradeOfferedFromUuid = null;
        this.tradeOfferedAmount = 0.0;
        this.tradeOfferedAtTime = 0L;
        // Initialize new fields
        this.pendingGiftOfferUuid = null;
        this.pendingGiftOfferTime = 0L;
    }
    
    // --- Core Getters --- 

    public UUID getUuid() {
        return uuid;
    }
    
    public String getName() {
        return name;
    }
    
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
    
    public boolean isOnline() {
        return getPlayer() != null;
    }
    
    // --- Core Setters --- 

    public void setName(String name) {
        this.name = name;
    }

    // --- Vampire State --- 

    public boolean isVampire() {
        return isVampire;
    }

    /**
     * Internal method to set vampire state. 
     * Effects, permissions, events are handled by VampireManager.
     */
    public void setVampireInternal(boolean vampire) {
        if (this.isVampire != vampire) {
            this.isVampire = vampire;
            if (vampire) {
                this.infectionLevel = 0.0;
                this.infectionReason = null;
            } else {
                this.blood = 0.0;
                this.bloodlusting = false;
                this.usingNightVision = false;
                this.intending = false;
            }
        }
    }

    public double getBlood() {
        return blood;
    }

    /**
     * Internal method to set blood level. Clamping applied.
     * Low blood effects handled by VampireTask/VampireManager.
     */
    public void setBloodInternal(double blood) {
        this.blood = Math.max(0.0, blood);
    }
    
    public int getVampireLevel() {
        return vampireLevel;
    }

    /**
     * Sets the player's vampire level.
     * Should be called by systems managing level progression.
     * 
     * @param vampireLevel The new vampire level.
     */
    public void setVampireLevel(int vampireLevel) {
        // Add validation if needed (e.g., non-negative)
        this.vampireLevel = Math.max(0, vampireLevel);
    }

    // --- Infection State --- 

    public boolean isInfected() {
        return this.infectionLevel > 0.0;
    }

    public double getInfectionLevel() {
        return infectionLevel;
    }

    /**
     * Internal method to set infection level. Clamping applied.
     * Effects/Vampire conversion handled by VampireManager/VampireTask.
     */
    public void setInfectionLevelInternal(double infection) {
        if (this.isVampire) {
            this.infectionLevel = 0.0;
            return;
        }
        this.infectionLevel = Math.max(0.0, Math.min(1.0, infection));
    }

    public String getInfectionReason() {
        return infectionReason;
    }

    public void setInfectionReason(String reason) {
        this.infectionReason = reason;
    }

    public long getInfectionTime() {
        return infectionTime;
    }

    public void setInfectionTime(long time) {
        this.infectionTime = time;
    }
    
    public UUID getMakerId() {
        return makerId;
    }
    
    public void setMakerId(UUID makerId) {
        this.makerId = makerId;
    }
    
    // --- Mode States --- 

    public boolean isBloodlusting() {
        return bloodlusting;
    }
    
    public void setBloodlusting(boolean bloodlusting) {
        this.bloodlusting = this.isVampire && bloodlusting;
        if (this.bloodlusting) this.lastBloodlustTime = System.currentTimeMillis();
    }

    public boolean isUsingNightVision() {
        return usingNightVision;
    }
    
    public void setUsingNightVision(boolean usingNightVision) {
        this.usingNightVision = this.isVampire && usingNightVision;
    }

    public boolean isIntending() {
        return intending;
    }

    public void setIntending(boolean intending) {
        this.intending = this.isVampire && intending;
    }

    // --- Timestamps/Cooldowns --- 

    public long getLastShriekTime() {
        return lastShriekTime;
    }

    public void setLastShriekTime(long time) {
        this.lastShriekTime = time;
    }

    public long getLastBloodlustTime() {
        return lastBloodlustTime;
    }

    public long getLastModeChange() {
        return lastModeChange;
    }

    public void setLastModeChange(long lastModeChange) {
        this.lastModeChange = lastModeChange;
    }
    
    /**
     * Gets the system timestamp (milliseconds) of the last time this player received damage.
     * Used to determine if regeneration should be paused.
     * 
     * @return The timestamp of the last damage event.
     */
    public long getLastDamageTime() {
        return lastDamageTime;
    }
    
    /**
     * Sets the system timestamp (milliseconds) of the last time this player received damage.
     * Should be called by the damage listener.
     * 
     * @param lastDamageTime The system time when damage occurred.
     */
    public void setLastDamageTime(long lastDamageTime) {
        this.lastDamageTime = lastDamageTime;
    }
    
    // --- Trade State --- 

    public long getLastBloodTradeTime() {
        return lastBloodTradeTime;
    }

    public void setLastBloodTradeTime(long time) {
        this.lastBloodTradeTime = time;
    }

    public UUID getLastBloodTradePartner() {
        return lastBloodTradePartner;
    }

    public void setLastBloodTradePartner(UUID partner) {
        this.lastBloodTradePartner = partner;
    }

    public double getLastBloodTradeAmount() {
        return lastBloodTradeAmount;
    }

    public void setLastBloodTradeAmount(double amount) {
        this.lastBloodTradeAmount = amount;
    }

    public String getLastBloodTradeType() {
        return lastBloodTradeType;
    }

    public void setLastBloodTradeType(String type) {
        this.lastBloodTradeType = type;
    }

    public UUID getTradeOfferedFromUuid() {
        return tradeOfferedFromUuid;
    }

    public void setTradeOffer(UUID senderUuid, double amount) {
        this.tradeOfferedFromUuid = senderUuid;
        this.tradeOfferedAmount = amount;
        this.tradeOfferedAtTime = System.currentTimeMillis();
    }

    public void clearTradeOffer() {
        this.tradeOfferedFromUuid = null;
        this.tradeOfferedAmount = 0.0;
        this.tradeOfferedAtTime = 0L;
    }

    public double getTradeOfferedAmount() {
        return tradeOfferedAmount;
    }
    
    public long getTradeOfferedAtTime() {
        return tradeOfferedAtTime;
    }
    
    // --- Gift Offer State --- 

    /**
     * Gets the UUID of the player who made the pending Dark Gift offer.
     * 
     * @return The offering player's UUID, or null if no offer is pending.
     */
    public UUID getPendingGiftOfferUuid() {
        return pendingGiftOfferUuid;
    }

    /**
     * Gets the timestamp when the pending Dark Gift offer was made.
     * 
     * @return The offer timestamp (milliseconds), or 0 if no offer is pending.
     */
    public long getPendingGiftOfferTime() {
        return pendingGiftOfferTime;
    }

    /**
     * Sets the pending Dark Gift offer details.
     * Should only be called by VampireManager.
     * 
     * @param offererUuid The UUID of the player making the offer.
     * @param offerTime The timestamp when the offer was made.
     */
    public void setPendingGiftOffer(UUID offererUuid, long offerTime) {
        this.pendingGiftOfferUuid = offererUuid;
        this.pendingGiftOfferTime = offerTime;
    }

    /**
     * Clears the pending Dark Gift offer.
     * Should be called by VampireManager when an offer is accepted, rejected, or expires.
     */
    public void clearPendingGiftOffer() {
        this.pendingGiftOfferUuid = null;
        this.pendingGiftOfferTime = 0L;
    }

} 