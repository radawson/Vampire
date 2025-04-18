package org.clockworx.vampire.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "vampire_players")
public class VampirePlayerEntity {
    @Id
    private UUID uuid;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "is_vampire")
    private boolean isVampire;
    
    @Column(name = "blood_level")
    private double bloodLevel;
    
    @Column(name = "infection_level")
    private double infectionLevel;
    
    @Column(name = "infection_reason")
    private String infectionReason;
    
    @Column(name = "infection_time")
    private long infectionTime;
    
    @Column(name = "last_shriek_time")
    private long lastShriekTime;
    
    @Column(name = "last_blood_trade_time")
    private long lastBloodTradeTime;
    
    @Column(name = "last_blood_trade_partner")
    private UUID lastBloodTradePartner;
    
    @Column(name = "last_blood_trade_amount")
    private double lastBloodTradeAmount;
    
    @Column(name = "last_blood_trade_type")
    private String lastBloodTradeType;

    // Default constructor required by Hibernate
    protected VampirePlayerEntity() {}

    public VampirePlayerEntity(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.isVampire = false;
        this.bloodLevel = 0.0;
        this.infectionLevel = 0.0;
        this.infectionTime = 0L;
        this.lastShriekTime = 0L;
        this.lastBloodTradeTime = 0L;
    }

    // Getters and setters
    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public boolean isVampire() { return isVampire; }
    public void setVampire(boolean vampire) { isVampire = vampire; }
    
    public double getBloodLevel() { return bloodLevel; }
    public void setBloodLevel(double bloodLevel) { this.bloodLevel = bloodLevel; }
    
    public double getInfectionLevel() { return infectionLevel; }
    public void setInfectionLevel(double infectionLevel) { this.infectionLevel = infectionLevel; }
    
    public String getInfectionReason() { return infectionReason; }
    public void setInfectionReason(String infectionReason) { this.infectionReason = infectionReason; }
    
    public long getInfectionTime() { return infectionTime; }
    public void setInfectionTime(long infectionTime) { this.infectionTime = infectionTime; }
    
    public long getLastShriekTime() { return lastShriekTime; }
    public void setLastShriekTime(long lastShriekTime) { this.lastShriekTime = lastShriekTime; }
    
    public long getLastBloodTradeTime() { return lastBloodTradeTime; }
    public void setLastBloodTradeTime(long lastBloodTradeTime) { this.lastBloodTradeTime = lastBloodTradeTime; }
    
    public UUID getLastBloodTradePartner() { return lastBloodTradePartner; }
    public void setLastBloodTradePartner(UUID lastBloodTradePartner) { this.lastBloodTradePartner = lastBloodTradePartner; }
    
    public double getLastBloodTradeAmount() { return lastBloodTradeAmount; }
    public void setLastBloodTradeAmount(double lastBloodTradeAmount) { this.lastBloodTradeAmount = lastBloodTradeAmount; }
    
    public String getLastBloodTradeType() { return lastBloodTradeType; }
    public void setLastBloodTradeType(String lastBloodTradeType) { this.lastBloodTradeType = lastBloodTradeType; }
} 