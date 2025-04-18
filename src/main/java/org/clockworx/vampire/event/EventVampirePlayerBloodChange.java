package org.clockworx.vampire.event;

import org.clockworx.vampire.entity.VampirePlayer;

public class EventVampirePlayerBloodChange extends AbstractVampireEvent {
    protected final double blood;
    
    public EventVampirePlayerBloodChange(double blood, VampirePlayer vampirePlayer) {
        super(vampirePlayer);
        this.blood = blood;
    }
    
    public double getBlood() {
        return this.blood;
    }
} 