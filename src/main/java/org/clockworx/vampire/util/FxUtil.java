package org.clockworx.vampire.util;

import java.util.Map;
import java.util.Random;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.config.VampireConfig;

/**
 * Utility class for handling visual (particle) and audio (sound) effects 
 * within the Vampire plugin. Provides static methods for common effects 
 * related to players and locations.
 */
public class FxUtil
{
	/** The VampirePlugin instance, used for accessing server resources like worlds. */
	private static VampirePlugin plugin;
	/** Random number generator for particle location offsets. */
	private static final Random random = new Random();

	/**
	 * Initializes the FxUtil with the main plugin instance.
	 * This must be called once during plugin startup.
	 * 
	 * @param plugin The {@link VampirePlugin} instance.
	 */
	public static void init(VampirePlugin plugin) {
		// Store the plugin instance for later use, e.g., accessing worlds.
		FxUtil.plugin = plugin;
	}
	
	/**
	 * Ensures a player is set on fire for a minimum specified duration.
	 * If the player is already burning for longer, their fire duration is not changed.
	 * 
	 * @param player The {@link Player} to set on fire. Can be null.
	 * @param ticks The minimum duration in server ticks for the player to burn.
	 */
	public static void ensureBurn(Player player, int ticks)
	{
		// Safety check for null player
		if (player == null) return;
		// Don't overwrite if already burning for longer or equal duration
		if (player.getFireTicks() >= ticks) return; 
		player.setFireTicks(ticks);
	}
	
	/**
	 * Ensures a player has a specific potion effect for a minimum duration.
	 * If the player already has the effect for longer, it is not reapplied.
	 * Applies the effect with amplifier 0, ambient set to false, and particles set to false.
	 * 
	 * @param type The {@link PotionEffectType} to apply.
	 * @param player The {@link Player} to apply the effect to. Can be null.
	 * @param duration The minimum duration in server ticks for the effect.
	 */
	public static void ensure(PotionEffectType type, Player player, int duration)
	{
		// Safety check for null player
		if (player == null) return;
		
		PotionEffect effect = player.getPotionEffect(type);
		// Apply only if the effect is missing or its current duration is less than the desired minimum.
		if (effect == null || effect.getDuration() < duration) {
			// Apply effect level 1 (amplifier 0), non-ambient, no particles.
			player.addPotionEffect(new PotionEffect(type, duration, 0, false, false));
		}
	}
	
	/**
	 * Spawns a single large smoke particle at a specific location.
	 * 
	 * @param location The {@link Location} where the particle should spawn. Can be null.
	 */
	public static void smoke(Location location)
	{
		// Safety check for null location
		if (location == null) return;
		// Spawn one particle with no offset or extra data.
		location.getWorld().spawnParticle(Particle.LARGE_SMOKE, location, 1, 0, 0, 0, 0);
	}
	
	/**
	 * Spawns a single large smoke particle at a random location near a player 
	 * (either eye location or feet location).
	 * 
	 * @param player The {@link Player} around whom the particle should spawn. Can be null.
	 */
	public static void smoke(Player player)
	{
		// Safety check for null player
		if (player == null) return;
		Location loc = getRandomPlayerLocation(player);
		smoke(loc);
	}
	
