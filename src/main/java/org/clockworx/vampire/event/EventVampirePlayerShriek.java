package org.clockworx.vampire.event;

import org.clockworx.vampire.entity.VampirePlayer;

public class EventVampirePlayerShriek extends AbstractVampireEvent {
    
    public EventVampirePlayerShriek(VampirePlayer vampirePlayer) {
        super(vampirePlayer);
    }
} 