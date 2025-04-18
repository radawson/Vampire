package org.clockworx.vampire.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.clockworx.vampire.altar.AltarAbstract;
import org.clockworx.vampire.entity.VampirePlayer;

/**
 * Event that is fired when a player attempts to use an altar.
 * This event can be cancelled to prevent the altar from being used.
 */
public class EventAltarUse extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    
    private final AltarAbstract altar;
    private final VampirePlayer vampirePlayer;
    private final Player player;
    
    /**
     * Creates a new EventAltarUse event.
     * 
     * @param altar The altar being used
     * @param vampirePlayer The VampirePlayer instance of the player
     * @param player The Bukkit Player instance
     */
    public EventAltarUse(AltarAbstract altar, VampirePlayer vampirePlayer, Player player) {
        this.altar = altar;
        this.vampirePlayer = vampirePlayer;
        this.player = player;
        this.cancelled = false;
    }
    
    /**
     * Gets the altar being used.
     * 
     * @return The altar
     */
    public AltarAbstract getAltar() {
        return altar;
    }
    
    /**
     * Gets the VampirePlayer instance of the player using the altar.
     * 
     * @return The VampirePlayer
     */
    public VampirePlayer getVampirePlayer() {
        return vampirePlayer;
    }
    
    /**
     * Gets the Bukkit Player instance of the player using the altar.
     * 
     * @return The Player
     */
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    /**
     * Gets the HandlerList for this event.
     * 
     * @return The HandlerList
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
} 