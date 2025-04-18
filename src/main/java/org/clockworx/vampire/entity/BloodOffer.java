package org.clockworx.vampire.entity;

import java.util.UUID;

/**
 * Represents a blood offer from one player to another.
 */
public class BloodOffer {
    private final UUID senderUuid;
    private final UUID targetUuid;
    private final double amount;
    private final long timestamp;
    private boolean accepted;
    private boolean rejected;

    /**
     * Creates a new blood offer.
     * 
     * @param senderUuid The UUID of the player offering blood
     * @param targetUuid The UUID of the player being offered blood
     * @param amount The amount of blood being offered
     */
    public BloodOffer(UUID senderUuid, UUID targetUuid, double amount) {
        this.senderUuid = senderUuid;
        this.targetUuid = targetUuid;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.accepted = false;
        this.rejected = false;
    }

    /**
     * Gets the UUID of the player offering blood.
     * 
     * @return The sender's UUID
     */
    public UUID getSenderUuid() {
        return senderUuid;
    }

    /**
     * Gets the UUID of the player being offered blood.
     * 
     * @return The target's UUID
     */
    public UUID getTargetUuid() {
        return targetUuid;
    }

    /**
     * Gets the amount of blood being offered.
     * 
     * @return The blood amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Gets the timestamp when the offer was created.
     * 
     * @return The offer timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Checks if the offer has been accepted.
     * 
     * @return true if accepted, false otherwise
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * Sets the offer as accepted.
     */
    public void setAccepted() {
        this.accepted = true;
    }

    /**
     * Checks if the offer has been rejected.
     * 
     * @return true if rejected, false otherwise
     */
    public boolean isRejected() {
        return rejected;
    }

    /**
     * Sets the offer as rejected.
     */
    public void setRejected() {
        this.rejected = true;
    }

    /**
     * Checks if the offer is still pending (not accepted or rejected).
     * 
     * @return true if pending, false otherwise
     */
    public boolean isPending() {
        return !accepted && !rejected;
    }

    /**
     * Checks if the offer has expired based on the given timeout.
     * 
     * @param timeoutMillis The timeout in milliseconds
     * @return true if expired, false otherwise
     */
    public boolean isExpired(long timeoutMillis) {
        return System.currentTimeMillis() - timestamp > timeoutMillis;
    }
} 