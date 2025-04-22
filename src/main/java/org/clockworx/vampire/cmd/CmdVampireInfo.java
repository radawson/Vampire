package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.util.VampireMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for displaying general information about the Vampire plugin.
 * Shows details like version, authors, and website link.
 */
public class CmdVampireInfo extends VCommand {

    /**
     * Creates a new info command.
     *
     * @param plugin The plugin instance.
     */
    public CmdVampireInfo(VampirePlugin plugin) {
        super(plugin, "info", VampirePermission.INFO); // Use the new INFO permission
    }

    /**
     * Executes the /vampire info command.
     * Sends pre-defined informational messages to the command sender.
     *
     * @param sender The command sender.
     * @param command The command being executed.
     * @param label The alias used for the command.
     * @param args The command arguments (ignored for this command).
     * @return True, as the command is always handled.
     */
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Permission check already done by VCommand superclass

        // Send informational messages using localized keys
        VampireMessages.sendLocalized(sender, "command.info.header"); 
        VampireMessages.sendLocalized(sender, "command.info.version", plugin.getDescription().getVersion());
        // Combine authors into a single string
        String authors = String.join(", ", plugin.getDescription().getAuthors());
        VampireMessages.sendLocalized(sender, "command.info.authors", authors);
        VampireMessages.sendLocalized(sender, "command.info.website", plugin.getDescription().getWebsite());
        // Add any other relevant info here
        
        return true;
    }

    /**
     * Provides tab completions for the /vampire info command.
     * This command has no arguments, so it returns an empty list.
     *
     * @param sender The command sender.
     * @param command The command being executed.
     * @param label The alias used for the command.
     * @param args The command arguments.
     * @return An empty list, as there are no sub-arguments.
     */
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        // No arguments for this command
        return new ArrayList<>();
    }
} 