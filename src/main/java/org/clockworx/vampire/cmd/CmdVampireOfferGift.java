package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Command to offer the Dark Gift to another player.
 * Usage: /vampire offergift <player>
 */
public class CmdVampireOfferGift extends VCommand {

    private final VampireManager vampireManager;

    public CmdVampireOfferGift(VampirePlugin plugin) {
        super(plugin, "offergift", VampirePermission.GIFT_OFFER, 
              "Offer the Dark Gift to another player", "<player>");
        this.vampireManager = plugin.getVampireManager();
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!isPlayer(sender)) {
            sendError(sender, VampireMessages.getLocalizedMessage("command.error.player_only"));
            return true;
        }
        Player senderPlayer = (Player) sender;
        UUID senderUUID = senderPlayer.getUniqueId();

        if (args.length < 1) {
            sendError(sender, VampireMessages.getLocalizedMessage("command.error.missing_argument", "player"));
            return true;
        }

        if (!plugin.getVampireConfig().isGiftEnabled()) {
            VampireMessages.sendLocalized(sender, "gift.error.disabled");
            return true;
        }

        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayerExact(targetName);

        if (targetPlayer == null || !targetPlayer.isOnline()) {
            VampireMessages.sendLocalized(sender, "player.not-found", targetName);
            return true;
        }

        UUID targetUUID = targetPlayer.getUniqueId();

        boolean success = vampireManager.makeGiftOffer(senderUUID, targetUUID);
        
        VampireMessages.debug("CmdVampireOfferGift: makeGiftOffer returned " + success + " for " + sender.getName() + " -> " + targetName);
        
        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .filter(name -> !(sender instanceof Player) || !((Player) sender).getName().equalsIgnoreCase(name))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
} 