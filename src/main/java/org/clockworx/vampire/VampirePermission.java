package org.clockworx.vampire;

import io.papermc.paper.plugin.PaperPlugin;
import io.papermc.paper.plugin.PluginManager;
import io.papermc.paper.plugin.PluginManagerProvider;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Permission management system for the Vampire plugin.
 * This class handles all permission-related operations including:
 * - Registration of permission nodes
 * - Permission checking
 * - Temporary permission management
 * - Permission hierarchy management
 * 
 * The class uses Paper's permission system and provides a robust interface
 * for managing plugin permissions. It supports:
 * - Hierarchical permissions (parent-child relationships)
 * - Temporary permission grants
 * - Permission cleanup
 * - Default permission values
 * 
 * @author Clockworx
 * @version 3.0
 */
public class VampirePermission {
    
    /**
     * Permission nodes for the plugin.
     * These constants define all available permission nodes that can be
     * assigned to players or groups.
     */
    public static final String BASECOMMAND = "vampire.base";
    public static final String SHOW = "vampire.show";
    public static final String SHOW_OTHER = "vampire.show.other";
    public static final String LIST = "vampire.list";
    public static final String SHRIEK = "vampire.shriek";
    public static final String VERSION = "vampire.version";
    public static final String MODE_BLOODLUST = "vampire.mode.bloodlust";
    public static final String MODE_INTENT = "vampire.mode.intent";
    public static final String MODE_NIGHTVISION = "vampire.mode.nightvision";
    public static final String TRADE_OFFER = "vampire.trade.offer";
    public static final String TRADE_ACCEPT = "vampire.trade.accept";
    public static final String FLASK = "vampire.flask";
    public static final String SET = "vampire.set";
    public static final String SET_VAMPIRE_TRUE = "vampire.set.vampire.true";
    public static final String SET_VAMPIRE_FALSE = "vampire.set.vampire.false";
    public static final String SET_INFECTION = "vampire.set.infection";
    public static final String SET_FOOD = "vampire.set.food";
    public static final String SET_HEALTH = "vampire.set.health";
    public static final String CONFIG = "vampire.config";
    public static final String LANG = "vampire.lang";
    public static final String ALTAR_DARK = "vampire.altar.dark";
    public static final String ALTAR_LIGHT = "vampire.altar.light";
    
    /**
     * Maps to store registered permissions and temporary permission attachments.
     * These maps are used to track and manage permissions throughout the plugin's lifecycle.
     */
    private static final Map<String, Permission> PERMISSIONS = new HashMap<>();
    private static final Map<UUID, PermissionAttachment> ATTACHMENTS = new HashMap<>();
    
    /**
     * Registers all permissions with the Paper permission system.
     * This method sets up the permission hierarchy and default values for all
     * plugin permissions. It should be called during plugin initialization.
     * 
     * The registration process:
     * 1. Creates permission objects with descriptions and default values
     * 2. Establishes parent-child relationships between permissions
     * 3. Registers permissions with the Paper permission system
     */
    public static void registerPermissions() {
        // Base permissions
        registerPermission(BASECOMMAND, "Base permission for all vampire commands", PermissionDefault.TRUE);
        registerPermission(SHOW, "Permission to use the show command", PermissionDefault.TRUE);
        registerPermission(SHOW_OTHER, "Permission to show other players' vampire status", PermissionDefault.OP);
        registerPermission(LIST, "Permission to list vampires and infected players", PermissionDefault.OP);
        registerPermission(SHRIEK, "Permission to use the shriek command", PermissionDefault.TRUE);
        registerPermission(VERSION, "Permission to use the version command", PermissionDefault.TRUE);
        
        // Mode permissions
        registerPermission(MODE_BLOODLUST, "Permission to toggle bloodlust mode", PermissionDefault.TRUE);
        registerPermission(MODE_INTENT, "Permission to toggle infect intent mode", PermissionDefault.TRUE);
        registerPermission(MODE_NIGHTVISION, "Permission to toggle nightvision mode", PermissionDefault.TRUE);
        
        // Trade permissions
        registerPermission(TRADE_OFFER, "Permission to offer blood to other players", PermissionDefault.TRUE);
        registerPermission(TRADE_ACCEPT, "Permission to accept blood offers", PermissionDefault.TRUE);
        registerPermission(FLASK, "Permission to create blood flasks", PermissionDefault.TRUE);
        
        // Admin permissions
        registerPermission(SET, "Permission to use the set command", PermissionDefault.OP);
        registerPermission(SET_VAMPIRE_TRUE, "Permission to set a player as a vampire", PermissionDefault.OP);
        registerPermission(SET_VAMPIRE_FALSE, "Permission to remove vampire status from a player", PermissionDefault.OP);
        registerPermission(SET_INFECTION, "Permission to set a player's infection level", PermissionDefault.OP);
        registerPermission(SET_FOOD, "Permission to set a player's food level", PermissionDefault.OP);
        registerPermission(SET_HEALTH, "Permission to set a player's health", PermissionDefault.OP);
        registerPermission(CONFIG, "Permission to edit the plugin configuration", PermissionDefault.OP);
        registerPermission(LANG, "Permission to edit the plugin language", PermissionDefault.OP);
        
        // Altar permissions
        registerPermission(ALTAR_DARK, "Permission to use dark altars", PermissionDefault.TRUE);
        registerPermission(ALTAR_LIGHT, "Permission to use light altars", PermissionDefault.TRUE);
    }
    
