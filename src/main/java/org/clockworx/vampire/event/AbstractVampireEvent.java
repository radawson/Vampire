package org.clockworx.vampire.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.clockworx.vampire.entity.VampirePlayer;

public abstract class AbstractVampireEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
    protected final VampirePlayer vampirePlayer;
    private boolean cancelled;
    
    protected AbstractVampireEvent(VampirePlayer vampirePlayer) {
        this.vampirePlayer = vampirePlayer;
        this.cancelled = false;
    }
    
    public VampirePlayer getVampirePlayer() {
        return this.vampirePlayer;
    }
    
    public boolean isVampire() {
        return this.vampirePlayer.isVampire();
    }
    
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
} 