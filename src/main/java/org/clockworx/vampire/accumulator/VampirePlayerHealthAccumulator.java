package org.clockworx.vampire.accumulator;

import org.clockworx.vampire.entity.VampirePlayer;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;

/**
 * Manages a player's health level with persistence.
 * This accumulator is specifically designed for handling health-related mechanics.
 */
public class VampirePlayerHealthAccumulator extends VampirePlayerAccumulator
{
	private final VampirePlugin plugin;

	public VampirePlayerHealthAccumulator(VampirePlugin plugin, VampirePlayer vampirePlayer) {
		super(vampirePlayer, 20.0, 0.0, 20.0);
		this.plugin = plugin;
	}

	@Override
	protected void save() {
		Player player = getPlayer();
		if (player != null) {
			player.setHealth(getValue());
		}
	}

	/**
	 * Updates the health level based on the player's current state.
	 * This method should be called periodically to ensure health levels are synchronized.
	 */
	public void update() {
		Player player = getPlayer();
		if (player != null) {
			setValue(player.getHealth());
		}
	}
} 