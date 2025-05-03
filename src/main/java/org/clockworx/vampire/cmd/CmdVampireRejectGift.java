package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Command to reject a pending Dark Gift offer.
 * Usage: /vampire rejectgift
 */
public class CmdVampireRejectGift extends VCommand {

    private final VampireManager vampireManager;

    public CmdVampireRejectGift(VampirePlugin plugin) {
        super(plugin, "rejectgift", VampirePermission.GIFT_ACCEPT, 
              "Reject a pending Dark Gift offer", "");
        this.vampireManager = plugin.getVampireManager();
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Manual sender check
        if (!isPlayer(sender)) {
            sendError(sender, VampireMessages.getLocalizedMessage("command.error.player_only"));
            return true;
        }
        Player senderPlayer = (Player) sender;
        UUID senderUUID = senderPlayer.getUniqueId();

        // Config check
        if (!plugin.getVampireConfig().isGiftEnabled()) {
            VampireMessages.sendLocalized(sender, "gift.error.disabled");
            return true;
        }

        // No arguments expected for this command
        if (args.length > 0) {
            sendError(sender, VampireMessages.getLocalizedMessage("command.error.too_many_arguments")); 
            return true;
        }

        // Call the manager method to handle the reject logic
        boolean success = vampireManager.rejectGiftOffer(senderUUID);
        
        VampireMessages.debug("CmdVampireRejectGift: rejectGiftOffer returned " + success + " for " + sender.getName());
        
        // Return true because the command was handled
        return true; 
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        // No arguments, so no tab completion needed
        return new ArrayList<>();
    }
} 