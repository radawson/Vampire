package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.ResourceUtil;
import org.clockworx.vampire.util.VampireMessages;
import java.util.ArrayList;
import java.util.List;

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
     * A string indicating the command's arguments/usage pattern.
     * Example: "<player> <value>"
     */
    protected final String usage;
    
    /**
     * Creates a new command.
     * 
     * @param plugin The plugin instance
     * @param name The command name
     * @param permission The permission required to use the command
     * @param description A brief description of the command.
     * @param usage A string indicating the command's arguments/usage pattern (e.g., "<player> <value>"). Leave empty ("") if no arguments.
     */
    public VCommand(VampirePlugin plugin, String name, String permission, String description, String usage) {
        this.plugin = plugin;
        this.name = name;
        this.permission = permission;
        this.description = description;
        this.usage = (usage != null) ? usage : ""; // Ensure usage is not null
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
} 