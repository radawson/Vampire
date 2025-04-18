package org.clockworx.vampire.type;

import org.bukkit.command.CommandSender;
import java.util.List;

/**
 * Base interface for all type handlers.
 * Type handlers are responsible for parsing command arguments and providing tab completion.
 * 
 * @param <T> The type that this handler deals with
 */
public interface TypeHandler<T> {
    /**
     * Parses a string input into the appropriate type.
     * 
     * @param input The string input to parse
     * @param sender The command sender
     * @return The parsed value
     * @throws IllegalArgumentException if the input is invalid
     */
    T parse(String input, CommandSender sender) throws IllegalArgumentException;

    /**
     * Gets tab completion suggestions for this type.
     * 
     * @param sender The command sender
     * @param arg The current argument
     * @return A list of tab completion suggestions
     */
    List<String> getTabList(CommandSender sender, String arg);
} 