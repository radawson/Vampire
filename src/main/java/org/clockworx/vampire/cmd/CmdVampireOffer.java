package org.clockworx.vampire.cmd;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.util.ResourceUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for offering blood to another player.
 */
public class CmdVampireOffer extends VCommand {
    
    /**
     * Creates a new offer command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireOffer(VampirePlugin plugin) {
        super(plugin, "offer", "vampire.offer");
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.player_only"));
            return false;
        }
        
        if (args.length < 2) {
            ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.offer.usage"));
            return false;
        }
        
        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null) {
            ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.offer.player_not_found"));
            return false;
        }
        
        if (target.equals(player)) {
            ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.offer.cannot_offer_self"));
            return false;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.offer.invalid_amount"));
            return false;
        }
        
        // Get vampire data
        plugin.getVampirePlayer(player.getUniqueId()).thenAccept(vampire -> {
            if (vampire == null || !vampire.isVampire()) {
                ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.offer.not_vampire"));
                return;
            }
            
            if (vampire.getBlood() < amount) {
                ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.offer.insufficient_blood"));
                return;
            }
            
            // Create blood offer
            plugin.getDatabaseManager().createBloodOffer(player.getUniqueId(), target.getUniqueId(), amount)
                .thenAccept(offer -> {
                    if (offer != null) {
                        ResourceUtil.sendSuccess(sender, ResourceUtil.getMessage("command.offer.success")
                            .replace("%player%", target.getName())
                            .replace("%amount%", String.format("%.1f", amount)));
                        
                        ResourceUtil.sendInfo(target, ResourceUtil.getMessage("command.offer.notify_target")
                            .replace("%player%", player.getName())
                            .replace("%amount%", String.format("%.1f", amount)));
                    } else {
                        ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.offer.failed"));
                    }
                });
        });
        
        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Add online player names
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            // Add some common blood amounts
            completions.add("1.0");
            completions.add("2.0");
            completions.add("5.0");
            completions.add("10.0");
        }
        
        return completions;
    }
} 