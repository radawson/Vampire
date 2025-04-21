package org.clockworx.vampire.util;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.config.VampireConfig;
import org.clockworx.vampire.util.VampireMessages;
import org.bukkit.configuration.ConfigurationSection;

// Imports needed for Action Bar
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Utility class for calculating sun-related factors affecting vampires.
 * This includes:
 * <ul>
 *   <li>Determining the time of day relative to noon.</li>
 *   <li>Calculating the sun's angle and effective radiation strength.</li>
 *   <li>Calculating the opacity provided by terrain and armor against sunlight.</li>
 *   <li>Combining these factors to determine the final solar irradiation affecting a player.</li>
 * </ul>
 */
public class SunUtil
{
	// Static plugin instance, set via init()
	private static VampirePlugin plugin;

	/**
	 * Initializes the SunUtil with the main plugin instance.
	 * This must be called once during plugin startup.
	 * 
	 * @param plugin The {@link VampirePlugin} instance, used for accessing configurations.
	 */
	public static void init(VampirePlugin plugin) {
		SunUtil.plugin = plugin;
	}

	/** The time in ticks representing midday (peak sun). */
	public final static int MID_DAY_TICKS = 6000;
	/** The total number of ticks in a full Minecraft day-night cycle. */
	public final static int DAY_TICKS = 24000;
	/** The number of ticks in half a Minecraft day-night cycle. */
	public final static int HALF_DAY_TICKS = DAY_TICKS / 2;
	/** The approximate duration in ticks of daylight (sunrise to sunset). */
	public final static int DAYTIME_TICKS = 14000;
	/** Half the duration of daylight ticks. */
	public final static int HALF_DAYTIME_TICKS = DAYTIME_TICKS / 2;
	/** Constant for Pi / 2, used in angle calculations. */
	public final static double HALF_PI = Math.PI / 2;
	/** Factor to convert the delta ticks from midday to the sun angle in radians. */
	public final static double MDTICKS_TO_ANGLE_FACTIOR = HALF_PI / HALF_DAYTIME_TICKS;

	
	/**
	 * Calculates the time difference in ticks relative to midday ({@link #MID_DAY_TICKS}).
	 * The result ranges roughly from -7000 (sunrise) to +7000 (sunset).
	 * 0 indicates midday.
	 * Handles wrapping around the {@link #DAY_TICKS} cycle.
	 * 
	 * @param world The {@link World} to calculate the time for.
	 * @return The time delta in ticks from midday.
	 */
	public static int calcMidDeltaTicks(World world)
	{
		// Calculate the remainder of the full time relative to a day cycle, offset by midday.
		// Example: Time 7000 -> (7000 - 6000) % 24000 = 1000
		// Example: Time 0 (midnight) -> (0 - 6000) % 24000 = -6000. Needs adjustment.
		// Example: Time 23000 -> (23000 - 6000) % 24000 = 17000. Needs adjustment.
		int ret = (int) ((world.getFullTime() - MID_DAY_TICKS) % DAY_TICKS);
		
		// Adjust for times past midnight but before the next midday.
		// If the result is >= 12000 (half day), it means we wrapped past midnight.
		// Subtracting a full day brings it into the negative range representing time after evening.
		// Example: 17000 (late night) -> 17000 - 24000 = -7000 (approaching sunrise from night)
		if (ret >= HALF_DAY_TICKS) 
		{
			ret -= DAY_TICKS;
		}
		// Example: -6000 (midnight) remains -6000 (correctly before sunrise)
		return ret;
	}
	
	/**
	 * Calculates the sun's angle relative to the zenith (directly overhead).
	 * The angle is in radians.
	 * 0 radians means the sun is directly overhead (midday).
	 * -Pi/2 radians (~-1.57) corresponds to sunrise.
	 * +Pi/2 radians (~+1.57) corresponds to sunset.
	 * 
	 * @param world The {@link World} to calculate the sun angle for.
	 * @return The sun angle in radians.
	 */
	public static double calcSunAngle(World world)
	{
		// Get the time delta from midday.
		int mdticks = calcMidDeltaTicks(world);
		// Convert the time delta to an angle using the precalculated factor.
		// This maps the range [-HALF_DAYTIME_TICKS, +HALF_DAYTIME_TICKS] to [-Pi/2, +Pi/2].
		return MDTICKS_TO_ANGLE_FACTIOR * mdticks;
	}
	
