package org.clockworx.vampire.event;

import org.clockworx.vampire.entity.VampirePlayer;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a player's vampirism infection level changes.
 * This event is fired in the following scenarios:
 * 1. Initial Infection: When a player first becomes infected (infection > 0.0)
 * 2. Infection Increase: When a player's infection level increases (e.g., from AltarDark)
 * 3. Infection Decrease: When a player's infection level decreases (e.g., from AltarLight)
 * 4. Cure: When a player's infection is completely removed (infection <= 0.0)
 * 
 * The infection level is a double value between 0.0 and 1.0:
 * - 0.0: Not infected
 * - 0.1-0.9: Infected with varying severity
 * - 1.0: Fully infected (becomes a vampire)
 * 
 * This event can be cancelled to prevent the infection change from occurring.
 */
public class EventVampirePlayerInfectionChange extends AbstractVampireEvent
{

	
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }

	
	/**
	 * The new infection level that will be applied if the event is not cancelled.
	 * This value should be between 0.0 and 1.0.
	 */
	protected final double infection;

	
	/**
	 * Creates a new infection change event.
	 * 
	 * @param infection The new infection level to be applied
	 * @param vampirePlayer The player whose infection is changing
	 */
	public EventVampirePlayerInfectionChange(double infection, VampirePlayer vampirePlayer)
	{
		super(vampirePlayer);
		this.infection = infection;
	}
	
	/**
	 * Gets the new infection level that will be applied if the event is not cancelled.
	 * 
	 * @return The new infection level (0.0 to 1.0)
	 */
	public double getInfection() {
		return this.infection;
	}
}
