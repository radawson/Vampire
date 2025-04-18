package org.clockworx.vampire.accumulator;

import org.clockworx.vampire.entity.VampirePlayer;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;

/**
 * Manages a player's experience level with persistence.
 * This accumulator is specifically designed for handling experience-related mechanics.
 */
public class VampirePlayerExperienceAccumulator extends VampirePlayerAccumulator
{
	private final VampirePlugin plugin;

	public VampirePlayerExperienceAccumulator(VampirePlugin plugin, VampirePlayer vampirePlayer) {
		super(vampirePlayer, 100.0, 0.0, 100.0);
		this.plugin = plugin;
	}

	@Override
	protected void save() {
		Player player = getPlayer();
		if (player != null) {
			player.setExp((float) (getValue() / 100.0));
		}
	}

	/**
	 * Updates the experience level based on the player's current state.
	 * This method should be called periodically to ensure experience levels are synchronized.
	 */
	public void update() {
		Player player = getPlayer();
		if (player != null) {
			setValue(player.getExp() * 100.0);
		}
	}
} 