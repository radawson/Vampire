package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.ResourceUtil;
import org.clockworx.vampire.util.VampireMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Base class for all Vampire plugin commands.
 * Provides common functionality and structure for command handling.
 * 
 * <p>This abstract class implements both CommandExecutor and TabCompleter interfaces,
 * providing a unified approach to command handling in the Vampire plugin.</p>
 * 
 * <p>All command classes in the plugin should extend this class rather than implementing
 * CommandExecutor directly. This ensures consistent behavior and reduces code duplication.</p>
 * 
 * <p>The class handles permission checking, command execution, and tab completion in a
 * standardized way, while allowing subclasses to focus on their specific functionality.</p>
 */
public abstract class VCommand implements CommandExecutor, TabCompleter {
    
    /**
     * The plugin instance that this command belongs to.
     */
    protected final VampirePlugin plugin;
    
    /**
     * The name of the command (subcommand name).
     */
    protected final String name;
    
    /**
     * The permission required to use this command.
     */
    protected final String permission;
    
    /**
     * A brief description of what the command does.
     */
    protected final String description;

    /**
     * Whether the command is toggleable.
     */
    protected final boolean toggleable;

    /**
     * A string indicating the command's arguments/usage pattern.
     * Example: "<player> <value>"
     */
    protected final String usage;
    
    /**
     * Creates a new command, defaulting toggleable to false.
     * 
     * @param plugin The plugin instance
     * @param name The command name
     * @param permission The permission required to use the command
     * @param description A brief description of the command.
     * @param usage A string indicating the command's arguments/usage pattern (e.g., "<player> <value>"). Leave empty ("") if no arguments.
     */
    public VCommand(VampirePlugin plugin, String name, String permission, String description, String usage) {
        this(plugin, name, permission, description, usage, false);
    }

    /**
     * Creates a new command with an explicit toggleable setting.
     * 
     * @param plugin The plugin instance
     * @param name The command name
     * @param permission The permission required to use the command
     * @param description A brief description of the command.
     * @param usage A string indicating the command's arguments/usage pattern.
     * @param toggleable Whether this command represents a toggleable state.
     */
    public VCommand(VampirePlugin plugin, String name, String permission, String description, String usage, boolean toggleable) {
        this.plugin = plugin;
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.toggleable = toggleable;
        this.usage = (usage != null) ? usage : "";
    }
    
    /**
     * Gets the command name.
     * 
     * @return The command name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the command permission.
     * 
     * @return The command permission
     */
    public String getPermission() {
        return permission;
    }
    
    /**
     * Gets the command description.
     * 
     * @return The command description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the command usage string (arguments part).
     * 
     * @return The command usage string.
     */
    public String getUsage() {
        return usage;
    }
    
    /**
     * Checks if this command is marked as toggleable.
     * 
     * @return true if the command is toggleable, false otherwise.
     */
    public boolean isToggleable() {
        return toggleable;
    }
    
    /**
     * Executes the command.
     * This method is called by the Bukkit command system and delegates to the
     * abstract execute method after checking permissions.
     * 
     * @param sender The command sender
     * @param command The command
     * @param label The command label
     * @param args The command arguments
     * @return true if the command was executed successfully
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(VampireMessages.getLocalizedMessage("no-permission"));
            return true;
        }
        return execute(sender, command, label, args);
    }
    
    /**
     * Abstract method that must be implemented by all commands.
     * This is where the actual command logic should be implemented.
     * 
     * @param sender The command sender
     * @param command The command
     * @param label The command label
     * @param args The command arguments
     * @return true if the command was executed successfully
     */
    protected abstract boolean execute(CommandSender sender, Command command, String label, String[] args);
    
    /**
     * Provides tab completion for the command.
     * This method is called by the Bukkit command system.
     * 
     * @param sender The command sender
     * @param command The command
     * @param label The command label
     * @param args The command arguments
     * @return A list of possible completions
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(permission)) {
            return new ArrayList<>();
        }
        return tabComplete(sender, command, label, args);
    }
    
    /**
     * Abstract method that must be implemented by all commands for tab completion.
     * 
     * @param sender The command sender
     * @param command The command
     * @param label The command label
     * @param args The command arguments
     * @return A list of possible completions
     */
    protected abstract List<String> tabComplete(CommandSender sender, Command command, String label, String[] args);
    
    /**
     * Checks if the sender is a player.
     * 
     * @param sender The command sender
     * @return true if the sender is a player, false otherwise
     */
    protected boolean isPlayer(CommandSender sender) {
        return sender instanceof org.bukkit.entity.Player;
    }
    
    /**
     * Gets the player from the command sender.
     * 
     * @param sender The command sender
     * @return The player, or null if the sender is not a player
     */
    protected org.bukkit.entity.Player getPlayer(CommandSender sender) {
        return sender instanceof org.bukkit.entity.Player ? (org.bukkit.entity.Player) sender : null;
    }
    
    /**
     * Sends an error message to the sender.
     * 
     * @param sender The command sender
     * @param message The error message
     */
    protected void sendError(CommandSender sender, String message) {
        // Use VampireMessages directly. Note: message might already be localized.
        // For consistent prefixing/coloring, localization keys are preferred.
        VampireMessages.send(sender, message); // Assuming message includes necessary color codes
    }
    
