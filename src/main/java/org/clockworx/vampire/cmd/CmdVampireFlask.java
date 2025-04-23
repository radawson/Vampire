package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.ItemManager;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Command class for creating Blood Vials and Holy Water.
 */
public class CmdVampireFlask extends VCommand {

    private final VampireManager vampireManager;
    private final ItemManager itemManager;
    private final VampirePlugin plugin;

    /**
     * Constructs the command handler for /vampire flask.
     *
     * @param plugin The main Vampire plugin instance.
     */
    public CmdVampireFlask(VampirePlugin plugin) {
        // Base permission is for blood vial, holy water has specific check
        super(plugin, "flask", VampirePermission.FLASK, 
              "Create a Blood Vial or Holy Water", "[blood|holy]");
        this.vampireManager = plugin.getVampireManager();
        this.itemManager = plugin.getItemManager();
        this.plugin = plugin;
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!isPlayer(sender)) {
            sendError(sender, VampireMessages.getLocalizedMessage("command.error.must_be_player"));
            return true;
        }
        Player player = (Player) sender;

        // Determine vial type
        String vialType = (args.length > 0) ? args[0].toLowerCase() : "blood";

        if ("blood".equals(vialType)) {
            return createBloodVial(player);
        } else if ("holy".equals(vialType)) {
            return createHolyWater(player);
        } else {
            sendError(sender, VampireMessages.getLocalizedMessage("command.flask.invalid_vial_type"));
            return true;
        }
    }

    /**
     * Handles the creation of a Blood Vial.
     *
     * @param player The player executing the command.
     * @return True if the command execution was handled.
     */
    private boolean createBloodVial(Player player) {
        // Base permission VampirePermission.FLASK is already checked by VCommand
        UUID playerUUID = player.getUniqueId();
        VampirePlayer vampirePlayer = vampireManager.getCachedVampirePlayer(playerUUID);
        if (vampirePlayer == null) {
            sendError(player, VampireMessages.getLocalizedMessage("command.error.player_data_not_found"));
            return true;
        }

        if (!vampirePlayer.isVampire()) {
            VampireMessages.sendLocalized(player, "command.flask.not_vampire");
            return true;
        }

        // Check if player has an empty glass bottle in hand
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.GLASS_BOTTLE) {
             VampireMessages.sendLocalized(player, "command.flask.need_empty_bottle");
             return true;
        }

        // TODO: Configurable blood cost
        double bloodCost = 1.0;
        if (vampirePlayer.getBlood() < bloodCost) {
             VampireMessages.sendLocalized(player, "command.flask.not_enough_blood", String.format("%.1f", bloodCost));
             return true;
        }

        if (!vampireManager.useBlood(playerUUID, bloodCost)) {
             VampireMessages.sendLocalized(player, "command.flask.blood_use_failed");
             VampireMessages.error("Failed to use blood for flask creation for " + player.getName() + " despite passing check!", null);
             return true;
        }
        
        ItemStack bloodVial = itemManager.createBloodVial();
        if (bloodVial == null || bloodVial.getType() == Material.AIR) {
             VampireMessages.error("ItemManager failed to create Blood Vial!", null);
             vampireManager.addBlood(playerUUID, bloodCost); // Refund blood
             sendError(player, "Failed to create blood vial item.");
             return true;
        }
        
        itemInHand.setAmount(itemInHand.getAmount() - 1);
        
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(bloodVial);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(0));
            VampireMessages.sendLocalized(player, "command.flask.inventory_full");
        }
        
        VampireMessages.sendLocalized(player, "command.flask.success");
        VampireMessages.debug(player.getName() + " created a blood vial (cost: " + bloodCost + " blood)");
        return true;
    }

    /**
     * Handles the creation of Holy Water via command.
     *
     * @param player The player executing the command.
     * @return True if the command execution was handled.
     */
    private boolean createHolyWater(Player player) {
        // Check specific permission for creating holy water via command
        if (!VampirePermission.has(player, VampirePermission.FLASK_HOLYWATER, true)) {
            return true; // Message already sent by has()
        }

        ItemStack holyWater = itemManager.createHolyWater();
        if (holyWater == null || holyWater.getType() == Material.AIR) {
             VampireMessages.error("ItemManager failed to create Holy Water!", null);
             sendError(player, VampireMessages.getLocalizedMessage("command.flask.failed_to_create_item")); 
             return true;
        }

        // Add holy water to inventory or drop if full
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(holyWater);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover.get(0));
             VampireMessages.sendLocalized(player, "command.flask.inventory_full_holy"); // Need lang key
        }

        VampireMessages.sendLocalized(player, "command.flask.holy_success"); // Need lang key
        VampireMessages.debug(player.getName() + " created holy water via command.");
        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            // Suggest 'blood' if player has base flask perm (and is vampire? maybe not needed for tab complete)
            if (sender instanceof Player && VampirePermission.has(sender, VampirePermission.FLASK)) {
                 suggestions.add("blood");
            }
            // Suggest 'holy' if player has holy water flask perm
            if (VampirePermission.has(sender, VampirePermission.FLASK_HOLYWATER)) {
                 suggestions.add("holy");
            }
            return suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(); // No suggestions for > 1 argument
    }
} 