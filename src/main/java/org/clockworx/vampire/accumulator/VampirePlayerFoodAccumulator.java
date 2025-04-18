package org.clockworx.vampire.accumulator;

import org.clockworx.vampire.entity.VampirePlayer;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;

/**
 * Manages a player's food level with persistence.
 * This accumulator is specifically designed for handling food-related mechanics.
 */
public class VampirePlayerFoodAccumulator extends VampirePlayerAccumulator
{
	private final VampirePlugin plugin;

	public VampirePlayerFoodAccumulator(VampirePlugin plugin, VampirePlayer vampirePlayer) {
		super(vampirePlayer, 20.0, 0.0, 20.0);
		this.plugin = plugin;
	}

	@Override
	protected void save() {
		Player player = getPlayer();
		if (player != null) {
			player.setFoodLevel((int) getValue());
		}
	}

	/**
	 * Updates the food level based on the player's current state.
	 * This method should be called periodically to ensure food levels are synchronized.
	 */
	public void update() {
		Player player = getPlayer();
		if (player != null) {
			setValue(player.getFoodLevel());
		}
	}
}