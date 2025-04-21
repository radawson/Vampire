package org.clockworx.vampire.util;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.config.LanguageConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for handling various resources, formatting, and messaging tasks 
 * within the Vampire plugin. This includes:
 * <ul>
 *   <li>Player inventory management (checking, adding, removing items).</li>
 *   <li>Formatting numbers (blood, percentages) and time durations.</li>
 *   <li>Text colorization and message sending (though {@link VampireMessages} is preferred).</li>
 *   <li>Retrieval of localized messages via {@link LanguageConfig}.</li>
 *   <li>Basic item description generation.</li>
 * </ul>
 * 
 * All methods are static for easy access.
 */
public class ResourceUtil
{
	/** The VampirePlugin instance, needed for accessing configs like LanguageConfig. */
	private static VampirePlugin plugin;
	/** Formatter for displaying blood values (e.g., "15.5"). */
	private static final DecimalFormat BLOOD_FORMAT = new DecimalFormat("0.0");
	/** Formatter for displaying percentages (e.g., "75.0%"). */
	private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("0.0%");

	/**
	 * Initializes the ResourceUtil with the main plugin instance.
	 * This must be called once during plugin startup.
	 * 
	 * @param plugin The {@link VampirePlugin} instance.
	 */
	public static void init(VampirePlugin plugin) {
		ResourceUtil.plugin = plugin;
	}

	/**
	 * Checks if a player has at least a specified amount of a particular item 
	 * in their inventory, matching type and durability.
	 * 
	 * @param player The {@link Player} whose inventory to check.
	 * @param stack The {@link ItemStack} representing the item to check for (type, amount, durability).
	 * @return {@code true} if the player has at least the required amount of the item, {@code false} otherwise.
	 * @deprecated Uses {@link ItemStack#getDurability()}, consider {@link org.bukkit.inventory.meta.Damageable} or custom item data for modern versions.
	 */
	@Deprecated
	public static boolean playerHas(Player player, ItemStack stack)
	{
		Material requiredType = stack.getType();
		// Note: getDurability() is deprecated. For damageable items, use Damageable meta.
		// For custom variants, PersistentDataContainer or custom model data might be better.
		short requiredDamage = stack.getDurability();
		int requiredAmount = stack.getAmount();
		
		int actualAmount = 0;
		for (ItemStack pstack : player.getInventory().getContents())
		{
			if (pstack == null) continue;
			if (pstack.getType() != requiredType) continue;
			// Check durability match
			if (pstack.getDurability() != requiredDamage) continue;
			actualAmount += pstack.getAmount();
		}
		
		return actualAmount >= requiredAmount;
	}
	
	/**
	 * Checks if a player has all the items specified in a collection in their inventory.
	 * Uses {@link #playerHas(Player, ItemStack)} for each item.
	 * 
	 * @param player The {@link Player} whose inventory to check.
	 * @param stacks A {@link Collection} of {@link ItemStack}s to check for.
	 * @return {@code true} if the player possesses all items in the collection, {@code false} otherwise.
	 */
	public static boolean playerHas(Player player, Collection<? extends ItemStack> stacks)
	{
		for (ItemStack stack : stacks)
		{
			// If any item is missing, return false immediately.
			if ( ! playerHas(player, stack)) return false;
		}
		// All items were found.
		return true;
	}
	
	/**
	 * Removes items specified in a collection from a player's inventory.
	 * Uses {@link PlayerInventory#removeItem(ItemStack...)}.
	 * 
	 * @param player The {@link Player} from whom to remove items.
	 * @param stacks A {@link Collection} of {@link ItemStack}s to remove.
	 */
	public static void playerRemove(Player player, Collection<? extends ItemStack> stacks)
	{
		// Convert collection to array for removeItem varargs.
		playerRemove(player, stacks.toArray(new ItemStack[0]));
	}
	
