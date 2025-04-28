package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.VampireMessages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the /vampire mode <type> command, delegating to specific mode commands.
 */
public class CmdVampireMode extends VCommand {

    private final Map<String, CmdVampireModeAbstract> specificModes;

    /**
     * Creates the main 'mode' command handler and registers specific mode commands.
     * @param plugin The plugin instance.
     */
    public CmdVampireMode(VampirePlugin plugin) {
        // Base permission for /vampire mode, specific permissions checked later
        // Assuming a base permission like MODE_BASE exists or should be created
        super(plugin, "mode", VampirePermission.MODE_BASE, 
              "Toggle various vampire modes", "<bloodlust|intend|nightvision>"); 
        this.specificModes = new HashMap<>();

        // Instantiate and register each specific mode command
        registerSpecificMode(new CmdVampireModeBloodlust(plugin));
        registerSpecificMode(new CmdVampireModeIntend(plugin));
        registerSpecificMode(new CmdVampireModeNightvision(plugin));
        // Add other CmdVampireModeAbstract subclasses here if created
    }

    /**
     * Registers a specific mode command implementation.
     * Uses the value returned by getName() (which is the lowercase mode name) as the key.
     * @param modeCmd The specific mode command instance.
     */
    private void registerSpecificMode(CmdVampireModeAbstract modeCmd) {
        if (modeCmd != null && modeCmd.getName() != null) {
            // CmdVampireModeAbstract constructor already lowercases the name
            specificModes.put(modeCmd.getName(), modeCmd);
        } else {
            plugin.getLogger().warning("Attempted to register a null or unnamed specific mode command.");
        }
    }

    /**
     * Retrieves a specific mode command instance by its name.
     * Used by the main VampireCommand handler to implement shortcut commands.
     * 
     * @param modeName The lowercase name of the mode (e.g., "intent").
     * @return The CmdVampireModeAbstract instance, or null if not found.
     */
    public CmdVampireModeAbstract getSpecificModeCommand(String modeName) {
        return specificModes.get(modeName.toLowerCase());
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Base permission (VampirePermission.MODE_BASE) checked by VCommand superclass

        if (args.length < 1) {
            sendError(sender, "Usage: /" + label + " " + name + " " + usage);
            sendError(sender, "Available modes: " + String.join(", ", specificModes.keySet()));
            return true;
        }

        // Check if sender is a player (required for all mode commands)
        if (!isPlayer(sender)) {
             sendError(sender, VampireMessages.getLocalizedMessage("command.error.must_be_player"));
             return true;
        }
        Player player = (Player) sender;

        String type = args[0].toLowerCase();
        CmdVampireModeAbstract specificModeCmd = specificModes.get(type);

        if (specificModeCmd == null) {
            sendError(sender, "Unknown mode type: " + type);
            sendError(sender, "Available modes: " + String.join(", ", specificModes.keySet()));
            return true;
        }

        // Check permission for the SPECIFIC mode command
        if (!sender.hasPermission(specificModeCmd.getPermission())) {
            sendError(sender, "You do not have permission to use mode '" + type + "'. Required: " + specificModeCmd.getPermission());
            return true;
        }

        // Prepare arguments for the specific mode command (remove the type)
        // Most mode commands don't use further args, but pass them anyway for consistency
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        // Delegate execution to the specific mode command's execute method
        // (which is inherited from CmdVampireModeAbstract)
        return specificModeCmd.execute(sender, command, label, subArgs);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Base permission (VampirePermission.MODE_BASE) checked by VCommand superclass

        if (args.length == 1) {
            // Suggest available modes the sender has permission for
            String input = args[0].toLowerCase();
            return specificModes.entrySet().stream()
                .filter(entry -> sender.hasPermission(entry.getValue().getPermission())) // Check specific perm
                .map(Map.Entry::getKey)
                .filter(modeName -> modeName.startsWith(input))
                .sorted()
                .collect(Collectors.toList());
        }

        if (args.length > 1) {
            // Delegate to the specific mode's tab completer if needed (unlikely for current modes)
            String type = args[0].toLowerCase();
            CmdVampireModeAbstract specificModeCmd = specificModes.get(type);

            if (specificModeCmd != null && sender.hasPermission(specificModeCmd.getPermission())) {
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                return specificModeCmd.tabComplete(sender, command, label, subArgs);
            }
        }

        return new ArrayList<>(); // No completions otherwise
    }
} 