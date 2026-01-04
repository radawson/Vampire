package org.clockworx.vampire.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for providing command suggestions in Brigadier commands.
 */
public class CommandSuggestions {
    
    /**
     * Suggests online player names.
     * 
     * @param context The command context
     * @param builder The suggestions builder
     * @return Future with suggestions
     */
    public static CompletableFuture<Suggestions> suggestOnlinePlayers(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(remaining)) {
                builder.suggest(player.getName());
            }
        }
        return builder.buildFuture();
    }
    
    /**
     * Suggests from a list of string options.
     * 
     * @param context The command context
     * @param builder The suggestions builder
     * @param options The list of options to suggest
     * @return Future with suggestions
     */
    public static CompletableFuture<Suggestions> suggestFromList(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder,
            List<String> options) {
        String remaining = builder.getRemaining().toLowerCase();
        for (String option : options) {
            if (option.toLowerCase().startsWith(remaining)) {
                builder.suggest(option);
            }
        }
        return builder.buildFuture();
    }
    
    /**
     * Suggests from an array of string options.
     * 
     * @param context The command context
     * @param builder The suggestions builder
     * @param options The array of options to suggest
     * @return Future with suggestions
     */
    public static CompletableFuture<Suggestions> suggestFromArray(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder,
            String... options) {
        return suggestFromList(context, builder, Arrays.asList(options));
    }
    
    /**
     * Suggests vampire set types.
     */
    public static CompletableFuture<Suggestions> suggestSetTypes(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        return suggestFromArray(context, builder, "vampire", "infection", "blood", "level", "food", "health");
    }
    
    /**
     * Suggests vampire mode types.
     */
    public static CompletableFuture<Suggestions> suggestModeTypes(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        return suggestFromArray(context, builder, "bloodlust", "intent", "nightvision");
    }
    
    /**
     * Suggests help topics.
     */
    public static CompletableFuture<Suggestions> suggestHelpTopics(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        return suggestFromArray(context, builder, "blood_regeneration", "sunlight", "infection");
    }
    
    /**
     * Suggests level operations.
     */
    public static CompletableFuture<Suggestions> suggestLevelOperations(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        return suggestFromArray(context, builder, "set", "add", "remove");
    }
    
    /**
     * Suggests flask types.
     */
    public static CompletableFuture<Suggestions> suggestFlaskTypes(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        return suggestFromArray(context, builder, "blood", "holy");
    }
}

