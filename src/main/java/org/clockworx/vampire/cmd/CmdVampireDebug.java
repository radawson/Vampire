package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.config.VampireConfig;
import org.clockworx.vampire.util.VampireMessages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command to toggle debug mode at runtime.
 */
public class CmdVampireDebug extends VCommand {

    public CmdVampireDebug(VampirePlugin plugin) {
        super(plugin,
              "debug",
              VampirePermission.DEBUG_TOGGLE,
              "Toggle runtime debug logging",
              "[on|off]");
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        VampireConfig config = plugin.getVampireConfig();
        boolean currentState = config.isDebug();
        boolean newState;

        if (args.length == 0) {
            // Toggle if no argument provided
            newState = !currentState;
        } else {
            String arg = args[0].toLowerCase();
            if (arg.equals("on") || arg.equals("true") || arg.equals("1")) {
                newState = true;
            } else if (arg.equals("off") || arg.equals("false") || arg.equals("0")) {
                newState = false;
            } else {
                sendError(sender, "Invalid argument. Use 'on' or 'off'.");
                return false; // Indicate command usage error
            }
        }

        if (newState == currentState) {
            VampireMessages.sendLocalized(sender, "command.debug.no_change", String.valueOf(currentState));
        } else {
            config.setDebug(newState); // Update the config in memory
            VampireMessages.sendLocalized(sender, "command.debug.set", String.valueOf(newState));
            // Log the change to console as well
            plugin.getLogger().info(sender.getName() + " set runtime debug mode to: " + newState);
        }

        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Arrays.asList("on", "off").stream()
                       .filter(s -> s.startsWith(input))
                       .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
} 