	/**
	 * Spawns a single flame particle at a specific location.
	 * 
	 * @param location The {@link Location} where the particle should spawn. Can be null.
	 */
	public static void flame(Location location)
	{
		// Safety check for null location
		if (location == null) return;
		// Spawn one particle with no offset or extra data.
		location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0, 0, 0, 0);
	}
	
	/**
	 * Spawns a single flame particle at a random location near a player 
	 * (either eye location or feet location).
	 * 
	 * @param player The {@link Player} around whom the particle should spawn. Can be null.
	 */
	public static void flame(Player player)
	{
		// Safety check for null player
		if (player == null) return;
		Location loc = getRandomPlayerLocation(player);
		flame(loc);
	}

	
	/**
	 * Spawns a single portal (ender) particle at a specific location.
	 * 
	 * @param location The {@link Location} where the particle should spawn. Can be null.
	 */
	public static void ender(Location location)
	{
		// Safety check for null location
		if (location == null) return;
		// Spawn one particle with no offset or extra data.
		location.getWorld().spawnParticle(Particle.PORTAL, location, 1, 0, 0, 0, 0);
	}
	
	/**
	 * Spawns a single portal (ender) particle at a random location near a player,
	 * potentially offset by a random distance up to {@code randomMaxLen} blocks
	 * on each axis.
	 * 
	 * @param player The {@link Player} around whom the particle should spawn. Can be null.
	 * @param randomMaxLen The maximum random offset distance (in blocks) along each axis (x, y, z).
	 */
	public static void ender(Player player, int randomMaxLen)
	{
		// Safety check for null player
		if (player == null) return;
		Location loc = getRandomPlayerLocation(player, randomMaxLen);
		ender(loc);
	}

	/**
	 * Gets a random location associated with a player: either their main location (feet) 
	 * or their eye location.
	 * 
	 * @param player The {@link Player} whose location is needed. Must not be null.
	 * @return A randomly chosen {@link Location} (feet or eyes) of the player.
	 */
	public static Location getRandomPlayerLocation(Player player)
	{
		// 50/50 chance to return feet location or eye location.
		return random.nextBoolean() ? player.getLocation() : player.getEyeLocation();
	}
	
	/**
	 * Gets a random location around a player, applying a random offset.
	 * First, it chooses randomly between the player's feet or eye location,
	 * then adds a random delta (between -randomMaxLen and +randomMaxLen) 
	 * to each coordinate (x, y, z).
	 * 
	 * @param player The {@link Player} to get a location around. Must not be null.
	 * @param randomMaxLen The maximum random offset distance (in blocks) along each axis.
	 * @return A {@link Location} near the player with a random offset applied.
	 */
	public static Location getRandomPlayerLocation(Player player, int randomMaxLen)
	{
		// Start with a random base location (feet or eyes).
		Location loc = getRandomPlayerLocation(player);
		
		// Calculate random deltas for each axis.
		int dx = getRandomDelta(randomMaxLen);
		int dy = getRandomDelta(randomMaxLen);
		int dz = getRandomDelta(randomMaxLen);
		
		// Return a new location instance with the added offset.
		return loc.add(dx, dy, dz);
	}
	
	/**
	 * Generates a random integer delta within a specified range.
	 * The range is from -randomMaxLen to +randomMaxLen (inclusive).
	 * 
	 * @param randomMaxLen The maximum absolute value of the delta.
	 * @return A random integer between -randomMaxLen and +randomMaxLen.
	 */
	public static int getRandomDelta(int randomMaxLen)
	{
		// random.nextInt(N) returns 0 to N-1.
		// So, random.nextInt(randomMaxLen * 2 + 1) returns 0 to randomMaxLen * 2.
		// Subtracting randomMaxLen shifts the range to -randomMaxLen to +randomMaxLen.
		return random.nextInt(randomMaxLen * 2 + 1) - randomMaxLen;
	}
	
	/**
	 * Spawns a cluster of portal (ender) particles around a player.
	 * Uses default offsets and speed suitable for a general effect.
	 * 
	 * @param player The {@link Player} to spawn particles around. Can be null.
	 */
	public static void runEnder(Player player) {
		// Safety check for null player
		if (player == null) return;
		
		Location loc = player.getLocation();
		// Spawn 50 portal particles within a 0.5 block radius cube, speed 0.1.
		player.spawnParticle(Particle.PORTAL, loc, 50, 0.5, 0.5, 0.5, 0.1);
	}
	
	/**
	 * Spawns a larger burst of portal (ender) particles around a player.
	 * Uses larger offsets and speed for a more dramatic effect.
	 * 
	 * @param player The {@link Player} to spawn particles around. Can be null.
	 */
	public static void runEnderBurst(Player player) {
		// Safety check for null player
		if (player == null) return;
		
		Location loc = player.getLocation();
		// Spawn 100 portal particles within a 1.0 block radius cube, speed 0.2.
		player.spawnParticle(Particle.PORTAL, loc, 100, 1.0, 1.0, 1.0, 0.2);
	}
	
	/**
	 * Spawns a cluster of large smoke particles around a player.
	 * Uses default offsets and speed suitable for a general effect.
	 * 
	 * @param player The {@link Player} to spawn particles around. Can be null.
	 */
	public static void runSmoke(Player player) {
		// Safety check for null player
		if (player == null) return;
		
		Location loc = player.getLocation();
		// Spawn 20 smoke particles within a 0.5 block radius cube, speed 0.1.
		player.spawnParticle(Particle.LARGE_SMOKE, loc, 20, 0.5, 0.5, 0.5, 0.1);
	}
	
	/**
	 * Spawns a larger burst of large smoke particles around a player.
	 * Uses larger offsets and speed for a more dramatic effect.
	 * 
	 * @param player The {@link Player} to spawn particles around. Can be null.
	 */
	public static void runSmokeBurst(Player player) {
		// Safety check for null player
		if (player == null) return;
		
		Location loc = player.getLocation();
		// Spawn 100 smoke particles within a 1.0 block radius cube, speed 0.2.
		player.spawnParticle(Particle.LARGE_SMOKE, loc, 100, 1.0, 1.0, 1.0, 0.2);
	}
	
	/**
	 * Spawns a cluster of heart (healing) particles around a player.
	 * Uses default offsets and speed suitable for a general effect.
	 * 
	 * @param player The {@link Player} to spawn particles around. Can be null.
	 */
	public static void runHeal(Player player) {
		// Safety check for null player
		if (player == null) return;
		
		Location loc = player.getLocation();
		// Spawn 10 heart particles within a 0.5 block radius cube, speed 0.1.
		player.spawnParticle(Particle.HEART, loc, 10, 0.5, 0.5, 0.5, 0.1);
	}
	
	/**
	 * Spawns a larger burst of heart (healing) particles around a player.
	 * Uses larger offsets and speed for a more dramatic effect.
	 * 
	 * @param player The {@link Player} to spawn particles around. Can be null.
	 */
	public static void runHealBurst(Player player) {
		// Safety check for null player
		if (player == null) return;
		
		Location loc = player.getLocation();
		// Spawn 30 heart particles within a 1.0 block radius cube, speed 0.2.
		player.spawnParticle(Particle.HEART, loc, 30, 1.0, 1.0, 1.0, 0.2);
	}

	/**
	 * Generic method to spawn particles at a specific location with detailed control.
	 * 
	 * @param location The {@link Location} to spawn particles at. Can be null.
	 * @param particle The {@link Particle} type to spawn.
	 * @param count The number of particles to spawn.
	 * @param offsetX The random offset range along the X-axis.
	 * @param offsetY The random offset range along the Y-axis.
	 * @param offsetZ The random offset range along the Z-axis.
	 * @param speed The speed (or extra data) of the particle. Meaning varies by particle type.
	 */
	public static void playParticle(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed) {
		// Safety check for null location
		if (location == null) return;
		location.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
	}

	/**
	 * Plays a sound at a specific location using its string key.
	 * 
	 * @param location The {@link Location} to play the sound at. Can be null.
	 * @param soundKey The string key of the sound (e.g., "minecraft:entity.player.levelup").
	 * @param volume The volume of the sound (1.0 is default).
	 * @param pitch The pitch of the sound (1.0 is default).
	 */
	public static void playSound(Location location, String soundKey, float volume, float pitch) {
		// Safety check for null location or empty key
		if (location == null || soundKey == null || soundKey.isEmpty()) return;
		// Use the version of playSound that accepts a String key
		location.getWorld().playSound(location, soundKey, volume, pitch);
	}

	/**
	 * Plays the combined particle and sound effect for vampire transformation.
	 * Uses LARGE_SMOKE particles and the sound defined by config `vampire.shriek.sound_key`.
	 * 
	 * @param player The {@link Player} undergoing the effect. Can be null.
	 */
	public static void playVampireEffect(Player player) {
		// Safety check for null player
		if (player == null) return;
		Location loc = player.getLocation();
		// Use the generic particle and sound methods for consistency.
		playParticle(loc, Particle.LARGE_SMOKE, 20, 0.5, 1, 0.5, 0.1); // Smoke spread wider vertically
		// Get sound key from config
		String soundKey = plugin.getVampireConfig().getShriekSoundKey(); // Use the new method
		playSound(loc, soundKey, 1.0f, 0.5f); // Low pitch teleport sound (NOTE: Config defaults to ghast scream, sound here is enderman teleport pitch - consider aligning config/code)
	}

	/**
	 * Plays a combined particle and sound effect typically used when collecting blood.
	 * Spawns red dust particles and plays a player hurt sound.
	 * 
	 * @param location The {@link Location} where the blood collection occurs. Can be null.
	 */
	public static void playBloodEffect(Location location) {
		// Safety check for null location
		if (location == null) return;
		// Specific options for red dust particles.
		DustOptions dustOptions = new DustOptions(Color.RED, 1.0f); // Red color, size 1.0
		location.getWorld().spawnParticle(Particle.DUST, location, 10, 0.2, 0.2, 0.2, 0, dustOptions); // Tight cluster, no speed, specific data
		// Assuming blood effect sound is not configurable, keep using enum for now
		location.getWorld().playSound(location, Sound.ENTITY_PLAYER_HURT, 0.5f, 1.0f); // Quieter hurt sound, default pitch 
	}

	/**
	 * Plays a combined particle and sound effect typically used for infection.
	 * Spawns witch particles and plays a Wither ambient sound.
	 * 
	 * @param player The {@link Player} associated with the infection effect. Can be null.
	 */
	public static void playInfectionEffect(Player player) {
		// Safety check for null player
		if (player == null) return;
		Location loc = player.getLocation();
		VampireConfig config = plugin.getVampireConfig();

		// Play Sound from Config
		String soundKey = config.getInfectionSoundKey();
		playSound(loc, soundKey, 0.5f, 2.0f); // Example volume/pitch

		// Play Particles from Config
		Map<String, Object> particleSettings = config.getInfectionParticleSettings();
		Particle particleType = config.getParticleType(particleSettings, Particle.WITCH);
		int count = config.getParticleInt(particleSettings, "count", 30);
		double offsetX = config.getParticleDouble(particleSettings, "offset_x", 0.5);
		double offsetY = config.getParticleDouble(particleSettings, "offset_y", 1.0);
		double offsetZ = config.getParticleDouble(particleSettings, "offset_z", 0.5);
		double speed = config.getParticleDouble(particleSettings, "speed", 0.1);
		playParticle(loc, particleType, count, offsetX, offsetY, offsetZ, speed);
	}

	/**
	 * Plays a combined particle and sound effect typically used for curing vampirism/infection.
	 * Spawns entity effect (potion swirl) particles and plays a player level-up sound.
	 * 
	 * @param player The {@link Player} being cured. Can be null.
	 */
	public static void playCureEffect(Player player) {
		// Safety check for null player
		if (player == null) return;
		Location loc = player.getLocation();
		VampireConfig config = plugin.getVampireConfig();

		// Play Sound from Config
		String soundKey = config.getCureSoundKey();
		playSound(loc, soundKey, 1.0f, 1.0f); // Example volume/pitch

		// Play Particles from Config
		Map<String, Object> particleSettings = config.getCureParticleSettings();
		Particle particleType = config.getParticleType(particleSettings, Particle.ENTITY_EFFECT);
		int count = config.getParticleInt(particleSettings, "count", 30);
		double offsetX = config.getParticleDouble(particleSettings, "offset_x", 0.5);
		double offsetY = config.getParticleDouble(particleSettings, "offset_y", 1.0);
		double offsetZ = config.getParticleDouble(particleSettings, "offset_z", 0.5);
		double speed = config.getParticleDouble(particleSettings, "speed", 0.1);
		playParticle(loc, particleType, count, offsetX, offsetY, offsetZ, speed);

		// Old hardcoded calls:
		// playParticle(loc, Particle.ENTITY_EFFECT, 30, 0.5, 1, 0.5, 0.1);
		// loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
	}

	// --- New Centralized Effect Methods ---

	/**
	 * Plays the sound and particle effects associated with a successful Dark Altar ritual.
	 * Uses sound key from config: `altar.dark.success_sound_key`
	 * 
	 * @param player The player who successfully used the altar.
	 */
	public static void playAltarDarkSuccessEffect(Player player) {
		if (player == null) return;
		Location loc = player.getLocation();
		String soundKey = plugin.getVampireConfig().getAltarDarkSuccessSoundKey();
		playSound(loc, soundKey, 1.0f, 0.5f); // Example volume/pitch
		// Add associated particles (e.g., the Wither ambient particles from original code)
		playParticle(loc, Particle.WITCH, 20, 0.5, 0.8, 0.5, 0.1);
	}

	/**
	 * Plays the sound and particle effects associated with a successful Light Altar ritual.
	 * Uses sound key from config: `altar.light.success_sound_key`
	 * 
	 * @param player The player who successfully used the altar.
	 */
	public static void playAltarLightSuccessEffect(Player player) {
		if (player == null) return;
		Location loc = player.getLocation();
		String soundKey = plugin.getVampireConfig().getAltarLightSuccessSoundKey();
		playSound(loc, soundKey, 1.0f, 1.2f); // Example volume/pitch
		// Add associated particles (e.g., beacon power select particles?)
		// FxUtil.runHeal(player); // Or maybe the heal particles?
		playParticle(loc, Particle.HAPPY_VILLAGER, 25, 0.5, 0.8, 0.5, 0.1); // From original weaken curse case

	}
	
	/**
	 * Plays the sound effect for a failed altar ritual (e.g., player moved).
	 * Uses sound key from config: `altar.fail_sound_key`
	 * 
	 * @param location The location where the failure occurred.
	 */
	public static void playAltarFailEffect(Location location) {
		if (location == null) return;
		String soundKey = plugin.getVampireConfig().getAltarFailSoundKey();
		playSound(location, soundKey, 0.5f, 0.5f); // Example volume/pitch (low pitch teleport)
		// Optional: Add failure particles?
	}

	/**
	 * Plays the sound and particle effects associated with the Shriek ability.
	 * Uses sound key from config: `vampire.shriek.sound_key`
	 * 
	 * @param player The player performing the shriek.
	 */
	public static void playShriekEffect(Player player) {
		if (player == null) return;
		Location loc = player.getLocation();
		String soundKey = plugin.getVampireConfig().getShriekSoundKey();
		playSound(loc, soundKey, 1.0f, 1.0f); // Default volume/pitch for shriek sound
		// Add particles from original shriek code
		player.getWorld().strikeLightningEffect(loc); 
		playParticle(loc, Particle.LARGE_SMOKE, 50, 1, 1, 1, 0.1);
	}
}
