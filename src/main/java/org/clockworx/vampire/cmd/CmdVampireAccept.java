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
import java.util.concurrent.CompletableFuture;

/**
 * Command class for accepting blood offers.
 */
public class CmdVampireAccept extends VCommand {
    
    /**
     * Creates a new accept command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireAccept(VampirePlugin plugin) {
        super(plugin, "accept", "vampire.accept");
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.player_only"));
            return false;
        }
        
        Player player = (Player) sender;
        
        // Check if player has a pending offer
        plugin.getDatabaseManager().getBloodOffer(player.getUniqueId())
            .thenAccept(offer -> {
                if (offer == null) {
                    ResourceUtil.sendError(sender, ResourceUtil.getMessage("command.accept.no_offer"));
                    return;
                }
                
                // Get the sender of the offer
                Player offerSender = Bukkit.getPlayer(offer.getSenderUuid());
                if (offerSender == null) {
                    ResourceUtil.sendError(player, ResourceUtil.getMessage("command.accept.sender_offline"));
                    return;
                }
                
                // Get both players' vampire data
                CompletableFuture<VampirePlayer> senderFuture = plugin.getVampirePlayer(offer.getSenderUuid());
                CompletableFuture<VampirePlayer> targetFuture = plugin.getVampirePlayer(player.getUniqueId());
                
                CompletableFuture.allOf(senderFuture, targetFuture).thenAccept(v -> {
                    try {
                        VampirePlayer senderVampire = senderFuture.get();
                        VampirePlayer targetVampire = targetFuture.get();
                        
                        if (senderVampire == null || !senderVampire.isVampire()) {
                            ResourceUtil.sendError(player, ResourceUtil.getMessage("command.accept.sender_not_vampire"));
                            return;
                        }
                        
                        if (senderVampire.getBlood() < offer.getAmount()) {
                            ResourceUtil.sendError(player, ResourceUtil.getMessage("command.accept.insufficient_blood"));
                            return;
                        }
                        
                        // Process the blood transfer
                        plugin.getDatabaseManager().acceptBloodOffer(player.getUniqueId())
                            .thenAccept(success -> {
                                if (success) {
                                    // Update blood levels
                                    senderVampire.setBlood(senderVampire.getBlood() - offer.getAmount());
                                    if (targetVampire != null) {
                                        targetVampire.setInfection(targetVampire.getInfection() + offer.getAmount());
                                    }
                                    
                                    // Save changes
                                    plugin.getDatabaseManager().savePlayer(senderVampire);
                                    if (targetVampire != null) {
                                        plugin.getDatabaseManager().savePlayer(targetVampire);
                                    }
                                    
                                    // Send messages
                                    ResourceUtil.sendSuccess(player, ResourceUtil.getMessage("command.accept.success")
                                        .replace("%player%", offerSender.getName())
                                        .replace("%amount%", String.format("%.1f", offer.getAmount())));
                                    
                                    ResourceUtil.sendSuccess(offerSender, ResourceUtil.getMessage("command.accept.notify_sender")
                                        .replace("%player%", player.getName())
                                        .replace("%amount%", String.format("%.1f", offer.getAmount())));
                                } else {
                                    ResourceUtil.sendError(player, ResourceUtil.getMessage("command.accept.failed"));
                                }
                            });
                        
                    } catch (Exception e) {
                        plugin.getLogger().severe("Error processing blood acceptance: " + e.getMessage());
                        ResourceUtil.sendError(player, ResourceUtil.getMessage("command.accept.failed"));
                    }
                });
            });
        
        return true; // Return true since we've started the async operation
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
} 