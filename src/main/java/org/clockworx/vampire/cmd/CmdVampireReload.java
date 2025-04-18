package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.ResourceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for reloading the plugin configuration.
 */
public class CmdVampireReload extends VCommand {
    
    /**
     * Creates a new reload command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireReload(VampirePlugin plugin) {
        super(plugin, "reload", "vampire.admin");
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        try {
            plugin.reloadConfig();
            plugin.getVampireConfig().reload();
            plugin.getLanguageConfig().reload();
            sendSuccess(sender, getMessage("command.reload.success"));
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
            sendError(sender, getMessage("command.reload.failed"));
            return false;
        }
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
} 