package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Command class for displaying help information about vampire commands and lore.
 */
public class CmdVampireHelp extends VCommand {
    
    // Define base permission needed for any help
    private static final String BASE_HELP_PERMISSION = VampirePermission.HELP_COMMAND;

    /**
     * Creates a new help command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireHelp(VampirePlugin plugin) {
        // Pass the base permission required to use the help command at all
        super(plugin, "help", BASE_HELP_PERMISSION, 
              "Displays command help or lore information", "[topic]");
        // Aliases can be set here if needed
        // setAliases(Arrays.asList("?")); 
        // Set usage message key
        // setUsageKey("command.help.usage"); // Example
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Base permission check is now handled by VCommand.onCommand based on the permission passed above
        // We can remove the redundant check here if VCommand handles it.
        // However, keeping it explicit might be clearer, or VCommand's check needs adjusting.
        // Let's assume VCommand's check is sufficient for BASE_HELP_PERMISSION.
        // if (!VampirePermission.has(sender, BASE_HELP_PERMISSION, true)) { 
        //     return true;
        // }

        if (args.length == 0) {
            // --- Display Command List --- 
            // Permission already checked (BASE_HELP_PERMISSION)
            sendHelpList(sender);

        } else {
            // --- Display Lore Topic --- 
            // Check for specific lore permission
            if (!VampirePermission.has(sender, VampirePermission.HELP_LORE, true)) {
                return true; // Message sent by has()
            }
            
            String topic = args[0].toLowerCase();
            sendLoreTopic(sender, topic);
        }
        
        return true;
    }

    /**
     * Sends the list of available commands to the sender.
     */
    private void sendHelpList(CommandSender sender) {
        // Get the header message
        VampireMessages.sendLocalized(sender, "command.help.header");

        // Get registered subcommands from the base VCommand class
        // This requires access to the registered subcommands map, maybe add a getter to VCommand?
        // For now, let's assume we can get them or list them manually based on language file.
        
        // Example: Manually listing based on keys in language file
        VampireMessages.sendLocalized(sender, "command.help.info");
        VampireMessages.sendLocalized(sender, "command.help.list");
        VampireMessages.sendLocalized(sender, "command.help.offer");
        VampireMessages.sendLocalized(sender, "command.help.accept");
        // ... list other commands defined in en.yml command.help section ...
        VampireMessages.sendLocalized(sender, "command.help.mode");
        VampireMessages.sendLocalized(sender, "command.help.shriek");
        VampireMessages.sendLocalized(sender, "command.help.reload");
        // Add more as needed

        // Add info about lore help if they have permission
        if (sender.hasPermission(VampirePermission.HELP_LORE)) {
            VampireMessages.sendLocalized(sender, "command.help.lore_hint"); // Need to add this key to en.yml
        }
    }

    /**
     * Sends the lore information for a specific topic.
     */
    private void sendLoreTopic(CommandSender sender, String topic) {
        String loreKey = "help.lore." + topic;
        
        // VampireMessages.getLocalizedMessage returns the key if not found.
        // We need a way to check if the key *actually* exists in the language file.
        // Maybe LanguageConfig needs a `hasMessage(key)` method?
        // For now, we fetch the message and compare. Crude but works without LanguageConfig changes.
        String messageOrKey = VampireMessages.getLocalizedMessage(loreKey);

        if (messageOrKey.startsWith("&cMissing message: ")) { // Check if the key was missing
            VampireMessages.sendLocalized(sender, "help.lore.topic_not_found", topic);
        } else {
            // The message might be a list in YAML, need to handle that.
            // Assuming getLocalizedMessage returns a single string (potentially with newlines \n)
            // Or we need a method to get a list of strings.
            // Let's assume single string with newlines for now.
            sender.sendMessage(messageOrKey); // Send the retrieved lore text
            // If it's a list, we'd iterate and send each line.
        }
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            // Suggest lore topics if player has HELP_LORE permission
            if (sender.hasPermission(VampirePermission.HELP_LORE)) {
                List<String> topics = Arrays.asList("blood_regeneration", "sunlight", "infection"); // Example topics
                // TODO: Dynamically get topics from lang file keys under help.lore?
                return topics.stream()
                           .filter(topic -> topic.startsWith(args[0].toLowerCase()))
                           .collect(Collectors.toList());
            }
        }
        return new ArrayList<>(); // No suggestions otherwise
    }
} 