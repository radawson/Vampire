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
 * Utility class for handling various resources and formatting in the Vampire plugin.
 * This class provides methods for:
 * <ul>
 *   <li>Player inventory management (adding/removing items)</li>
 *   <li>Message formatting and sending</li>
 *   <li>Time and number formatting</li>
 *   <li>Text colorization</li>
 *   <li>Language message retrieval</li>
 * </ul>
 * 
 * All methods in this class are static for easy access throughout the plugin.
 */
public class ResourceUtil
{
	private static VampirePlugin plugin;
	private static final DecimalFormat BLOOD_FORMAT = new DecimalFormat("0.0");
	private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("0.0%");

	/**
	 * Initializes the ResourceUtil with the plugin instance.
	 * This method must be called before using any other methods in this class.
	 * 
	 * @param plugin The VampirePlugin instance
	 */
	public static void init(VampirePlugin plugin) {
		ResourceUtil.plugin = plugin;
	}

	/**
	 * Checks if a player has a specific item in their inventory.
	 * 
	 * @param player The player to check
	 * @param stack The item stack to check for
	 * @return true if the player has the item, false otherwise
	 */
	public static boolean playerHas(Player player, ItemStack stack)
	{
		Material requiredType = stack.getType();
		short requiredDamage = stack.getDurability();
		int requiredAmount = stack.getAmount();
		
		int actualAmount = 0;
		for (ItemStack pstack : player.getInventory().getContents())
		{
			if (pstack == null) continue;
			if (pstack.getType() != requiredType) continue;
			if (pstack.getDurability() != requiredDamage) continue;
			actualAmount += pstack.getAmount();
		}
		
		return actualAmount >= requiredAmount;
	}
	
	/**
	 * Checks if a player has all the items in a collection.
	 * 
	 * @param player The player to check
	 * @param stacks The collection of item stacks to check for
	 * @return true if the player has all items, false otherwise
	 */
	public static boolean playerHas(Player player, Collection<? extends ItemStack> stacks)
	{
		for (ItemStack stack : stacks)
		{
			if ( ! playerHas(player, stack)) return false;
		}
		return true;
	}
	
	/**
	 * Removes items from a player's inventory.
	 * 
	 * @param player The player to remove items from
	 * @param stacks The collection of item stacks to remove
	 */
	public static void playerRemove(Player player, Collection<? extends ItemStack> stacks)
	{
		playerRemove(player, stacks.toArray(new ItemStack[0]));
	}
	
	/**
	 * Removes items from a player's inventory.
	 * 
	 * @param player The player to remove items from
	 * @param stacks The item stacks to remove
	 */
	public static void playerRemove(Player player, ItemStack... stacks)
	{
		player.getInventory().removeItem(stacks);
		player.updateInventory();
	}
	
	/**
	 * Adds items to a player's inventory.
	 * 
	 * @param player The player to add items to
	 * @param stacks The collection of item stacks to add
	 */
	public static void playerAdd(Player player, Collection<? extends ItemStack> stacks)
	{
		Inventory inventory = player.getInventory();
		inventory.addItem(stacks.toArray(new ItemStack[0]));
		player.updateInventory();
	}
	
	/**
	 * Adds an item to a player's inventory.
	 * 
	 * @param player The player to add the item to
	 * @param stack The item stack to add
	 */
	public static void playerAdd(Player player, ItemStack stack)
	{
		Inventory inventory = player.getInventory();
		inventory.addItem(stack);
		player.updateInventory();
	}
	
	/**
	 * Describes a collection of item stacks.
	 * 
	 * @param stacks The collection of item stacks to describe
	 * @return A string describing the items
	 */
	public static String describe(Collection<? extends ItemStack> stacks)
	{
		ArrayList<String> lines = new ArrayList<>();
		for (ItemStack stack : stacks)
		{
			String desc = describe(stack.getType(), stack.getDurability());
			lines.add(String.format("%d %s", stack.getAmount(), desc));
		}
		return String.join(", ", lines);
	}
	
	/**
	 * Describes a material type and damage value.
	 * 
	 * @param type The material type
	 * @param damage The damage value
	 * @return A string describing the item
	 */
	public static String describe(Material type, short damage)
	{
		if (type == Material.POTION && damage == 0) return "Water Bottle";
		if (type == Material.INK_SAC && damage == 4 ) return "Lapis Lazuli Dye";
		if (type == Material.COAL && damage == 1 ) return "Charcoal";
		
		return type.name().toLowerCase().replace("_", " ");
	}
	
	/**
	 * Attempts to remove items from a player's inventory and sends a message based on success or failure.
	 * 
	 * @param player The player to remove items from
	 * @param resources The items to remove
	 * @param successMessage The message to send on success
	 * @param failMessage The message to send on failure
	 * @return true if the items were removed, false otherwise
	 */
	public static boolean playerRemoveAttempt(Player player, List<ItemStack> resources, String successMessage, String failMessage)
	{
		PlayerInventory inventory = player.getInventory();
		
		// Check if player has all required items
		for (ItemStack item : resources)
		{
			if (!hasItem(inventory, item))
			{
				sendMessage(player, failMessage);
				return false;
			}
		}
		
		// Remove all items
		for (ItemStack item : resources)
		{
			removeItem(inventory, item);
		}
		
		player.updateInventory();
		sendMessage(player, successMessage);
		return true;
	}
	
