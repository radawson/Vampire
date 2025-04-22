package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.VampireMessages;

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
        super(plugin, "reload", VampirePermission.CONFIG);
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        try {
            plugin.reloadConfig();
            plugin.getVampireConfig().loadConfig();
            plugin.getLanguageConfig().loadLanguage(plugin.getVampireConfig().getLanguage());
            sendSuccess(sender, VampireMessages.getLocalizedMessage("command.reload.success"));
            plugin.getLogger().info("Configuration and language files reloaded by " + sender.getName());
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(java.util.logging.Level.SEVERE, "Failed to reload configuration for Vampire plugin.", e);
            sendError(sender, VampireMessages.getLocalizedMessage("command.reload.failed"));
            return true;
        }
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
} 