package org.clockworx.vampire.util;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.config.LanguageConfig;
import org.clockworx.vampire.config.VampireConfig;
import org.clockworx.vampire.entity.VampirePlayer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Centralized utility for handling localized messages within the Vampire plugin.
 * Provides static methods to send messages to various recipients (Players, Console, Broadcast)
 * using keys defined in the language configuration file.
 * 
 * <p>Ensures consistent message formatting (color codes) and facilitates easy localization 
 * by loading messages from {@link LanguageConfig}.</p>
 * 
 * <p>Usage: Call {@link #init(VampirePlugin)} during plugin startup, then use the static 
 * {@code send()}, {@code sendLocalized()}, {@code broadcast()}, etc. methods.</p>
 */
public class VampireMessages {
    
    /** The VampirePlugin instance, used for accessing config, logger, and server resources. */
    private static VampirePlugin plugin;
    /** Cache holding the loaded messages from the language file (Key -> Message String). */
    private static Map<String, String> messageCache = new HashMap<>();
    
    /** Private constructor to prevent instantiation of utility class. */
    private VampireMessages() {}
    
    /**
     * Initializes the messaging system with the main plugin instance.
     * Loads the messages from the configured language file into the cache.
     * This must be called once during plugin startup (e.g., in onEnable).
     * 
     * @param plugin The {@link VampirePlugin} instance.
     */
    public static void init(VampirePlugin plugin) {
        VampireMessages.plugin = plugin;
        loadMessages();
    }
    
    /**
     * Loads messages from the plugin's {@link LanguageConfig} into the internal cache.
     * Clears any existing messages before loading.
     * Logs the number of messages loaded or a warning if the config is unavailable.
     */
    private static void loadMessages() {
        messageCache.clear();
        LanguageConfig langConfig = plugin.getLanguageConfig();
        if (langConfig != null) {
            // Get all messages from the language config and store them in the cache.
            messageCache.putAll(langConfig.getMessages());
            plugin.getLogger().info("Loaded " + messageCache.size() + " messages from language config.");
        } else {
            // This should generally not happen if init is called after config loading.
            plugin.getLogger().warning("LanguageConfig not available during VampireMessages initialization. Messages may not work correctly.");
        }
    }
    
    /**
     * Reloads messages from the language configuration file.
     * Clears the existing cache and loads the messages again.
     * Useful for applying changes to the language file without restarting the server.
     */
    public static void reloadMessages() {
        loadMessages();
        plugin.getLogger().info("Reloaded messages from language config.");
    }
    
    /**
     * Sends a raw message string (after formatting color codes) to a specific player.
     * 
     * @param player The {@link Player} to send the message to. Can be null (message is ignored).
     * @param message The raw message string (use '&' for color codes).
     */
    public static void send(Player player, String message) {
        if (player != null && player.isOnline()) {
            player.sendMessage(formatMessage(message));
        }
    }
    
    /**
     * Sends a raw message string (after formatting color codes) to the player 
     * associated with a {@link VampirePlayer} object.
     * 
     * @param vampirePlayer The {@link VampirePlayer} whose associated player should receive the message. Can be null.
     * @param message The raw message string (use '&' for color codes).
     */
    public static void send(VampirePlayer vampirePlayer, String message) {
        if (vampirePlayer != null) {
            Player player = vampirePlayer.getPlayer(); // Get the underlying Bukkit Player
            // Send the message using the player-specific method.
            send(player, message); 
        }
    }
    
    /**
     * Sends a raw message string (after formatting color codes) to a {@link CommandSender} 
     * (can be a Player or the Console).
     * 
     * @param sender The {@link CommandSender} to send the message to. Can be null (message is ignored).
     * @param message The raw message string (use '&' for color codes).
     */
    public static void send(CommandSender sender, String message) {
        if (sender != null) {
            sender.sendMessage(formatMessage(message));
        }
    }
    
    /**
     * Sends a message to the server console (logs it as INFO level).
     * The message is formatted for color codes, though console may not display them.
     * 
     * @param message The message string to log.
     */
    public static void sendToConsole(String message) {
        // Logs the message using the plugin's logger.
        plugin.getLogger().info(formatMessage(message));
    }
    
    /**
     * Broadcasts a raw message string (after formatting color codes) to all online players.
     * 
     * @param message The raw message string to broadcast (use '&' for color codes).
     */
    public static void broadcast(String message) {
        // Uses Bukkit's broadcast method.
        Bukkit.broadcastMessage(formatMessage(message));
    }
    
    /**
     * Sends a message to a target player, indicating it came from another player.
     * Prepends "From [SenderName]: " to the message.
     * 
     * @param target The {@link Player} to receive the message. Can be null.
     * @param sender The {@link Player} who sent the message. Can be null.
     * @param message The content of the message.
     */
    public static void sendFromPlayer(Player target, Player sender, String message) {
        // Ensure both players are valid and online.
        if (target != null && target.isOnline() && sender != null) {
            // Format the message with sender context.
            String formatted = "&7From " + sender.getName() + ": &f" + message;
            send(target, formatted);
        }
    }
    
    /**
     * Sends a localized message to a player using a key from the language file.
     * Formats the message using provided arguments if any.
     * 
     * @param player The {@link Player} to send the message to. Can be null.
     * @param key The key corresponding to the message in the language file.
     * @param args Optional arguments to be inserted into the message (using {@link String#format} placeholders like %s, %d).
     */
    public static void sendLocalized(Player player, String key, Object... args) {
        String message = getLocalizedMessage(key, args);
        send(player, message);
    }
    
    /**
     * Sends a localized message to the player associated with a {@link VampirePlayer} object.
     * Uses a key from the language file and formats with provided arguments.
     * 
     * @param vampirePlayer The {@link VampirePlayer} whose associated player should receive the message. Can be null.
     * @param key The key corresponding to the message in the language file.
     * @param args Optional arguments to be inserted into the message.
     */
    public static void sendLocalized(VampirePlayer vampirePlayer, String key, Object... args) {
        String message = getLocalizedMessage(key, args);
        send(vampirePlayer, message);
    }
    
    /**
     * Sends a localized message to a {@link CommandSender} (Player or Console).
     * Uses a key from the language file and formats with provided arguments.
     * 
     * @param sender The {@link CommandSender} to send the message to. Can be null.
     * @param key The key corresponding to the message in the language file.
     * @param args Optional arguments to be inserted into the message.
     */
    public static void sendLocalized(CommandSender sender, String key, Object... args) {
        String message = getLocalizedMessage(key, args);
        send(sender, message);
    }
    
    /**
     * Retrieves and formats a localized message string from the cache.
     * If the key is not found, returns a default error message indicating the missing key.
     * If arguments are provided, uses {@link String#format} to insert them.
     * Finally, formats the message for color codes.
     * 
     * @param key The key corresponding to the message in the language file.
     * @param args Optional arguments for formatting the message.
     * @return The fully formatted, localized message string.
     */
    public static String getLocalizedMessage(String key, Object... args) {
        // Retrieve the raw message from cache, or provide a default if missing.
        String rawMessage = messageCache.getOrDefault(key, "&cMissing message: " + key);
        String formattedMessage = rawMessage;
        
        // If arguments are provided, attempt to format the message.
        if (args != null && args.length > 0) {
            try {
                // Use String.format for placeholder replacement (e.g., %s, %d).
                formattedMessage = String.format(rawMessage, args);
            } catch (Exception e) {
                // Log a warning if formatting fails (e.g., wrong number/type of args).
                plugin.getLogger().log(Level.WARNING, "Error formatting message key '" + key + "' with String.format (Check arguments and format specifiers): " + e.getMessage());
                // Fallback to the raw message if formatting fails.
                formattedMessage = rawMessage;
            }
        }
        
        // Always apply color code formatting at the end.
        return formatMessage(formattedMessage);
    }
    
    /**
     * Translates Minecraft color codes (using '&') within a string to actual colors.
     * Internal helper method.
     * 
     * @param message The message string potentially containing '&' color codes.
     * @return The message string with color codes translated.
     */
    public static String formatMessage(String message) {
        // Use Adventure API to parse legacy codes (&) and serialize back to legacy format (§)
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
    
    /**
     * Logs a debug message to the console if debug mode is enabled in the plugin config.
     * Prefixes the message with "[DEBUG] ".
     * 
     * @param message The debug message to log.
     */
    public static void debug(String message) {
        // Default to not logging if plugin or config is unavailable
        boolean shouldLog = false;
        if (plugin != null) { // Check if VampireMessages.init() has been called
            VampireConfig config = plugin.getVampireConfig();
            // Check if config object exists AND its debug flag is true
            if (config != null && config.isDebug()) {
                shouldLog = true;
            }
        }

        if (shouldLog) {
            // We already know plugin is not null if shouldLog is true
            plugin.getLogger().info("[DEBUG] " + message);
        }
        // If shouldLog is false (due to plugin being null, config being null,
        // or debug being false), nothing is logged.
    }
    
    /**
     * Logs an error message and stack trace to the console at SEVERE level.
     * Use this for critical errors or exceptions.
     * 
     * @param message The error message to log.
     * @param throwable The associated {@link Throwable} (exception/error) to log. Can be null.
     */
    public static void error(String message, Throwable throwable) {
        plugin.getLogger().log(Level.SEVERE, message, throwable);
    }
} 