	/**
	 * Calculates the effective solar radiation intensity, scaled between 0.0 and 1.0.
	 * 0.0 means no sunlight (night, storm, wrong environment).
	 * 1.0 means maximum sunlight (midday, clear sky, normal world).
	 * The calculation is based on the sine of the sun's elevation angle (angle above horizon).
	 * See: <a href="http://en.wikipedia.org/wiki/Effect_of_sun_angle_on_climate">Effect of sun angle on climate</a>
	 * 
	 * @param world The {@link World} to calculate solar radiation for.
	 * @return A value between 0.0 and 1.0 representing the solar radiation intensity.
	 */
	public static double calcSolarRad(World world)
	{
		// No sun effect in dimensions other than the Overworld.
		if (world.getEnvironment() != Environment.NORMAL) return 0d;
		// No sun effect during rain or thunderstorms.
		if (world.hasStorm()) return 0d;
		
		// Calculate the sun angle relative to zenith (0 = overhead, +/- Pi/2 = horizon).
		double angle = calcSunAngle(world);
		// Get the absolute angle from zenith.
		double absangle = Math.abs(angle);
		
		// If the absolute angle is Pi/2 or more, the sun is at or below the horizon.
		if (absangle >= HALF_PI) return 0;
		
		// Calculate the elevation angle (angle above the horizon).
		// Elevation = Pi/2 - abs(angle from zenith).
		double elevationAngle = HALF_PI - absangle;
		
		// Solar radiation intensity is proportional to the sine of the elevation angle.
		// sin(0) = 0 (horizon), sin(Pi/2) = 1 (overhead).
		return Math.sin(elevationAngle);
	}

	/**
	 * Calculates the total opacity provided by the terrain blocks directly above a given block.
	 * It sums the opacity values (obtained from config) of each block from the starting block's Y level 
	 * up to the world's maximum height, capping the total opacity at 1.0.
	 * 
	 * @param block The {@link Block} at the base Y level to start checking from.
	 * @return The total terrain opacity, ranging from 0.0 (clear sky) to 1.0 (fully blocked).
	 * @deprecated Uses {@link World#getMaxHeight()} and {@link Block#getType()} which are deprecated.
	 */
	@SuppressWarnings("deprecation") // Suppress warnings for World.getMaxHeight() and Block.getType()
	@Deprecated
	public static double calcTerrainOpacity(Block block)
	{
		double ret = 0;
		
		int x = block.getX();
		int z = block.getZ();
		World world = block.getWorld();
		// Note: World.getMaxHeight() is deprecated. Modern alternatives depend on context,
		// but for checking skylight, iterating up to build limit is often still needed.
		int maxy = world.getLogicalHeight() - 1; // Use logical height instead
		
		// Iterate vertically upwards from the starting block's Y.
		for (int y = block.getY(); y <= maxy && ret < 1d; y++) // Stop if max opacity is reached
		{
			// Note: Block.getType() is deprecated. Use Block.getBlockData().getMaterial() in modern API.
			Material material = world.getBlockAt(x, y, z).getBlockData().getMaterial(); // MODERN API
			// Get opacity value from plugin config, defaulting to 1.0 (fully opaque) if not specified.
			Double opacity = plugin.getVampireConfig().getBlockOpacity(material);
			if (opacity == null) {
				opacity = 1d; // Blocks not explicitly listed are assumed fully opaque.
			}
			ret += opacity;
		}
		
		// Cap the total opacity at 1.0.
		if (ret > 1.0D) ret = 1d;
		
		// P.p.log("calcTerrainOpacity",ret); // Leftover debug logging
		
		return ret;
	}

	
	/**
	 * Calculates the total opacity provided by the player's armor, based on 
	 * configured material base opacities and slot type weights.
	 *
	 * @param player The player whose armor is being checked.
	 * @param config The {@link VampireConfig} instance containing armor settings.
	 * @return The total opacity value from armor (0.0 to potentially > 1.0).
	 */
	public static double calcArmorOpacity(Player player, VampireConfig config) {
		PlayerInventory inventory = player.getInventory();
		ItemStack[] armor = inventory.getArmorContents();
		double totalOpacity = 0.0;

		Map<String, Double> baseOpacities = config.getArmorBaseMaterialOpacities();
		Map<String, Double> typeWeights = config.getArmorTypeWeights();

		if (baseOpacities == null || typeWeights == null) {
			VampireMessages.error("Armor opacity config maps are null! Check config loading.");
			return 0.0; // Return 0 if config is broken
		}

		for (ItemStack armorPiece : armor) {
			if (armorPiece != null && armorPiece.getType() != Material.AIR) {
				Material material = armorPiece.getType();
				String baseMaterial = getArmorBaseMaterial(material);
				String armorType = getArmorType(material);

				if (baseMaterial != null && armorType != null) {
					// Get opacity from config, default to 0 if not found
					double baseOpacity = baseOpacities.getOrDefault(baseMaterial.toUpperCase(), 0.0); // Use uppercase for map keys
					double typeWeight = typeWeights.getOrDefault(armorType.toUpperCase(), 0.0); // Use uppercase for map keys

					VampireMessages.debug("Armor piece: " + material.name() + 
										  ", BaseMaterial: " + baseMaterial + " (Opacity: " + baseOpacity + ")" +
										  ", Type: " + armorType + " (Weight: " + typeWeight + ")" +
										  ", Contribution: " + (baseOpacity * typeWeight));
					totalOpacity += baseOpacity * typeWeight;
				} else {
					VampireMessages.debug("Could not determine base material or type for: " + material.name());
				}
			}
		}
		
		// Let's NOT clamp total opacity here. Let the damage calculation handle the final effect.
		VampireMessages.debug("Total calculated armor opacity: " + totalOpacity);
		return totalOpacity;
	}