	/**
	 * Checks if an inventory contains an item.
	 * 
	 * @param inventory The inventory to check
	 * @param item The item to check for
	 * @return true if the inventory contains the item, false otherwise
	 */
	private static boolean hasItem(PlayerInventory inventory, ItemStack item) {
		Material requiredType = item.getType();
		short requiredDamage = item.getDurability();
		int requiredAmount = item.getAmount();
		
		int actualAmount = 0;
		for (ItemStack pstack : inventory.getContents())
		{
			if (pstack == null) continue;
			if (pstack.getType() != requiredType) continue;
			if (pstack.getDurability() != requiredDamage) continue;
			actualAmount += pstack.getAmount();
		}
		
		return actualAmount >= requiredAmount;
	}
	
	/**
	 * Removes an item from an inventory.
	 * 
	 * @param inventory The inventory to remove the item from
	 * @param item The item to remove
	 */
	private static void removeItem(PlayerInventory inventory, ItemStack item) {
		Material requiredType = item.getType();
		short requiredDamage = item.getDurability();
		int requiredAmount = item.getAmount();
		
		int remaining = requiredAmount;
		for (ItemStack pstack : inventory.getContents())
		{
			if (remaining <= 0) break;
			if (pstack == null) continue;
			if (pstack.getType() != requiredType) continue;
			if (pstack.getDurability() != requiredDamage) continue;
			
			int toRemove = Math.min(remaining, pstack.getAmount());
			pstack.setAmount(pstack.getAmount() - toRemove);
			remaining -= toRemove;
		}
	}
	
	/**
	 * Sends a message to a command sender.
	 * 
	 * @param sender The command sender to send the message to
	 * @param message The message to send
	 */
	public static void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(colorize(message));
	}
	
	/**
	 * Sends an error message to a command sender.
	 * 
	 * @param sender The command sender to send the error message to
	 * @param message The error message to send
	 */
	public static void sendError(CommandSender sender, String message) {
		sender.sendMessage(colorize("&c" + message));
	}
	
	/**
	 * Sends a success message to a command sender.
	 * 
	 * @param sender The command sender to send the success message to
	 * @param message The success message to send
	 */
	public static void sendSuccess(CommandSender sender, String message) {
		sender.sendMessage(colorize("&a" + message));
	}
	
	/**
	 * Sends an info message to a command sender.
	 * 
	 * @param sender The command sender to send the info message to
	 * @param message The info message to send
	 */
	public static void sendInfo(CommandSender sender, String message) {
		sender.sendMessage(colorize("&7" + message));
	}
	
	/**
	 * Sends a warning message to a command sender.
	 * 
	 * @param sender The command sender to send the warning message to
	 * @param message The warning message to send
	 */
	public static void sendWarning(CommandSender sender, String message) {
		sender.sendMessage(colorize("&e" + message));
	}
	
	/**
	 * Formats a blood amount.
	 * 
	 * @param amount The blood amount to format
	 * @return A formatted string representing the blood amount
	 */
	public static String formatBlood(double amount) {
		return BLOOD_FORMAT.format(amount);
	}
	
	/**
	 * Formats a percentage.
	 * 
	 * @param percentage The percentage to format
	 * @return A formatted string representing the percentage
	 */
	public static String formatPercentage(double percentage) {
		return PERCENTAGE_FORMAT.format(percentage);
	}
	
	/**
	 * Formats a time in seconds to a human-readable string.
	 * 
	 * @param seconds The time in seconds
	 * @return A formatted string representing the time
	 */
	public static String formatTime(long seconds) {
		if (seconds < 60) {
			return seconds + " seconds";
		}
		
		long minutes = seconds / 60;
		if (minutes < 60) {
			return minutes + " minutes";
		}
		
		long hours = minutes / 60;
		minutes = minutes % 60;
		return hours + " hours, " + minutes + " minutes";
	}
	
	/**
	 * Colorizes a string using Minecraft color codes.
	 * 
	 * @param text The text to colorize
	 * @return The colorized text
	 */
	public static String colorize(String text) {
		return ChatColor.translateAlternateColorCodes('&', text);
	}
	
	/**
	 * Broadcasts a message to all players.
	 * 
	 * @param message The message to broadcast
	 */
	public static void broadcastMessage(String message) {
		plugin.getServer().broadcastMessage(colorize(message));
	}
	
	/**
	 * Broadcasts an error message to all players.
	 * 
	 * @param message The error message to broadcast
	 */
	public static void broadcastError(String message) {
		plugin.getServer().broadcastMessage(colorize("&c" + message));
	}
	
	/**
	 * Broadcasts a success message to all players.
	 * 
	 * @param message The success message to broadcast
	 */
	public static void broadcastSuccess(String message) {
		plugin.getServer().broadcastMessage(colorize("&a" + message));
	}
	
	/**
	 * Broadcasts a warning message to all players.
	 * 
	 * @param message The warning message to broadcast
	 */
	public static void broadcastWarning(String message) {
		plugin.getServer().broadcastMessage(colorize("&e" + message));
	}
	
	/**
	 * Broadcasts an info message to all players.
	 * 
	 * @param message The info message to broadcast
	 */
	public static void broadcastInfo(String message) {
		plugin.getServer().broadcastMessage(colorize("&7" + message));
	}
	
	/**
	 * Gets a message from the language configuration.
	 * 
	 * @param key The message key
	 * @return The message, or the key if not found
	 */
	public static String getMessage(String key) {
		return plugin.getLanguageConfig().getMessage(key);
	}
	
	/**
	 * Gets a message from the language configuration with replacements.
	 * 
	 * @param key The message key
	 * @param args The arguments to replace in the message
	 * @return The message with replacements, or the key if not found
	 */
	public static String getMessage(String key, String... args) {
		return plugin.getLanguageConfig().getMessage(key, args);
	}
}
