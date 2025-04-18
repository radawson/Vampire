package org.clockworx.vampire.type;

import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A type handler for double values that must be within a specific range.
 * Used for command arguments that require numeric input within bounds.
 */
public class TypeLimitedDouble implements TypeHandler<Double> {
    private final double min;
    private final double max;
    private final VampirePlugin plugin;

    /**
     * Creates a new TypeLimitedDouble instance.
     * 
     * @param plugin The plugin instance
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     */
    public TypeLimitedDouble(VampirePlugin plugin, double min, double max) {
        this.plugin = plugin;
        this.min = min;
        this.max = max;
    }

    /**
     * Parses a string input into a double value, ensuring it's within the valid range.
     * 
     * @param input The string input to parse
     * @param sender The command sender
     * @return The parsed double value
     * @throws IllegalArgumentException if the input is not a valid number or is outside the valid range
     */
    @Override
    public Double parse(String input, CommandSender sender) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(
                plugin.getLanguageConfig().getMessage("error.empty-input")
            );
        }
        
        try {
            double value = Double.parseDouble(input.trim());
            if (Double.isNaN(value) || Double.isInfinite(value)) {
                throw new IllegalArgumentException(
                    plugin.getLanguageConfig().getMessage("error.invalid-number")
                );
            }
            
            if (value < min || value > max) {
                throw new IllegalArgumentException(
                    plugin.getLanguageConfig().getMessage("error.number-range", 
                        String.format("%.1f", min), 
                        String.format("%.1f", max))
                );
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                plugin.getLanguageConfig().getMessage("error.invalid-number")
            );
        }
    }

    /**
     * Gets tab completion suggestions for this type.
     * 
     * @param sender The command sender
     * @param arg The current argument
     * @return A list of tab completion suggestions
     */
    @Override
    public List<String> getTabList(CommandSender sender, String arg) {
        if (arg.isEmpty()) {
            return Arrays.asList(
                String.format("%.1f", min),
                String.format("%.1f", (min + max) / 2),
                String.format("%.1f", max)
            );
        }
        
        try {
            double value = Double.parseDouble(arg);
            if (value < min) {
                return Collections.singletonList(String.format("%.1f", min));
            } else if (value > max) {
                return Collections.singletonList(String.format("%.1f", max));
            }
        } catch (NumberFormatException ignored) {
            // Invalid number format, return empty list
        }
        
        return new ArrayList<>();
    }
} 