package org.clockworx.vampire.event;

import org.clockworx.vampire.entity.VampirePlayer;

public class EventVampirePlayerModeChange extends AbstractVampireEvent {
    protected final String mode;
    
    public EventVampirePlayerModeChange(String mode, VampirePlayer vampirePlayer) {
        super(vampirePlayer);
        this.mode = mode;
    }
    
    public String getMode() {
        return this.mode;
    }
} 