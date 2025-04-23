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
 * Command to accept a pending Dark Gift offer.
 * Usage: /vampire acceptgift
 */
public class CmdVampireAcceptGift extends VCommand {

    private final VampireManager vampireManager;

    public CmdVampireAcceptGift(VampirePlugin plugin) {
        super(plugin, "acceptgift", VampirePermission.GIFT_ACCEPT, 
              "Accept a pending Dark Gift offer", "");
        this.vampireManager = plugin.getVampireManager();
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Manual sender check
        if (!isPlayer(sender)) {
            sendError(sender, VampireMessages.getLocalizedMessage("command.error.must_be_player"));
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
            // Optionally send usage or just proceed
            sendError(sender, VampireMessages.getLocalizedMessage("command.error.too_many_arguments")); // Assuming key exists
            return true;
        }

        // Call the manager method to handle the accept logic
        // The manager method will handle all checks and feedback messages internally
        boolean success = vampireManager.acceptGiftOffer(senderUUID);
        
        VampireMessages.debug("CmdVampireAcceptGift: acceptGiftOffer returned " + success + " for " + sender.getName());
        
        // Return true because the command was handled
        return true; 
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        // No arguments, so no tab completion needed
        return new ArrayList<>();
    }
} 