	/**
	 * Removes specified items from a player's inventory.
	 * Uses {@link PlayerInventory#removeItem(ItemStack...)} and updates the inventory.
	 * 
	 * @param player The {@link Player} from whom to remove items.
	 * @param stacks The {@link ItemStack}s to remove (varargs).
	 */
	public static void playerRemove(Player player, ItemStack... stacks)
	{
		// Bukkit's method handles removing the specified amounts across multiple stacks.
		player.getInventory().removeItem(stacks);
		// Ensure the client inventory is updated visually.
		player.updateInventory();
	}
	
	/**
	 * Adds items specified in a collection to a player's inventory.
	 * If the inventory is full, items may be dropped on the ground.
	 * Uses {@link Inventory#addItem(ItemStack...)}.
	 * 
	 * @param player The {@link Player} to whom items should be added.
	 * @param stacks A {@link Collection} of {@link ItemStack}s to add.
	 */
	public static void playerAdd(Player player, Collection<? extends ItemStack> stacks)
	{
		Inventory inventory = player.getInventory();
		// Convert collection to array for addItem varargs.
		inventory.addItem(stacks.toArray(new ItemStack[0]));
		// Ensure the client inventory is updated visually.
		player.updateInventory();
	}
	
	/**
	 * Adds a single item stack to a player's inventory.
	 * If the inventory is full, the item may be dropped on the ground.
	 * Uses {@link Inventory#addItem(ItemStack...)}.
	 * 
	 * @param player The {@link Player} to whom the item should be added.
	 * @param stack The {@link ItemStack} to add.
	 */
	public static void playerAdd(Player player, ItemStack stack)
	{
		Inventory inventory = player.getInventory();
		inventory.addItem(stack);
		// Ensure the client inventory is updated visually.
		player.updateInventory();
	}
	
	/**
	 * Creates a human-readable description of a collection of item stacks.
	 * Example: "10 Stone, 5 Oak Log"
	 * 
	 * @param stacks The {@link Collection} of {@link ItemStack}s to describe.
	 * @return A single String listing the items and their amounts, separated by commas.
	 */
	public static String describe(Collection<? extends ItemStack> stacks)
	{
		ArrayList<String> lines = new ArrayList<>();
		for (ItemStack stack : stacks)
		{
			// Get the description for the specific material and damage value.
			String desc = describe(stack.getType(), stack.getDurability());
			lines.add(String.format("%d %s", stack.getAmount(), desc));
		}
		// Join the individual descriptions with ", ".
		return String.join(", ", lines);
	}
	
	/**
	 * Creates a human-readable description for a specific material and damage value.
	 * Includes special cases for common items like Water Bottle, Lapis Lazuli, Charcoal.
	 * Otherwise, formats the material name (lowercase, underscores replaced with spaces).
	 * 
	 * @param type The {@link Material} of the item.
	 * @param damage The damage value (used for variants like dyes, coal type).
	 * @return A String describing the item (e.g., "Water Bottle", "lapis lazuli dye", "stone").
	 * @deprecated Uses damage value for variants. Modern approach uses specific Materials 
	 *             (e.g., LAPIS_LAZULI) or ItemMeta/PersistentDataContainer.
	 */
	@Deprecated
	public static String describe(Material type, short damage)
	{
		// Handle specific common cases based on type and damage.
		if (type == Material.POTION && damage == 0) return "Water Bottle";
		// Note: INK_SAC is legacy. Modern versions use explicit dye materials (e.g., LAPIS_LAZULI).
		if (type == Material.INK_SAC && damage == 4 ) return "Lapis Lazuli Dye"; 
		// Note: COAL damage value distinguishes charcoal. Modern versions use CHARCOAL material.
		if (type == Material.COAL && damage == 1 ) return "Charcoal";
		
		// Default: return the material name, cleaned up.
		return type.name().toLowerCase().replace("_", " ");
	}
	
