package org.clockworx.vampire.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.clockworx.vampire.VampirePlugin;

import java.util.Random;

/**
 * Utility class for handling visual and sound effects in the Vampire plugin.
 * This class provides methods for:
 * <ul>
 *   <li>Player buffs and effects</li>
 *   <li>Particle effects</li>
 *   <li>Sound effects</li>
 *   <li>Combined visual and sound effects</li>
 * </ul>
 */
public class FxUtil
{
	private static VampirePlugin plugin;
	private static final Random random = new Random();

	/**
	 * Initializes the FxUtil with the plugin instance.
	 * 
	 * @param plugin The VampirePlugin instance
	 */
	public static void init(VampirePlugin plugin) {
		FxUtil.plugin = plugin;
	}
	
	/**
	 * Ensures a player is burning for a specified duration.
	 * 
	 * @param player The player to set on fire
	 * @param ticks The duration in ticks
	 */
	public static void ensureBurn(Player player, int ticks)
	{
		if (player == null) return;
		if (player.getFireTicks() > 0) return;
		player.setFireTicks(ticks);
	}
	
	/**
	 * Ensures a player has a potion effect for a specified duration.
	 * 
	 * @param type The potion effect type
	 * @param player The player to apply the effect to
	 * @param duration The duration in ticks
	 */
	public static void ensure(PotionEffectType type, Player player, int duration)
	{
		if (player == null) return;
		
		PotionEffect effect = player.getPotionEffect(type);
		if (effect == null || effect.getDuration() < duration) {
			player.addPotionEffect(new PotionEffect(type, duration, 0, false, false));
		}
	}
	
	/**
	 * Spawns smoke particles at a location.
	 * 
	 * @param location The location to spawn particles at
	 */
	public static void smoke(Location location)
	{
		if (location == null) return;
		location.getWorld().spawnParticle(Particle.SMOKE_NORMAL, location, 1, 0, 0, 0, 0);
	}
	
	/**
	 * Spawns smoke particles around a player.
	 * 
	 * @param player The player to spawn particles around
	 */
	public static void smoke(Player player)
	{
		if (player == null) return;
		Location loc = getRandomPlayerLocation(player);
		smoke(loc);
	}
	
