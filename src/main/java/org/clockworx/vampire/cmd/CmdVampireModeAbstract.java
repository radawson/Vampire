package org.clockworx.vampire.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Abstract base class for vampire mode commands.
 * This class provides common functionality for commands that toggle vampire modes.
 */
public abstract class CmdVampireModeAbstract extends VCommand {

    protected final VampirePlugin plugin;
    protected final String modeName;

    /**
     * Creates a new vampire mode command.
     * 
     * @param plugin The plugin instance
     * @param modeName The name of the mode
     * @param permission The permission required to use this command
     */
    public CmdVampireModeAbstract(VampirePlugin plugin, String modeName, String permission) {
        super(plugin, modeName.toLowerCase(), permission);
        this.plugin = plugin;
        this.modeName = modeName;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;
        CompletableFuture<VampirePlayer> future = plugin.getVampirePlayer(player.getUniqueId());
        
        future.thenAccept(vampirePlayer -> {
            if (vampirePlayer == null) {
                player.sendMessage(getMessage("not-vampire"));
                return;
            }
            executeMode(player, vampirePlayer, args);
        });
        
        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
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
     * @param args The command arguments
     */
    protected abstract void executeMode(Player player, VampirePlayer vampirePlayer, String[] args);

    /**
     * Get tab completions for the mode argument.
     * 
     * @param partial The partial argument to complete
     * @return A list of possible completions
     */
    protected abstract List<String> getModeCompletions(String partial);
} 