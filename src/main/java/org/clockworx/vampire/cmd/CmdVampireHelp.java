package org.clockworx.vampire.cmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for displaying help information about vampire commands.
 */
public class CmdVampireHelp extends VCommand {
    
    /**
     * Creates a new help command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireHelp(VampirePlugin plugin) {
        super(plugin, "help", "vampire.help");
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "=== Vampire Plugin Help ===");
        sender.sendMessage(ChatColor.YELLOW + "/vampire help" + ChatColor.WHITE + " - Show this help message");
        sender.sendMessage(ChatColor.YELLOW + "/vampire version" + ChatColor.WHITE + " - Show plugin version");
        sender.sendMessage(ChatColor.YELLOW + "/vampire show [player]" + ChatColor.WHITE + " - Show vampire status");
        sender.sendMessage(ChatColor.YELLOW + "/vampire list" + ChatColor.WHITE + " - List vampires and infected players");
        sender.sendMessage(ChatColor.YELLOW + "/vampire set <type> <value> [player]" + ChatColor.WHITE + " - Set vampire properties");
        sender.sendMessage(ChatColor.YELLOW + "/vampire reset [player]" + ChatColor.WHITE + " - Reset vampire status");
        sender.sendMessage(ChatColor.YELLOW + "/vampire reload" + ChatColor.WHITE + " - Reload plugin configuration");
        
        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
} 