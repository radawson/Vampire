package org.clockworx.vampire.type;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.VampireMessages;

/**
 * A type handler for double values.
 * Used for command arguments that require numeric input.
 */
public class TypeDouble implements TypeHandler<Double> {

    /**
     * Creates a new TypeDouble instance.
     * 
     * @param plugin The plugin instance (No longer needed)
     */
    public TypeDouble(VampirePlugin plugin) {
        // No need to store plugin instance
    }

    @Override
    public Double parse(String input, CommandSender sender) throws IllegalArgumentException {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            // Use VampireMessages to get the localized error message
            throw new IllegalArgumentException(
                VampireMessages.getLocalizedMessage("command.error.invalid_number") // Need lang key
            );
        }
    }

    @Override
    public List<String> getTabList(CommandSender sender, String arg) {
        // No specific tab completions for a generic double
        return new ArrayList<>();
    }
} 