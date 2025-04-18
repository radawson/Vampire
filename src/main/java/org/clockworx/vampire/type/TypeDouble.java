package org.clockworx.vampire.type;

import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * A type handler for double values.
 * Used for command arguments that require numeric input.
 */
public class TypeDouble implements TypeHandler<Double> {
    private final VampirePlugin plugin;

    /**
     * Creates a new TypeDouble instance.
     * 
     * @param plugin The plugin instance
     */
    public TypeDouble(VampirePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Double parse(String input, CommandSender sender) throws IllegalArgumentException {
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                plugin.getLanguageConfig().getMessage("error.invalid-number")
            );
        }
    }

    @Override
    public List<String> getTabList(CommandSender sender, String arg) {
        return new ArrayList<>();
    }
} 