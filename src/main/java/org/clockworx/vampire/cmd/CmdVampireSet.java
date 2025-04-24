package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
 * Handles the /vampire set <type> ... command, delegating to specific setters.
 */
public class CmdVampireSet extends VCommand {

    private final Map<String, CmdVampireSetAbstract> specificSetters;

    /**
     * Creates the main 'set' command handler and registers specific setters.
     * @param plugin The plugin instance.
     */
    public CmdVampireSet(VampirePlugin plugin) {
        // Base permission for /vampire set, specific permissions checked later
        super(plugin, "set", VampirePermission.SET, 
              "Set various vampire properties", "<type> <player> <value>"); 
        this.specificSetters = new HashMap<>();

        // Instantiate and register each specific setter command
        // The 'name' passed to the specific setter constructor is somewhat arbitrary now,
        // but permission/description/usage should reflect the specific action.
        registerSpecificSetter(new CmdVampireSetVampire(plugin));
        registerSpecificSetter(new CmdVampireSetInfection(plugin));
        registerSpecificSetter(new CmdVampireSetHealth(plugin));
        registerSpecificSetter(new CmdVampireSetFood(plugin));
        // Add other CmdVampireSetAbstract subclasses here if created
    }

    /**
     * Registers a specific setter command implementation.
     * Uses the value returned by getName() as the key (lowercase).
     * @param setter The specific setter command instance.
     */
    private void registerSpecificSetter(CmdVampireSetAbstract setter) {
        if (setter != null && setter.getName() != null) {
            specificSetters.put(setter.getName().toLowerCase(), setter);
        } else {
            plugin.getLogger().warning("Attempted to register a null or unnamed specific setter command.");
        }
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Basic permission (VampirePermission.SET) checked by VCommand superclass

        if (args.length < 1) {
            sendError(sender, "Usage: /" + label + " " + name + " " + usage);
            // Optionally list available types:
            // sendError(sender, "Available types: " + String.join(", ", specificSetters.keySet()));
            return true;
        }

        String type = args[0].toLowerCase();
        CmdVampireSetAbstract specificSetter = specificSetters.get(type);

        if (specificSetter == null) {
            sendError(sender, "Unknown set type: " + type);
            sendError(sender, "Available types: " + String.join(", ", specificSetters.keySet()));
            return true;
        }

        // Check permission for the SPECIFIC setter command
        if (!sender.hasPermission(specificSetter.getPermission())) {
            // Use the specific command's permission message or a generic one
            sendError(sender, "You do not have permission to set '" + type + "'. Required: " + specificSetter.getPermission());
            return true;
        }

        // Prepare arguments for the specific setter (remove the type)
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        // Delegate execution to the specific setter's execute method
        // (which is inherited from CmdVampireSetAbstract)
        // Note: We pass the original 'label' (e.g., "vampire") and 'command'
        return specificSetter.execute(sender, command, label, subArgs);
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        // Basic permission (VampirePermission.SET) checked by VCommand superclass

        if (args.length == 1) {
            // Suggest available types the sender has permission for
            String input = args[0].toLowerCase();
            return specificSetters.entrySet().stream()
                .filter(entry -> sender.hasPermission(entry.getValue().getPermission())) // Check specific perm
                .map(Map.Entry::getKey)
                .filter(typeName -> typeName.startsWith(input))
                .sorted()
                .collect(Collectors.toList());
        }

        if (args.length > 1) {
            // Delegate to the specific setter's tab completer
            String type = args[0].toLowerCase();
            CmdVampireSetAbstract specificSetter = specificSetters.get(type);

            if (specificSetter != null && sender.hasPermission(specificSetter.getPermission())) {
                // Prepare arguments for the specific setter (remove the type)
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                // Delegate tab completion
                return specificSetter.tabComplete(sender, command, label, subArgs);
            }
        }

        return new ArrayList<>(); // No completions otherwise
    }
} 