	/**
	 * Sends a message to a command sender after colorizing it.
	 * 
	 * @param sender The {@link CommandSender} (Player, Console) to send the message to.
	 * @param message The raw message string (using '&' for color codes).
	 * @deprecated Prefer using {@link VampireMessages#send(CommandSender, String)} for consistency and localization.
	 */
	@Deprecated
	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(colorize(message));
	}
	
	/**
	 * Sends an error message (prefixed with red color code) to a command sender.
	 * 
	 * @param sender The {@link CommandSender} to send the error message to.
	 * @param message The raw error message string.
	 * @deprecated Prefer using {@link VampireMessages} methods (e.g., sending a localized error message).
	 */
	@Deprecated
	public static void sendError(CommandSender sender, String message) {
		sender.sendMessage(colorize("&c" + message));
	}
	
	/**
	 * Sends a success message (prefixed with green color code) to a command sender.
	 * 
	 * @param sender The {@link CommandSender} to send the success message to.
	 * @param message The raw success message string.
	 * @deprecated Prefer using {@link VampireMessages} methods.
	 */
	@Deprecated
	public static void sendSuccess(CommandSender sender, String message) {
		sender.sendMessage(colorize("&a" + message));
	}
	
	/**
	 * Sends an info message (prefixed with gray color code) to a command sender.
	 * 
	 * @param sender The {@link CommandSender} to send the info message to.
	 * @param message The raw info message string.
	 * @deprecated Prefer using {@link VampireMessages} methods.
	 */
	@Deprecated
	public static void sendInfo(CommandSender sender, String message) {
		sender.sendMessage(colorize("&7" + message));
	}
	
	/**
	 * Sends a warning message (prefixed with yellow color code) to a command sender.
	 * 
	 * @param sender The {@link CommandSender} to send the warning message to.
	 * @param message The raw warning message string.
	 * @deprecated Prefer using {@link VampireMessages} methods.
	 */
	@Deprecated
	public static void sendWarning(CommandSender sender, String message) {
		sender.sendMessage(colorize("&e" + message));
	}
	
	/**
	 * Formats a double representing a blood amount using the predefined {@link #BLOOD_FORMAT}.
	 * 
	 * @param amount The blood amount (double).
	 * @return A formatted string (e.g., "15.0", "8.5").
	 */
	public static String formatBlood(double amount) {
		return BLOOD_FORMAT.format(amount);
	}
	
	/**
	 * Formats a double representing a percentage using the predefined {@link #PERCENTAGE_FORMAT}.
	 * Assumes the input is a fraction (e.g., 0.75 for 75%).
	 * 
	 * @param percentage The percentage value (as a fraction, e.g., 0.0 to 1.0).
	 * @return A formatted string (e.g., "75.0%").
	 */
	public static String formatPercentage(double percentage) {
		return PERCENTAGE_FORMAT.format(percentage);
	}
	
	/**
	 * Formats a duration given in seconds into a human-readable string 
	 * showing seconds, minutes, or hours and minutes.
	 * 
	 * @param seconds The total duration in seconds.
	 * @return A formatted string (e.g., "30 seconds", "5 minutes", "2 hours, 15 minutes").
	 */
	public static String formatTime(long seconds) {
		if (seconds < 0) seconds = 0; // Handle negative durations gracefully

		if (seconds < 60) {
			return seconds + (seconds == 1 ? " second" : " seconds");
		}
		
		long minutes = seconds / 60;
		long remainingSeconds = seconds % 60;
		
		if (minutes < 60) {
			String result = minutes + (minutes == 1 ? " minute" : " minutes");
			if (remainingSeconds > 0) {
				result += ", " + remainingSeconds + (remainingSeconds == 1 ? " second" : " seconds");
			}
			return result;
		}
		
		long hours = minutes / 60;
		long remainingMinutes = minutes % 60;
		String result = hours + (hours == 1 ? " hour" : " hours");
		if (remainingMinutes > 0) {
			result += ", " + remainingMinutes + (remainingMinutes == 1 ? " minute" : " minutes");
		}
		// Optionally add seconds back for very long durations if needed
		// if (remainingSeconds > 0) { ... }
		return result;
	}
	
	/**
	 * Translates Minecraft color codes (using '&') within a string to actual colors.
	 * 
	 * @param text The input string potentially containing color codes (e.g., "&cError: &fSomething went wrong").
	 * @return The string with color codes translated for display in Minecraft.
	 */
	public static String colorize(String text) {
		// Standard Bukkit method for color code translation.
		return ChatColor.translateAlternateColorCodes('&', text);
	}
	
	/**
	 * Broadcasts a colorized message to all online players.
	 * 
	 * @param message The raw message string (using '&' for color codes).
	 * @deprecated Prefer using {@link VampireMessages#broadcast(String)} for consistency and localization.
	 */
	@Deprecated
	public static void broadcastMessage(String message) {
		plugin.getServer().broadcastMessage(colorize(message));
	}
	
	/**
	 * Broadcasts a colorized error message (prefixed with red) to all online players.
	 * 
	 * @param message The raw error message string.
	 * @deprecated Prefer using {@link VampireMessages} methods.
	 */
	@Deprecated
	public static void broadcastError(String message) {
		plugin.getServer().broadcastMessage(colorize("&c" + message));
	}
	
	/**
	 * Broadcasts a colorized success message (prefixed with green) to all online players.
	 * 
	 * @param message The raw success message string.
	 * @deprecated Prefer using {@link VampireMessages} methods.
	 */
	@Deprecated
	public static void broadcastSuccess(String message) {
		plugin.getServer().broadcastMessage(colorize("&a" + message));
	}
	
	/**
	 * Broadcasts a colorized warning message (prefixed with yellow) to all online players.
	 * 
	 * @param message The raw warning message string.
	 * @deprecated Prefer using {@link VampireMessages} methods.
	 */
	@Deprecated
	public static void broadcastWarning(String message) {
		plugin.getServer().broadcastMessage(colorize("&e" + message));
	}
	
	/**
	 * Broadcasts a colorized info message (prefixed with gray) to all online players.
	 * 
	 * @param message The raw info message string.
	 * @deprecated Prefer using {@link VampireMessages} methods.
	 */
	@Deprecated
	public static void broadcastInfo(String message) {
		plugin.getServer().broadcastMessage(colorize("&7" + message));
	}
	
	/**
	 * Gets a message directly from the loaded language configuration using its key.
	 * Returns the key itself if the message is not found.
	 * 
	 * @param key The key identifying the message in the language configuration.
	 * @return The localized message string, or the key if not found.
	 */
	public static String getMessage(String key) {
		// Delegates to the LanguageConfig instance managed by the plugin.
		return plugin.getLanguageConfig().getMessage(key);
	}
	
	/**
	 * Gets a message from the loaded language configuration using its key 
	 * and replaces placeholders ({0}, {1}, etc.) with the provided arguments.
	 * Returns the key itself if the message is not found.
	 * 
	 * @param key The key identifying the message in the language configuration.
	 * @param args The arguments to insert into the message placeholders.
	 * @return The localized and formatted message string, or the key if not found.
	 */
	public static String getMessage(String key, String... args) {
		// Delegates to the LanguageConfig instance managed by the plugin.
		return plugin.getLanguageConfig().getMessage(key, args);
	}

	/**
	 * Generates a user-friendly name for a given Material.
	 * Converts the Material enum name to lowercase, replaces underscores with spaces,
	 * and capitalizes the first letter of each word.
	 * Example: OAK_LOG -> "Oak Log"
	 * 
	 * @param material The {@link Material} to get the name for.
	 * @return A formatted, user-friendly string name for the material, or "Unknown" if null.
	 */
	public static String getMaterialName(Material material) {
	    if (material == null) return "Unknown";
	    
	    // Start with the enum name (e.g., "OAK_LOG").
	    String name = material.name();
	    // Convert to lower case ("oak_log").
	    name = name.toLowerCase();
	    // Replace underscores with spaces ("oak log").
	    name = name.replace('_', ' ');
	    
	    StringBuilder result = new StringBuilder();
	    boolean capitalize = true; // Capitalize the first character
	    
	    // Iterate through characters to capitalize after spaces.
	    for (char c : name.toCharArray()) {
	        if (Character.isWhitespace(c)) { // Check for whitespace instead of just space ' '
	            result.append(c);
	            capitalize = true; // Capitalize the next non-whitespace character
	        } else {
	            result.append(capitalize ? Character.toUpperCase(c) : c);
	            capitalize = false; // Don't capitalize subsequent characters until next whitespace
	        }
	    }
	    
	    return result.toString();
	}
}
