package org.clockworx.vampire.util;

import java.text.DecimalFormat;
import java.util.Collection;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.config.LanguageConfig;

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
	 * Checks if a player has all the items specified in a collection in their inventory.
	 * Uses {@link #playerHas(Player, ItemStack)} for each item.
	 * 
	 * @param player The {@link Player} whose inventory to check.
	 * @param stacks A {@link Collection} of {@link ItemStack}s to check for.
	 * @return {@code true} if the player possesses all items in the collection, {@code false} otherwise.
	 */
	public static boolean playerHas(Player player, Collection<? extends ItemStack> stacks)
	{
		// Iterate through each required item stack in the collection
		for (ItemStack requiredStack : stacks)
		{
			Material requiredType = requiredStack.getType();
			int requiredAmount = requiredStack.getAmount();
			int foundAmount = 0;

			// Iterate through the player's inventory to count matching items
			for (ItemStack pstack : player.getInventory().getContents()) {
				if (pstack != null && pstack.getType() == requiredType) {
					foundAmount += pstack.getAmount();
				}
			}

			// If the found amount is less than required for this item, the player doesn't have enough
			if (foundAmount < requiredAmount) {
				return false;
			}
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
		// Delegate to VampireMessages which uses the modern Adventure API for color translation
		return VampireMessages.formatMessage(text);
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
