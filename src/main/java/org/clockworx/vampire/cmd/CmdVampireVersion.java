package org.clockworx.vampire.cmd;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for displaying the plugin version.
 */
public class CmdVampireVersion extends VCommand {
    
    /**
     * Creates a new version command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireVersion(VampirePlugin plugin) {
        super(plugin, "version", "vampire.version");
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "Vampire Plugin " + ChatColor.YELLOW + "v" + plugin.getDescription().getVersion());
        sender.sendMessage(ChatColor.GRAY + "Created by Clockworx");
        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
} 