	/**
	 * Spawns flame particles at a location.
	 * 
	 * @param location The location to spawn particles at
	 */
	public static void flame(Location location)
	{
		if (location == null) return;
		location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0);
	}
	
	/**
	 * Spawns flame particles around a player.
	 * 
	 * @param player The player to spawn particles around
	 */
	public static void flame(Player player)
	{
		if (player == null) return;
		Location loc = getRandomPlayerLocation(player);
		flame(loc);
	}

	
	/**
	 * Spawns ender particles at a location.
	 * 
	 * @param location The location to spawn particles at
	 */
	public static void ender(Location location)
	{
		if (location == null) return;
		location.getWorld().spawnParticle(Particle.PORTAL, location, 1, 0, 0, 0, 0);
	}
	
	/**
	 * Spawns ender particles around a player with random offset.
	 * 
	 * @param player The player to spawn particles around
	 * @param randomMaxLen The maximum random offset
	 */
	public static void ender(Player player, int randomMaxLen)
	{
		if (player == null) return;
		Location loc = getRandomPlayerLocation(player, randomMaxLen);
		ender(loc);
	}

	/**
	 * Gets a random location around a player.
	 * 
	 * @param player The player to get a location around
	 * @return A random location around the player
	 */
	public static Location getRandomPlayerLocation(Player player)
	{
		return random.nextBoolean() ? player.getLocation() : player.getEyeLocation();
	}
	
	/**
	 * Gets a random location around a player with offset.
	 * 
	 * @param player The player to get a location around
	 * @param randomMaxLen The maximum random offset
	 * @return A random location around the player with offset
	 */
	public static Location getRandomPlayerLocation(Player player, int randomMaxLen)
	{
		Location loc = getRandomPlayerLocation(player);
		
		int dx = getRandomDelta(randomMaxLen);
		int dy = getRandomDelta(randomMaxLen);
		int dz = getRandomDelta(randomMaxLen);
		
		return loc.add(dx, dy, dz);
	}
	
	/**
	 * Gets a random delta value.
	 * 
	 * @param randomMaxLen The maximum value
	 * @return A random value between -randomMaxLen and randomMaxLen
	 */
	public static int getRandomDelta(int randomMaxLen)
	{
		return random.nextInt(randomMaxLen*2+1) - randomMaxLen;
	}
	
	/**
	 * Spawns ender particles around a player.
	 * 
	 * @param player The player to spawn particles around
	 */
	public static void runEnder(Player player) {
		if (player == null) return;
		
		Location loc = player.getLocation();
		player.spawnParticle(Particle.PORTAL, loc, 50, 0.5, 0.5, 0.5, 0.1);
	}
	
	/**
	 * Spawns a burst of ender particles around a player.
	 * 
	 * @param player The player to spawn particles around
	 */
	public static void runEnderBurst(Player player) {
		if (player == null) return;
		
		Location loc = player.getLocation();
		player.spawnParticle(Particle.PORTAL, loc, 100, 1.0, 1.0, 1.0, 0.2);
	}
	
	/**
	 * Spawns smoke particles around a player.
	 * 
	 * @param player The player to spawn particles around
	 */
	public static void runSmoke(Player player) {
		if (player == null) return;
		
		Location loc = player.getLocation();
		player.spawnParticle(Particle.SMOKE_NORMAL, loc, 20, 0.5, 0.5, 0.5, 0.1);
	}
	
	/**
	 * Spawns a burst of smoke particles around a player.
	 * 
	 * @param player The player to spawn particles around
	 */
	public static void runSmokeBurst(Player player) {
		if (player == null) return;
		
		Location loc = player.getLocation();
		player.spawnParticle(Particle.SMOKE_NORMAL, loc, 100, 1.0, 1.0, 1.0, 0.2);
	}
	
	/**
	 * Spawns healing particles around a player.
	 * 
	 * @param player The player to spawn particles around
	 */
	public static void runHeal(Player player) {
		if (player == null) return;
		
		Location loc = player.getLocation();
		player.spawnParticle(Particle.HEART, loc, 10, 0.5, 0.5, 0.5, 0.1);
	}
	
	/**
	 * Spawns a burst of healing particles around a player.
	 * 
	 * @param player The player to spawn particles around
	 */
	public static void runHealBurst(Player player) {
		if (player == null) return;
		
		Location loc = player.getLocation();
		player.spawnParticle(Particle.HEART, loc, 30, 1.0, 1.0, 1.0, 0.2);
	}

	/**
	 * Spawns particles at a location.
	 * 
	 * @param location The location to spawn particles at
	 * @param particle The particle type
	 * @param count The number of particles
	 * @param offsetX The X offset
	 * @param offsetY The Y offset
	 * @param offsetZ The Z offset
	 * @param speed The particle speed
	 */
	public static void playParticle(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
		if (location == null) return;
		location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
	}

	/**
	 * Plays a sound at a location.
	 * 
	 * @param location The location to play the sound at
	 * @param sound The sound to play
	 * @param volume The volume
	 * @param pitch The pitch
	 */
	public static void playSound(Location location, Sound sound, float volume, float pitch) {
		if (location == null) return;
		location.getWorld().playSound(location, sound, volume, pitch);
	}

	/**
	 * Plays vampire transformation effects.
	 * 
	 * @param player The player to play effects for
	 */
	public static void playVampireEffect(Player player) {
		if (player == null) return;
		Location loc = player.getLocation();
		playParticle(loc, Particle.SMOKE_NORMAL, 20, 0.5, 1, 0.5, 0.1);
		playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.5f);
	}

	/**
	 * Plays blood collection effects.
	 * 
	 * @param location The location to play effects at
	 */
	public static void playBloodEffect(Location location) {
		if (location == null) return;
		playParticle(location, Particle.REDSTONE, 10, 0.2, 0.2, 0.2, 0);
		playSound(location, Sound.ENTITY_PLAYER_HURT, 0.5f, 1.0f);
	}

	/**
	 * Plays infection effects.
	 * 
	 * @param player The player to play effects for
	 */
	public static void playInfectionEffect(Player player) {
		if (player == null) return;
		Location loc = player.getLocation();
		playParticle(loc, Particle.SPELL_WITCH, 30, 0.5, 1, 0.5, 0.1);
		playSound(loc, Sound.ENTITY_WITHER_AMBIENT, 0.5f, 2.0f);
	}

	/**
	 * Plays cure effects.
	 * 
	 * @param player The player to play effects for
	 */
	public static void playCureEffect(Player player) {
		if (player == null) return;
		Location loc = player.getLocation();
		playParticle(loc, Particle.SPELL_MOB, 30, 0.5, 1, 0.5, 0.1);
		playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
	}
}