	// Helper patterns for parsing armor material names
	private static final Pattern ARMOR_MATERIAL_PATTERN = Pattern.compile("^(NETHERITE|DIAMOND|GOLDEN|IRON|CHAINMAIL|LEATHER|TURTLE)_(HELMET|CHESTPLATE|LEGGINGS|BOOTS)$");
	private static final Pattern ELYTRA_PATTERN = Pattern.compile("^ELYTRA$");

	/**
	 * Extracts the base material name (e.g., "DIAMOND", "LEATHER") from an armor Material.
	 * Returns null if the material is not recognized armor.
	 * Handles ELYTRA specifically.
	 * 
	 * @param material The armor {@link Material}.
	 * @return The uppercase base material name, "ELYTRA", or null.
	 */
	private static String getArmorBaseMaterial(Material material) {
		if (material == null) return null;
		String materialName = material.name();

		Matcher matcher = ARMOR_MATERIAL_PATTERN.matcher(materialName);
		if (matcher.matches()) {
			return matcher.group(1); // Group 1 is the base material
		}
		
		if (ELYTRA_PATTERN.matcher(materialName).matches()) {
			return "ELYTRA"; // Special case for Elytra
		}

		return null; // Not a recognized armor material
	}

	/**
	 * Extracts the armor type (e.g., "HELMET", "CHESTPLATE") from an armor Material.
	 * Returns null if the material is not recognized armor.
	 * Handles ELYTRA specifically.
	 * 
	 * @param material The armor {@link Material}.
	 * @return The uppercase armor type, "ELYTRA", or null.
	 */
	private static String getArmorType(Material material) {
		if (material == null) return null;
		String materialName = material.name();

		Matcher matcher = ARMOR_MATERIAL_PATTERN.matcher(materialName);
		if (matcher.matches()) {
			return matcher.group(2); // Group 2 is the armor type
		}
		
		if (ELYTRA_PATTERN.matcher(materialName).matches()) {
			return "ELYTRA"; // Special case for Elytra
		}

		return null; // Not a recognized armor type
	}

