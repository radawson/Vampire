package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Abstract base class for vampire mode commands.
 * This class provides common functionality for commands that toggle vampire modes.
 */
public abstract class CmdVampireModeAbstract extends VCommand {

    protected final String modeName;
    protected final VampireManager vampireManager;

    /**
     * Creates a new vampire mode command.
     * 
     * @param plugin The plugin instance
     * @param modeName The name of the mode
     * @param permission The permission required to use this command
     * @param description A brief description of the command.
     * @param usage A string indicating the command's arguments/usage pattern.
     * @param toggleable Whether the command represents a toggleable state.
     */
    public CmdVampireModeAbstract(VampirePlugin plugin, String modeName, String permission, String description, String usage, boolean toggleable) {
        super(plugin, modeName.toLowerCase(), permission, description, usage, toggleable);
        this.modeName = modeName;
        this.vampireManager = plugin.getVampireManager();
    }

    /**
     * Overloaded constructor, defaulting toggleable to false.
     */
    public CmdVampireModeAbstract(VampirePlugin plugin, String modeName, String permission, String description, String usage) {
        this(plugin, modeName, permission, description, usage, false);
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            VampireMessages.sendLocalized(sender, "command.error.player_only");
            return true;
        }

        Player player = (Player) sender;
        VampirePlayer vampirePlayer = vampireManager.getCachedVampirePlayer(player.getUniqueId());
        
        if (vampirePlayer == null || !vampirePlayer.isVampire()) {
            sendError(sender, getMessage("vampire.not_vampire").replace("%player%", "You"));
            return true;
        }
        
        executeMode(player, vampirePlayer, vampireManager, args);
        
        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        VampirePlayer vp = vampireManager.getCachedVampirePlayer(((Player) sender).getUniqueId());
        if (vp == null || !vp.isVampire()) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return getModeCompletions(args[0]);
        }

        return new ArrayList<>();
    }

    /**
     * Execute the mode-specific command logic.
     * 
     * @param player The player executing the command
     * @param vampirePlayer The vampire player data
     * @param manager The VampireManager instance
     * @param args The command arguments
     */
    protected abstract void executeMode(Player player, VampirePlayer vampirePlayer, VampireManager manager, String[] args);

    /**
     * Get tab completions for the mode argument.
     * 
     * @param partial The partial argument to complete
     * @return A list of possible completions
     */
    protected abstract List<String> getModeCompletions(String partial);
} 