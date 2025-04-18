package org.clockworx.vampire;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.Map;

/**
 * Permission management system for the Vampire plugin.
 * This class handles permission registration and checking.
 */
public class VampirePermission {
    
    // Permission nodes
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
    
    // Permission map
    private static final Map<String, Permission> PERMISSIONS = new HashMap<>();
    
    /**
     * Registers all permissions with Bukkit.
     */
    public static void registerPermissions() {
        registerPermission(BASECOMMAND, "Base permission for all vampire commands", PermissionDefault.TRUE);
        registerPermission(SHOW, "Permission to use the show command", PermissionDefault.TRUE);
        registerPermission(SHOW_OTHER, "Permission to show other players' vampire status", PermissionDefault.OP);
        registerPermission(LIST, "Permission to list vampires and infected players", PermissionDefault.OP);
        registerPermission(SHRIEK, "Permission to use the shriek command", PermissionDefault.TRUE);
        registerPermission(VERSION, "Permission to use the version command", PermissionDefault.TRUE);
        registerPermission(MODE_BLOODLUST, "Permission to toggle bloodlust mode", PermissionDefault.TRUE);
        registerPermission(MODE_INTENT, "Permission to toggle infect intent mode", PermissionDefault.TRUE);
        registerPermission(MODE_NIGHTVISION, "Permission to toggle nightvision mode", PermissionDefault.TRUE);
        registerPermission(TRADE_OFFER, "Permission to offer blood to other players", PermissionDefault.TRUE);
        registerPermission(TRADE_ACCEPT, "Permission to accept blood offers", PermissionDefault.TRUE);
        registerPermission(FLASK, "Permission to create blood flasks", PermissionDefault.TRUE);
        registerPermission(SET, "Permission to use the set command", PermissionDefault.OP);
        registerPermission(SET_VAMPIRE_TRUE, "Permission to set a player as a vampire", PermissionDefault.OP);
        registerPermission(SET_VAMPIRE_FALSE, "Permission to remove vampire status from a player", PermissionDefault.OP);
        registerPermission(SET_INFECTION, "Permission to set a player's infection level", PermissionDefault.OP);
        registerPermission(SET_FOOD, "Permission to set a player's food level", PermissionDefault.OP);
        registerPermission(SET_HEALTH, "Permission to set a player's health", PermissionDefault.OP);
        registerPermission(CONFIG, "Permission to edit the plugin configuration", PermissionDefault.OP);
        registerPermission(LANG, "Permission to edit the plugin language", PermissionDefault.OP);
        registerPermission(ALTAR_DARK, "Permission to use dark altars", PermissionDefault.TRUE);
        registerPermission(ALTAR_LIGHT, "Permission to use light altars", PermissionDefault.TRUE);
    }
    
    /**
     * Registers a permission with Bukkit.
     * 
     * @param node The permission node
     * @param description The permission description
     * @param defaultValue The default value of the permission
     */
    private static void registerPermission(String node, String description, PermissionDefault defaultValue) {
        Permission permission = new Permission(node, description, defaultValue);
        Bukkit.getPluginManager().addPermission(permission);
        PERMISSIONS.put(node, permission);
    }
    
    /**
     * Checks if a sender has a permission.
     * 
     * @param sender The command sender
     * @param permission The permission to check
     * @return true if the sender has the permission, false otherwise
     */
    public static boolean has(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }
    
    /**
     * Checks if a sender has a permission and sends a message if they don't.
     * 
     * @param sender The command sender
     * @param permission The permission to check
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
     * Checks if a sender has a permission and sends a default message if they don't.
     * 
     * @param sender The command sender
     * @param permission The permission to check
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
} 