	/**
	 * Calculates the final effective solar irradiation affecting a player, between 0.0 and 1.0.
	 * This value considers:
	 * 1. Base solar radiation based on time of day, weather, and world ({@link #calcSolarRad}).
	 * 2. Reduction due to terrain opacity above the player ({@link #calcTerrainOpacity}).
	 * 3. Reduction due to armor opacity worn by the player ({@link #calcArmorOpacity}).
	 * 
	 * Irradiation = SolarRad * (1 - TerrainOpacity) * (1 - ArmorOpacity)
	 * (Values clamped between 0 and 1 where appropriate).
	 * 
	 * @param player The {@link Player} to calculate irradiation for.
	 * @return The effective solar irradiation value (0.0 to 1.0).
	 */
	public static double calcPlayerIrradiation(Player player)
	{
		// Basic checks: Player must be online and alive.
		if ( ! player.isOnline()) return 0;
		if (player.isDead()) return 0;
		
		// 1. Calculate base solar radiation based on world, time, weather.
		World world = player.getWorld();
		double ret = calcSolarRad(world);
		// If base radiation is zero, no further calculation needed.
		if (ret == 0) return 0;
		
		// 2. Factor in terrain opacity.
		// Check the block directly above the player's head location.
		Block block = player.getLocation().getBlock().getRelative(0, 1, 0); 
		double terrainOpacity = calcTerrainOpacity(block);
		// Reduce radiation by the terrain opacity (opacity is 0 to 1).
		// (1 - terrainOpacity) gives the fraction of light passing through.
		ret *= (1 - terrainOpacity);
		// If terrain blocks all light, result is zero.
		if (ret <= 0) return 0; // Use <= for safety with potential floating point inaccuracies
		
		// 3. Factor in armor opacity.
		double armorOpacity = calcArmorOpacity(player, plugin.getVampireConfig());
		// Reduce remaining radiation by the armor opacity.
		// Clamp armor opacity effect: Max reduction is 100% (opacity >= 1.0).
		double armorFactor = Math.max(0.0, 1.0 - armorOpacity); 
		ret *= armorFactor;
		// If armor blocks all remaining light, result is zero.
		if (ret <= 0) return 0;
		
		// P.p.log("calcPlayerIrradiation",ret); // Leftover debug logging
		
		// Return the final calculated irradiation value, ensuring it's not negative.
		return Math.max(0.0, ret);
	}
	
	/**
	 * Applies sun-related effects to a player based on the calculated solar irradiation.
	 *
	 * @param player The player to apply effects to.
	 * @param totalOpacity The calculated solar irradiation value (0.0 to 1.0).
	 * @param sunlightLevel The current sunlight level.
	 */
	public static void applySunEffects(Player player, double totalOpacity, int sunlightLevel) {
		// Example: Reduce effect duration or amplifier based on opacity
		// This is just a placeholder, replace with actual logic
		int durationTicks = (int) (200 * (1.0 - totalOpacity)); // Longer duration for less opacity
		int amplifier = (totalOpacity < 0.5) ? 1 : 0; // Higher amplifier for very low opacity
		
		if (durationTicks > 0) {
			VampireMessages.debug("Applying sun effects: Duration=" + durationTicks + " ticks, Amplifier=" + amplifier);
			player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, amplifier));
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, amplifier));
			// Consider adding fire damage for very low opacity / high sunlight
			if (totalOpacity < 0.1 && sunlightLevel > 12) { // Example threshold
				player.setFireTicks(Math.max(player.getFireTicks(), 40)); // 2 seconds of fire
			}
		}
		
		// Potentially send a message based on exposure level
		if (totalOpacity < 0.2 && sunlightLevel > 13) {
			VampireMessages.sendActionBarMessage(player, "&cThe sun burns! Find shelter!");
		} else if (totalOpacity < 0.7 && sunlightLevel > 10) {
			VampireMessages.sendActionBarMessage(player, "&eThe sun feels uncomfortable...");
		}
	}

	private static void loadOpacityMaps() {
		// ... (existing code) ...
		if (helmetOpacityMap == null || chestplateOpacityMap == null || leggingsOpacityMap == null || bootsOpacityMap == null) {
			VampireMessages.error("Armor opacity config maps are null! Check config loading.", null);
		}
	}

	private static void applySunDebuffs(Player player, double sunExposure) {
		VampireConfig config = VampirePlugin.getPlugin(VampirePlugin.class).getVampireConfig();
		int durationTicks = 40; // Apply for 2 seconds, task will re-apply if needed

		// Apply weakness regardless of exposure level (if exposed at all)
		player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, durationTicks, 0));

		// Apply slow based on exposure threshold
		if (sunExposure > config.getSunSlowThreshold()) {
			int amplifier = (sunExposure > 0.8) ? 1 : 0; // Example: higher slow at high exposure
			player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, amplifier));
		}

		// Apply blindness at higher exposure
		if (sunExposure > config.getSunBlindnessThreshold()) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationTicks, 0));
		}

		// Send Action Bar message based on severity
		if (sunExposure >= config.getSunBurnThreshold()) {
			TextComponent burnMessage = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&cThe sun burns! Find shelter!"));
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, burnMessage);
		} else if (sunExposure > 0.1) { // Threshold for feeling uncomfortable
			TextComponent discomfortMessage = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&eThe sun feels uncomfortable..."));
			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, discomfortMessage);
		}
	}
}