    /**
     * Sends a success message to the sender.
     * 
     * @param sender The command sender
     * @param message The success message
     */
    protected void sendSuccess(CommandSender sender, String message) {
        // Use VampireMessages directly.
        VampireMessages.send(sender, message);
    }
    
    /**
     * Sends an info message to the sender.
     * 
     * @param sender The command sender
     * @param message The info message
     */
    protected void sendInfo(CommandSender sender, String message) {
        // Use VampireMessages directly.
        VampireMessages.send(sender, message);
    }
    
    /**
     * Gets a message from the language file.
     * 
     * @param key The message key
     * @return The message
     */
    protected String getMessage(String key) {
        return VampireMessages.getLocalizedMessage(key);
    }
    
    /**
     * Result class for player target resolution.
     * Contains all information needed about a resolved target player.
     */
    protected static class TargetResolution {
        /** The UUID of the target player. */
        final UUID uuid;
        /** The name of the target player. */
        final String name;
        /** The online Player object, or null if offline. */
        final org.bukkit.entity.Player onlinePlayer;
        /** The OfflinePlayer object, or null if online. */
        final OfflinePlayer offlinePlayer;
        /** Whether the target is the command sender themselves. */
        final boolean isSelf;
        
        /**
         * Creates a new TargetResolution.
         * 
         * @param uuid The player's UUID
         * @param name The player's name
         * @param onlinePlayer The online Player object (null if offline)
         * @param offlinePlayer The OfflinePlayer object (null if online)
         * @param isSelf Whether this is the command sender
         */
        TargetResolution(UUID uuid, String name, org.bukkit.entity.Player onlinePlayer, 
                         OfflinePlayer offlinePlayer, boolean isSelf) {
            this.uuid = uuid;
            this.name = name;
            this.onlinePlayer = onlinePlayer;
            this.offlinePlayer = offlinePlayer;
            this.isSelf = isSelf;
        }
    }
    
    /**
     * Resolves the target player from command arguments.
     * If a player name is provided in args[playerArgIndex], returns that player.
     * If no name is provided (playerArgIndex < 0 or args.length <= playerArgIndex), 
     * returns the sender if they are a player.
     * 
     * @param sender The command sender
     * @param args Command arguments
     * @param playerArgIndex Index in args array where player name might be (or -1 if not present)
     * @param allowOffline If true, allows offline players; if false, requires online
     * @return TargetResolution containing player info, or null if resolution failed (error message sent)
     */
    protected TargetResolution resolveTargetPlayer(CommandSender sender, String[] args, int playerArgIndex, boolean allowOffline) {
        String playerName = null;
        
        // Check if player name is provided in args
        if (playerArgIndex >= 0 && playerArgIndex < args.length && args[playerArgIndex] != null && !args[playerArgIndex].isEmpty()) {
            playerName = args[playerArgIndex];
        }
        
        // If player name provided, look up that player
        if (playerName != null) {
            org.bukkit.entity.Player onlinePlayer = Bukkit.getPlayer(playerName);
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                // Player is online
                return new TargetResolution(
                    onlinePlayer.getUniqueId(),
                    onlinePlayer.getName(),
                    onlinePlayer,
                    null,
                    sender instanceof org.bukkit.entity.Player && ((org.bukkit.entity.Player) sender).getUniqueId().equals(onlinePlayer.getUniqueId())
                );
            } else if (allowOffline) {
                // Try offline player
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                if (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) {
                    return new TargetResolution(
                        offlinePlayer.getUniqueId(),
                        offlinePlayer.getName() != null ? offlinePlayer.getName() : playerName,
                        null,
                        offlinePlayer,
                        sender instanceof org.bukkit.entity.Player && ((org.bukkit.entity.Player) sender).getUniqueId().equals(offlinePlayer.getUniqueId())
                    );
                } else {
                    VampireMessages.sendLocalized(sender, "player.not_found", playerName);
                    return null;
                }
            } else {
                // Online required but player not online
                VampireMessages.sendLocalized(sender, "player.not_online", playerName);
                return null;
            }
        }
        
        // No player name provided - use sender if they are a player
        if (sender instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) sender;
            return new TargetResolution(
                player.getUniqueId(),
                player.getName(),
                player,
                null,
                true
            );
        } else {
            // Sender is not a player and no player name provided
            VampireMessages.sendLocalized(sender, "command.error.must_be_player_or_specify");
            return null;
        }
    }
    
    /**
     * Simple case: Resolves target player when player name is at args[0] or sender is target.
     * 
     * @param sender The command sender
     * @param args Command arguments (player name may be at index 0)
     * @param allowOffline If true, allows offline players; if false, requires online
     * @return TargetResolution containing player info, or null if resolution failed (error message sent)
     */
    protected TargetResolution resolveTargetPlayer(CommandSender sender, String[] args, boolean allowOffline) {
        return resolveTargetPlayer(sender, args, args.length > 0 ? 0 : -1, allowOffline);
    }
} 