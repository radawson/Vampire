package org.clockworx.vampire.cmd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Command for listing vampires and infected players.
 * Shows both online and offline vampires/infected players.
 */
public class CmdVampireList extends VCommand {

    /**
     * Creates a new list command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireList(VampirePlugin plugin) {
        super(plugin, "list", "vampire.list");
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Get page number
        int pageNum = 1;
        if (args.length > 0) {
            try {
                int parsedPage = Integer.parseInt(args[0]);
                if (parsedPage >= 1) {
                    pageNum = parsedPage;
                }
            } catch (NumberFormatException e) {
                sendError(sender, getMessage("command.list.invalid_page"));
                return true;
            }
        }

        final int page = pageNum;

        // Get player data asynchronously
        CompletableFuture<VampirePlayer> future = plugin.getVampirePlayer(sender instanceof Player ? ((Player) sender).getUniqueId() : null);
        future.thenAccept(vampirePlayer -> {
            // Get all online players
            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            
            // Separate vampires and infected
            List<String> onlineVampires = new ArrayList<>();
            List<String> onlineInfected = new ArrayList<>();
            List<String> offlineVampires = new ArrayList<>();
            List<String> offlineInfected = new ArrayList<>();
            
            // Process online players
            for (Player player : onlinePlayers) {
                CompletableFuture<VampirePlayer> playerFuture = plugin.getVampirePlayer(player.getUniqueId());
                playerFuture.thenAccept(playerData -> {
                    if (playerData != null) {
                        if (playerData.isVampire()) {
                            onlineVampires.add(player.getName());
                        } else if (playerData.isInfected()) {
                            onlineInfected.add(player.getName());
                        }
                    }
                });
            }
            
            // Display results
            sendInfo(sender, getMessage("command.list.header").replace("%page%", String.valueOf(page)));
            sendInfo(sender, getMessage("command.list.online_vampires").replace("%players%", String.join(", ", onlineVampires)));
            sendInfo(sender, getMessage("command.list.online_infected").replace("%players%", String.join(", ", onlineInfected)));
            sendInfo(sender, getMessage("command.list.offline_vampires").replace("%players%", String.join(", ", offlineVampires)));
            sendInfo(sender, getMessage("command.list.offline_infected").replace("%players%", String.join(", ", offlineInfected)));
        });
        
        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return List.of("1", "2", "3", "4", "5");
        }
        return new ArrayList<>();
    }
} 