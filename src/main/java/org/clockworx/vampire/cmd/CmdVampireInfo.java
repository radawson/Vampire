package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.altar.AltarAbstract;
import org.clockworx.vampire.manager.AltarManager;
import org.clockworx.vampire.util.VampireMessages;

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
        super(plugin, "info", VampirePermission.INFO, 
              "Show plugin information", ""); // Added desc/usage
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

        // Send standard informational messages
        VampireMessages.sendLocalized(sender, "command.info.header"); 
        VampireMessages.sendLocalized(sender, "command.info.version", plugin.getPluginMeta().getVersion());
        // Combine authors into a single string
        String authors = String.join(", ", plugin.getPluginMeta().getAuthors());
        VampireMessages.sendLocalized(sender, "command.info.authors", authors);
        VampireMessages.sendLocalized(sender, "command.info.website", plugin.getPluginMeta().getWebsite());
        // Add source if available in PluginMeta (might not be standard)
        // VampireMessages.sendLocalized(sender, "command.info.source", plugin.getPluginMeta().getSource());

        // Add runtime debug status
        boolean isDebug = plugin.getVampireConfig().isDebug();
        // Use %1% placeholder in the lang file for the status
        VampireMessages.sendLocalized(sender, "command.info.debug_status", isDebug ? "&aOn" : "&cOff"); 

        // --- Add Altar Information ---
        AltarManager altarManager = plugin.getAltarManager();
        if (altarManager != null && plugin.getVampireConfig().isAltarsEnabled()) {
            List<AltarAbstract> altars = altarManager.getAltars();
            if (!altars.isEmpty()) {
                VampireMessages.sendLocalized(sender, "command.info.altars.header"); // Add this key

                for (AltarAbstract altar : altars) {
                    // Format the materials map into a readable string
                    String materialsString = altar.getMaterialCounts().entrySet().stream()
                        .map(entry -> String.format("%s: %d", entry.getKey().name(), entry.getValue())) // Simple format: MATERIAL_NAME: COUNT
                        .collect(Collectors.joining(", "));

                    if (materialsString.isEmpty()) {
                        materialsString = "None specific (besides core block)"; // Or a localized key
                    }

                    VampireMessages.sendLocalized(sender, "command.info.altars.entry", altar.getName(), materialsString); // Add this key
                }
            }
        }
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