    /**
     * Registers a single permission with the Paper permission system.
     * This method handles the creation and registration of individual permissions,
     * including setting up parent-child relationships for hierarchical permissions.
     * 
     * @param node The permission node (e.g., "vampire.command.show")
     * @param description A human-readable description of the permission
     * @param defaultValue The default value for the permission (TRUE, FALSE, or OP)
     */
    private static void registerPermission(String node, String description, PermissionDefault defaultValue) {
        Permission permission = new Permission(node, description, defaultValue);
        
        // Set up permission hierarchy
        if (node.contains(".")) {
            String parent = node.substring(0, node.lastIndexOf("."));
            Permission parentPerm = PERMISSIONS.get(parent);
            if (parentPerm != null) {
                permission.addParent(parentPerm, true);
            }
        }
        
        // Register the permission
        Bukkit.getPluginManager().addPermission(permission);
        PERMISSIONS.put(node, permission);
    }
    
    /**
     * Checks if a sender has a specific permission.
     * This is a basic permission check that returns true if the sender
     * has the specified permission, false otherwise.
     * 
     * @param sender The command sender to check
     * @param permission The permission node to check
     * @return true if the sender has the permission, false otherwise
     */
    public static boolean has(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }
    
    /**
     * Checks if a sender has a permission and sends a message if they don't.
     * This method combines permission checking with user feedback.
     * 
     * @param sender The command sender to check
     * @param permission The permission node to check
     * @param message The message to send if the sender doesn't have the permission
     * @return true if the sender has the permission, false otherwise
     */
    public static boolean has(CommandSender sender, String permission, String message) {
        if (!has(sender, permission)) {
            sender.sendMessage(message);
            return false;
        }
        return true;
    }
    
    /**
     * Checks if a sender has a permission and optionally sends a default message.
     * This method provides a simpler interface for permission checking with
     * optional feedback.
     * 
     * @param sender The command sender to check
     * @param permission The permission node to check
     * @param message Whether to send a default message if the sender doesn't have the permission
     * @return true if the sender has the permission, false otherwise
     */
    public static boolean has(CommandSender sender, String permission, boolean message) {
        if (!has(sender, permission)) {
            if (message) {
                sender.sendMessage("§cYou don't have permission to use this command.");
            }
            return false;
        }
        return true;
    }

    /**
     * Temporarily grants a permission to a player.
     * This method is useful for temporary permission grants that should
     * be revoked later. The permission will be automatically cleaned up
     * when the player leaves the server.
     * 
     * @param player The player to grant the permission to
     * @param permission The permission node to grant
     * @return true if the permission was granted, false otherwise
     */
    public static boolean grantTemporaryPermission(Player player, String permission) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment attachment = ATTACHMENTS.get(uuid);
        
        if (attachment == null) {
            attachment = player.addAttachment(Bukkit.getPluginManager().getPlugin("Vampire"));
            ATTACHMENTS.put(uuid, attachment);
        }
        
        attachment.setPermission(permission, true);
        return true;
    }

    /**
     * Revokes a temporary permission from a player.
     * This method removes a previously granted temporary permission.
     * 
     * @param player The player to revoke the permission from
     * @param permission The permission node to revoke
     * @return true if the permission was revoked, false otherwise
     */
    public static boolean revokeTemporaryPermission(Player player, String permission) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment attachment = ATTACHMENTS.get(uuid);
        
        if (attachment != null) {
            attachment.unsetPermission(permission);
            return true;
        }
        
        return false;
    }

    /**
     * Cleans up temporary permissions for a player.
     * This method should be called when a player leaves the server or
     * when temporary permissions are no longer needed.
     * 
     * @param player The player to clean up permissions for
     */
    public static void cleanupTemporaryPermissions(Player player) {
        UUID uuid = player.getUniqueId();
        PermissionAttachment attachment = ATTACHMENTS.remove(uuid);
        
        if (attachment != null) {
            player.removeAttachment(attachment);
        }
    }
} 