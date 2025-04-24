package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.VampireMessages;

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
        super(plugin, "version", VampirePermission.VERSION, 
              "Display plugin version", "");
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        VampireMessages.sendLocalized(sender, "version.line1", plugin.getPluginMeta().getVersion());
        VampireMessages.sendLocalized(sender, "version.line2", String.join(", ", plugin.getPluginMeta().getAuthors()));
        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
} 