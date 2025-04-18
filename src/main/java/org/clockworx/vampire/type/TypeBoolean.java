package org.clockworx.vampire.type;

import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePlugin;

import java.util.Arrays;
import java.util.List;

/**
 * A type handler for boolean values.
 * Used for command arguments that require true/false input.
 */
public class TypeBoolean implements TypeHandler<Boolean> {
    private final VampirePlugin plugin;
    private static final List<String> TRUE_VALUES = Arrays.asList("true", "yes", "on", "1");
    private static final List<String> FALSE_VALUES = Arrays.asList("false", "no", "off", "0");

    /**
     * Creates a new TypeBoolean instance.
     * 
     * @param plugin The plugin instance
     */
    public TypeBoolean(VampirePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Boolean parse(String input, CommandSender sender) throws IllegalArgumentException {
        String lowerInput = input.toLowerCase();
        if (TRUE_VALUES.contains(lowerInput)) {
            return true;
        } else if (FALSE_VALUES.contains(lowerInput)) {
            return false;
        }
        throw new IllegalArgumentException(
            plugin.getLanguageConfig().getMessage("error.invalid-boolean")
        );
    }

    @Override
    public List<String> getTabList(CommandSender sender, String arg) {
        if (arg.isEmpty()) {
            return Arrays.asList("true", "false");
        }
        String lowerArg = arg.toLowerCase();
        if ("t".startsWith(lowerArg)) {
            return Arrays.asList("true");
        } else if ("f".startsWith(lowerArg)) {
            return Arrays.asList("false");
        }
        return Arrays.asList();
    }
} 