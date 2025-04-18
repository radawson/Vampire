package org.clockworx.vampire.accumulator;

import org.clockworx.vampire.entity.VampirePlayer;
import org.bukkit.entity.Player;

/**
 * Represents an accumulator that is tied to a specific player.
 * This class provides player-specific value management with persistence.
 */
public abstract class VampirePlayerAccumulator extends Accumulator
{
	protected final VampirePlayer vampirePlayer;

	protected VampirePlayerAccumulator(VampirePlayer vampirePlayer, double value, double min, double max) {
		super(value, min, max);
		this.vampirePlayer = vampirePlayer;
	}

	/**
	 * Gets the associated VampirePlayer instance.
	 * @return The VampirePlayer instance
	 */
	public VampirePlayer getVampirePlayer() {
		return vampirePlayer;
	}

	/**
	 * Gets the Bukkit Player instance.
	 * @return The Player instance, or null if the player is offline
	 */
	public Player getPlayer() {
		return vampirePlayer.getPlayer();
	}

	/**
	 * Saves the current value to persistent storage.
	 * This method should be called when the value changes significantly.
	 */
	protected abstract void save();

	@Override
	public synchronized void setValue(double value) {
		double oldValue = getValue();
		super.setValue(value);
		if (Math.abs(oldValue - value) > 0.01) {
			save();
		}
	}

	@Override
	public synchronized void addValue(double value) {
		double oldValue = getValue();
		super.addValue(value);
		if (Math.abs(oldValue - getValue()) > 0.01) {
			save();
		}
	}
} 