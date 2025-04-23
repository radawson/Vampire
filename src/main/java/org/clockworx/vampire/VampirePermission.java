package org.clockworx.vampire;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.clockworx.vampire.util.VampireMessages; // For sending no-permission messages

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all permission nodes for the Vampire plugin.
 * This utility class handles:
 * <ul>
 *     <li>Defining constant permission strings.</li>
 *     <li>Registering permissions with the Bukkit {@link PluginManager}.</li>
 *     <li>Setting up permission hierarchies (parent-child relationships).</li>
 *     <li>Providing helper methods for checking player permissions.</li>
 *     <li>Managing temporary permission attachments (though usage might be limited).</li>
 * </ul>
 * Permissions are registered during plugin startup via {@link #registerPermissions()}.
 * Permission checks should use the static {@code has()} methods.
 */
public class VampirePermission {
    
    // Prevent instantiation of utility class
    private VampirePermission() {}

    // --- Permission Node Constants ---
    // It's good practice to define permissions as constants for type safety and maintainability.

    /** Base permission for all general /vampire user commands. */
    public static final String BASECOMMAND = "vampire.base";
    /** Allows players to view their own vampire status (/vampire show). */
    public static final String SHOW = "vampire.show";
    /** Allows players/admins to view another player's vampire status (/vampire show [player]). */
    public static final String SHOW_OTHER = "vampire.show.other";
    /** Allows listing all online vampires and infected players (/vampire list). */
    public static final String LIST = "vampire.list";
    /** Allows vampires to use the shriek ability (/vampire shriek). */
    public static final String SHRIEK = "vampire.shriek";
    /** Allows viewing the plugin version (/vampire version). */
    public static final String VERSION = "vampire.version";
    /** Allows vampires to toggle bloodlust mode (/vampire mode bloodlust). */
    public static final String MODE_BLOODLUST = "vampire.mode.bloodlust";
    /** Allows vampires to toggle infection intent mode (/vampire mode intent). */
    public static final String MODE_INTENT = "vampire.mode.intent";
    /** Allows vampires to toggle night vision mode (/vampire mode nightvision). */
    public static final String MODE_NIGHTVISION = "vampire.mode.nightvision";
    /** Allows players to offer blood to others (/vampire trade offer). */
    public static final String TRADE_OFFER = "vampire.trade.offer";
    /** Allows players to accept blood offers (/vampire trade accept). */
    public static final String TRADE_ACCEPT = "vampire.trade.accept";
    /** Allows vampires to create blood flasks. */
    public static final String FLASK = "vampire.flask";
    /** Base permission for admin commands that modify player data (/vampire set ...). */
    public static final String SET = "vampire.set";
    /** Allows admins to turn a player into a vampire (/vampire set [player] vampire true). */
    public static final String SET_VAMPIRE_TRUE = "vampire.set.vampire.true";
    /** Allows admins to cure a player of vampirism (/vampire set [player] vampire false). */
    public static final String SET_VAMPIRE_FALSE = "vampire.set.vampire.false";
    /** Allows admins to set a player's infection level (/vampire set [player] infection [level]). */
    public static final String SET_INFECTION = "vampire.set.infection";
    /** Allows admins to set a player's blood/food level (/vampire set [player] food [level]). */
    public static final String SET_FOOD = "vampire.set.food";
    /** Allows admins to set a player's health level (/vampire set [player] health [level]). */
    public static final String SET_HEALTH = "vampire.set.health";
    /** Allows admins to modify the plugin's main configuration. */
    public static final String CONFIG = "vampire.config";
    /** Allows admins to modify the plugin's language configuration. */
    public static final String LANG = "vampire.lang";
    /** Allows players to use Dark Altars for rituals. */
    public static final String ALTAR_DARK = "vampire.altar.dark";
    /** Allows players to use Light Altars for rituals. */
    public static final String ALTAR_LIGHT = "vampire.altar.light";
    /** Allows viewing the list of commands via /vampire help. */
    public static final String HELP_COMMAND = "vampire.help.command";
    /** Allows viewing detailed explanations of mechanics via /vampire help <topic>. */
    public static final String HELP_LORE = "vampire.help.lore";
    /** Allows viewing general plugin information (/vampire info). */
    public static final String INFO = "vampire.info";
    /** Allows offering the Dark Gift to turn a human player into a vampire. */
    public static final String GIFT_OFFER = "vampire.gift.offer";
    /** Allows accepting or rejecting an offer of the Dark Gift. */
    public static final String GIFT_ACCEPT = "vampire.gift.accept";
    /** Allows viewing your own vampire statistics. */
    public static final String STATS = "vampire.stats";
    /** Allows viewing other players' vampire statistics. */
    public static final String STATS_OTHER = "vampire.stats.other";
    /** Allows resetting a player's vampire data (cure, remove infection/blood). */
    public static final String RESET = "vampire.reset";
    /** Allows creating Holy Water via command. */
    public static final String FLASK_HOLYWATER = "vampire.flask.holywater";
    
    /**
     * Stores registered permission objects (Node -> Permission). 
     * Used internally to manage parent-child relationships during registration. 
     */
    private static final Map<String, Permission> PERMISSIONS = new HashMap<>();
    
    /** 
     * Maps player UUIDs to their temporary permission attachments. 
     * Used by {@link #grantTemporaryPermission(Player, String)}, 
     * {@link #revokeTemporaryPermission(Player, String)}, and 
     * {@link #cleanupTemporaryPermissions(Player)}. 
     * Note: Careful management is needed, especially on player logout/plugin disable.
     */
    private static final Map<UUID, PermissionAttachment> ATTACHMENTS = new HashMap<>();
    
    /**
     * Registers all defined Vampire plugin permissions with the Bukkit {@link PluginManager}.
     * This method creates {@link Permission} objects for each constant defined above,
     * sets their descriptions and default values, establishes parent-child relationships
     * based on the node structure (e.g., `vampire.set.vampire.true` is a child of `vampire.set.vampire`),
     * and adds them to Bukkit's permission system.
     * 
     * Should be called once during plugin initialization (e.g., in `onEnable`).
     */
    public static void registerPermissions() {
        PluginManager pm = Bukkit.getPluginManager();
        if (pm == null) {
            // Should not happen in a normal Bukkit environment during onEnable
            VampireMessages.sendToConsole("&c[VampirePermission] Failed to get PluginManager! Permissions cannot be registered.");
            return;
        }
        
        // Clear any potentially stale data if re-registering (e.g., during a reload)
        PERMISSIONS.clear();
        // Note: ATTACHMENTS are usually cleared on player quit or plugin disable.

        // --- Register All Permissions ---
        // Structure: registerPermission(Node, Description, Default Access)

        // Base permissions (Default: TRUE = accessible by default)
        registerPermission(pm, BASECOMMAND, "Base permission for all general vampire commands", PermissionDefault.TRUE);
        registerPermission(pm, SHOW, "Allows viewing own vampire status", PermissionDefault.TRUE);
        registerPermission(pm, LIST, "Allows listing online vampires/infected", PermissionDefault.OP);
        registerPermission(pm, SHRIEK, "Allows vampires to use shriek ability", PermissionDefault.TRUE);
        registerPermission(pm, VERSION, "Allows viewing plugin version", PermissionDefault.TRUE);
        
        // Mode permissions (Default: TRUE = accessible by default, logic checks if player is vampire)
        registerPermission(pm, MODE_BLOODLUST, "Allows toggling bloodlust mode", PermissionDefault.TRUE);
        registerPermission(pm, MODE_INTENT, "Allows toggling infection intent mode", PermissionDefault.TRUE);
        registerPermission(pm, MODE_NIGHTVISION, "Allows toggling night vision mode", PermissionDefault.TRUE);
        
        // Trade permissions (Default: TRUE)
        registerPermission(pm, TRADE_OFFER, "Allows offering blood to others", PermissionDefault.TRUE);
        registerPermission(pm, TRADE_ACCEPT, "Allows accepting blood offers", PermissionDefault.TRUE);
        registerPermission(pm, FLASK, "Allows creating blood flasks", PermissionDefault.TRUE);
        
        // Admin set permissions (Default: OP = accessible only by server operators or explicitly granted)
        registerPermission(pm, SET, "Base permission for admin set commands", PermissionDefault.OP);
        registerPermission(pm, SET_VAMPIRE_TRUE, "Allows setting a player as a vampire", PermissionDefault.OP);
        registerPermission(pm, SET_VAMPIRE_FALSE, "Allows curing a player of vampirism", PermissionDefault.OP);
        registerPermission(pm, SET_INFECTION, "Allows setting player infection level", PermissionDefault.OP);
        registerPermission(pm, SET_FOOD, "Allows setting player blood/food level", PermissionDefault.OP);
        registerPermission(pm, SET_HEALTH, "Allows setting player health level", PermissionDefault.OP);

        // Config permissions (Default: OP)
        registerPermission(pm, CONFIG, "Allows editing plugin configuration", PermissionDefault.OP);
        registerPermission(pm, LANG, "Allows editing plugin language files", PermissionDefault.OP);
        
        // Altar permissions (Default: TRUE)
        registerPermission(pm, ALTAR_DARK, "Allows using Dark Altars", PermissionDefault.TRUE);
        registerPermission(pm, ALTAR_LIGHT, "Allows using Light Altars", PermissionDefault.TRUE);

        // Help permissions
        registerPermission(pm, HELP_COMMAND, "Allows viewing the command list (/vampire help)", PermissionDefault.TRUE);
        registerPermission(pm, HELP_LORE, "Allows viewing detailed help/lore (/vampire help <topic>)", PermissionDefault.OP); // Often lore is admin-only?
        registerPermission(pm, INFO, "Allows viewing general plugin information", PermissionDefault.TRUE); // New info command permission

        // Gift permissions
        registerPermission(pm, GIFT_OFFER, "Allows offering the Dark Gift to turn players", PermissionDefault.OP); // Typically restricted
        registerPermission(pm, GIFT_ACCEPT, "Allows accepting/rejecting the Dark Gift", PermissionDefault.TRUE);

        // Stats/Reset permissions
        registerPermission(pm, INFO, "Allows viewing Vampire plugin information", PermissionDefault.OP);
        registerPermission(pm, STATS, "Allows viewing own vampire statistics", PermissionDefault.TRUE);
        registerPermission(pm, STATS_OTHER, "Allows viewing other players' vampire statistics", PermissionDefault.OP);
        registerPermission(pm, RESET, "Allows resetting player vampire data (admin)", PermissionDefault.OP);
        registerPermission(pm, FLASK_HOLYWATER, "Allows creating Holy Water (admin command)", PermissionDefault.OP);

        // --- Setup Parent-Child Relationships ---
        // Automatically done by registerPermission based on node structure.
        // Example: SET_VAMPIRE_TRUE gets SET as a parent implicitly.
        // We also need SHOW_OTHER -> SHOW
        linkParentChild(pm, SHOW, SHOW_OTHER);
        linkParentChild(pm, STATS, STATS_OTHER);
        // Ensure all SET_* have SET as parent implicitly
        // Ensure all MODE_* have BASECOMMAND as parent implicitly? (or keep separate)

        VampireMessages.sendToConsole("&a[VampirePermission] Successfully registered permissions.");
    }
    
    /**
     * Registers a single permission node with the Bukkit PluginManager.
     * Creates the Permission object, adds it to the internal map, sets up
     * parent-child relationships based on the node string, and registers
     * it with Bukkit.
     * 
     * @param pm The Bukkit {@link PluginManager}.
     * @param node The permission node string (e.g., "vampire.show.other").
     * @param description A human-readable description of the permission.
     * @param defaultValue The default access level (TRUE, FALSE, OP, NOT_OP).
     */
    private static void registerPermission(PluginManager pm, String node, String description, PermissionDefault defaultValue) {
        // Avoid registering duplicates if called multiple times
        if (PERMISSIONS.containsKey(node) || pm.getPermission(node) != null) {
            return;
        }

        Permission permission = new Permission(node, description, defaultValue);
        
        // Automatically determine and add parent permission if applicable.
        // Example: node "vampire.set.vampire.true"
        int lastDot = node.lastIndexOf('.');
        if (lastDot > 0) {
            String parentNode = node.substring(0, lastDot); // "vampire.set.vampire"
            Permission parentPerm = PERMISSIONS.get(parentNode); // Check our internal map first
            if (parentPerm == null) {
                 parentPerm = pm.getPermission(parentNode); // Check Bukkit's map if not in ours yet
            }
            if (parentPerm != null) {
                // Add parent-child relationship
                permission.addParent(parentPerm, true);
            } else {
                 // Optional: Log a warning if a potential parent node doesn't exist
                 // VampireMessages.debug("[VampirePermission] Parent node '" + parentNode + "' not found for '" + node + "'.");
            }
        }
        
        // Register the permission with Bukkit
        pm.addPermission(permission);
        // Store it in our map for potential future parent lookups
        PERMISSIONS.put(node, permission);
    }
    
    /**
     * Explicitly links a child permission node to a parent node.
     * Useful when the automatic dot-based hierarchy isn't sufficient or desired.
     * 
     * @param pm The Bukkit {@link PluginManager}.
     * @param parentNode The permission node string of the parent.
     * @param childNode The permission node string of the child.
     */
    private static void linkParentChild(PluginManager pm, String parentNode, String childNode) {
        Permission parentPerm = pm.getPermission(parentNode);
        Permission childPerm = pm.getPermission(childNode);

        if (parentPerm != null && childPerm != null) {
            try {
                childPerm.addParent(parentPerm, true);
            } catch (IllegalArgumentException e) {
                 // Can happen if already linked, or circular dependency (shouldn't occur here)
                 VampireMessages.debug("[VampirePermission] Could not link parent '" + parentNode + "' to child '" + childNode + "': " + e.getMessage());
            }
        } else {
            if (parentPerm == null) VampireMessages.debug("[VampirePermission] Parent node '" + parentNode + "' not found for explicit linking.");
            if (childPerm == null) VampireMessages.debug("[VampirePermission] Child node '" + childNode + "' not found for explicit linking.");
        }
    }
    
    /**
     * Checks if a {@link CommandSender} (Player or Console) has a specific permission node.
     * 
     * @param sender The CommandSender to check.
     * @param permission The permission node string.
     * @return {@code true} if the sender has the permission, {@code false} otherwise.
     */
    public static boolean has(CommandSender sender, String permission) {
        // Use the standard Bukkit permission check.
        return sender.hasPermission(permission);
    }
    
    /**
     * Checks if a {@link CommandSender} has a specific permission node.
     * If the sender lacks the permission, sends them a specified (non-localized) message.
     * 
     * @param sender The CommandSender to check.
     * @param permission The permission node string.
     * @param noPermissionMessage The message to send if permission is denied (should ideally be a localized key).
     * @return {@code true} if the sender has the permission, {@code false} otherwise.
     * @deprecated Sending raw messages bypasses localization. Use the boolean overload or check permission and send localized message separately.
     */
    @Deprecated
    public static boolean has(CommandSender sender, String permission, String noPermissionMessage) {
        if (!has(sender, permission)) {
            // Directly sending a message here avoids localization. 
            // Better practice: Return false and let the caller handle sending a localized message.
            VampireMessages.send(sender, noPermissionMessage); // Use VampireMessages for consistency
            return false;
        }
        return true;
    }
    
    /**
     * Checks if a {@link CommandSender} has a specific permission node.
     * If the sender lacks the permission and {@code sendMessage} is true, sends a 
     * default (non-localized) no-permission message.
     * 
     * @param sender The CommandSender to check.
     * @param permission The permission node string.
     * @param sendMessage If true, send a default no-permission message on failure.
     * @return {@code true} if the sender has the permission, {@code false} otherwise.
     */
    public static boolean has(CommandSender sender, String permission, boolean sendMessage) {
        if (!has(sender, permission)) {
            if (sendMessage) {
                // Send a default, non-localized message. Consider using a key like "error.no_permission"
                VampireMessages.send(sender, "&cYou don't have permission to use this command."); 
            }
            return false;
        }
        return true;
    }

    /**
     * Temporarily grants a permission node to a player using a {@link PermissionAttachment}.
     * The attachment is stored and should be removed when the player logs out or the 
     * temporary permission is no longer needed using {@link #cleanupTemporaryPermissions(Player)} 
     * or {@link #revokeTemporaryPermission(Player, String)}.
     * 
     * Note: Requires a valid {@link Plugin} instance to create the attachment.
     * 
     * @param player The player to grant the permission to.
     * @param permission The permission node string to grant.
     * @return {@code true} if the permission was granted successfully, {@code false} if the plugin instance is missing.
     */
    public static boolean grantTemporaryPermission(Player player, String permission) {
        UUID uuid = player.getUniqueId();
        // Retrieve the plugin instance associated with this plugin's code.
        // Assumes this class is part of the "Vampire" plugin.
        Plugin pluginInstance = Bukkit.getPluginManager().getPlugin("Vampire"); 
        if (pluginInstance == null) {
             VampireMessages.sendToConsole("&c[VampirePermission] Cannot grant temporary permission: Plugin instance is null!");
             return false; 
        }

        // Get existing attachment or create a new one.
        PermissionAttachment attachment = ATTACHMENTS.computeIfAbsent(uuid, k -> player.addAttachment(pluginInstance));
        
        // Set the permission value to true on the attachment.
        attachment.setPermission(permission, true);
        // Optional: Recalculate player permissions immediately
        // player.recalculatePermissions(); 
        return true;
    }

    /**
     * Revokes a previously granted temporary permission from a player's {@link PermissionAttachment}.
     * 
     * @param player The player to revoke the permission from.
     * @param permission The permission node string to revoke (unset).
     * @return {@code true} if the permission was found on an attachment and unset, {@code false} otherwise.
     */
    public static boolean revokeTemporaryPermission(Player player, String permission) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment attachment = ATTACHMENTS.get(uuid);
        
        if (attachment != null) {
            attachment.unsetPermission(permission);
            // Optional: If the attachment becomes empty, consider removing it entirely.
            // if (attachment.getPermissions().isEmpty()) {
            //     try {
            //         player.removeAttachment(attachment);
            //     } catch (IllegalArgumentException e) { /* Already removed */ }
            //     ATTACHMENTS.remove(uuid);
            // }
            // Optional: Recalculate player permissions immediately
            // player.recalculatePermissions(); 
            return true;
        }
        
        return false; // No attachment found for this player
    }

    /**
     * Removes and cleans up the {@link PermissionAttachment} associated with a player.
     * This should typically be called when a player logs out to prevent memory leaks and 
     * ensure temporary permissions don't persist unexpectedly.
     * 
     * @param player The player whose temporary permissions should be cleaned up.
     */
    public static void cleanupTemporaryPermissions(Player player) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment attachment = ATTACHMENTS.remove(uuid);
        
        if (attachment != null) {
            // Safely attempt to remove the attachment from the player.
            try {
                player.removeAttachment(attachment);
            } catch (IllegalArgumentException e) {
                 // Attachment might have already been removed (e.g., by another plugin or server mechanism)
                 // Log if this case needs investigation:
                 // VampireMessages.debug("[VampirePermission] Error removing attachment for " + player.getName() + ": " + e.getMessage());
            }
        }
    }
} 