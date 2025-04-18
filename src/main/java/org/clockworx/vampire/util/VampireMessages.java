package org.clockworx.vampire.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Centralized messaging system for the Vampire plugin.
 * This class handles all message output to players, other players, and the console.
 * It also supports localization through message keys.
 */
public class VampireMessages {
    
    private static VampirePlugin plugin;
    private static final Map<String, String> messageCache = new HashMap<>();
    
    /**
     * Initializes the messaging system.
     * 
     * @param plugin The VampirePlugin instance
     */
    public static void init(VampirePlugin plugin) {
        VampireMessages.plugin = plugin;
        loadMessages();
    }
    
    /**
     * Loads messages from the config.
     */
    private static void loadMessages() {
        // Load messages from config
        // This is a placeholder for actual implementation
        // In a real implementation, you would load messages from a config file
    }
    
    /**
     * Sends a message to a player.
     * 
     * @param player The player to send the message to
     * @param message The message to send
     */
    public static void send(Player player, String message) {
        if (player != null && player.isOnline()) {
            player.sendMessage(formatMessage(message));
        }
    }
    
    /**
     * Sends a message to a VampirePlayer.
     * 
     * @param vampirePlayer The VampirePlayer to send the message to
     * @param message The message to send
     */
    public static void send(VampirePlayer vampirePlayer, String message) {
        if (vampirePlayer != null) {
            Player player = vampirePlayer.getPlayer();
            if (player != null && player.isOnline()) {
                player.sendMessage(formatMessage(message));
            }
        }
    }
    
    /**
     * Sends a message to a CommandSender.
     * 
     * @param sender The CommandSender to send the message to
     * @param message The message to send
     */
    public static void send(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(formatMessage(message));
        }
    }
    
    /**
     * Sends a message to the console.
     * 
     * @param message The message to send
     */
    public static void sendToConsole(String message) {
        plugin.getLogger().info(formatMessage(message));
    }
    
    /**
     * Sends a message to all online players.
     * 
     * @param message The message to send
     */
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(formatMessage(message));
    }
    
    /**
     * Sends a message to a specific player from another player.
     * 
     * @param target The player to send the message to
     * @param sender The player sending the message
     * @param message The message to send
     */
    public static void sendFromPlayer(Player target, Player sender, String message) {
        if (target != null && target.isOnline() && sender != null) {
            target.sendMessage(formatMessage("&7From " + sender.getName() + ": &f" + message));
        }
    }
    
    /**
     * Sends a localized message to a player.
     * 
     * @param player The player to send the message to
     * @param key The message key
     * @param args The arguments to format the message with
     */
    public static void sendLocalized(Player player, String key, Object... args) {
        String message = getLocalizedMessage(key, args);
        send(player, message);
    }
    
    /**
     * Sends a localized message to a VampirePlayer.
     * 
     * @param vampirePlayer The VampirePlayer to send the message to
     * @param key The message key
     * @param args The arguments to format the message with
     */
    public static void sendLocalized(VampirePlayer vampirePlayer, String key, Object... args) {
        String message = getLocalizedMessage(key, args);
        send(vampirePlayer, message);
    }
    
    /**
     * Gets a localized message.
     * 
     * @param key The message key
     * @param args The arguments to format the message with
     * @return The localized message
     */
    public static String getLocalizedMessage(String key, Object... args) {
        String message = messageCache.get(key);
        if (message == null) {
            // If the message is not in the cache, use the key as the message
            message = key;
        }
        
        // Format the message with the arguments
        if (args != null && args.length > 0) {
            try {
                message = String.format(message, args);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Error formatting message: " + key, e);
            }
        }
        
        return message;
    }
    
    /**
     * Formats a message with color codes.
     * 
     * @param message The message to format
     * @return The formatted message
     */
    private static String formatMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Logs a debug message to the console.
     * 
     * @param message The message to log
     */
    public static void debug(String message) {
        if (plugin.getConfig().getBoolean("debug")) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }
    
    /**
     * Logs an error message to the console.
     * 
     * @param message The message to log
     * @param throwable The throwable to log
     */
    public static void error(String message, Throwable throwable) {
        plugin.getLogger().log(Level.SEVERE, message, throwable);
    }
} 