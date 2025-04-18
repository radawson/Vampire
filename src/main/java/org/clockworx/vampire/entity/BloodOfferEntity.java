package org.clockworx.vampire.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "blood_offers")
public class BloodOfferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sender_uuid", nullable = false)
    private UUID senderUuid;
    
    @Column(name = "target_uuid", nullable = false)
    private UUID targetUuid;
    
    @Column(nullable = false)
    private double amount;
    
    @Column(nullable = false)
    private long timestamp;
    
    @Column(nullable = false)
    private boolean accepted;
    
    @Column(nullable = false)
    private boolean rejected;

    // Default constructor required by Hibernate
    protected BloodOfferEntity() {}

    public BloodOfferEntity(UUID senderUuid, UUID targetUuid, double amount) {
        this.senderUuid = senderUuid;
        this.targetUuid = targetUuid;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.accepted = false;
        this.rejected = false;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public UUID getSenderUuid() { return senderUuid; }
    public void setSenderUuid(UUID senderUuid) { this.senderUuid = senderUuid; }
    
    public UUID getTargetUuid() { return targetUuid; }
    public void setTargetUuid(UUID targetUuid) { this.targetUuid = targetUuid; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    public boolean isAccepted() { return accepted; }
    public void setAccepted(boolean accepted) { this.accepted = accepted; }
    
    public boolean isRejected() { return rejected; }
    public void setRejected(boolean rejected) { this.rejected = rejected; }
} 