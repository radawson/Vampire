package org.clockworx.vampire.cmd;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.clockworx.vampire.VampirePlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Command class for managing blood flasks.
 */
public class CmdVampireFlask extends VCommand {
    
    /**
     * Creates a new flask command.
     * 
     * @param plugin The plugin instance
     */
    public CmdVampireFlask(VampirePlugin plugin) {
        super(plugin, "flask", "vampire.flask");
    }
    
    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        // Check if sender has permission
        if (!sender.hasPermission("vampire.flask")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Get vampire player data asynchronously
        plugin.getVampirePlayer(player.getUniqueId()).thenAccept(vampirePlayer -> {
            if (vampirePlayer == null) {
                sender.sendMessage(ChatColor.RED + "Player data not found.");
                return;
            }
            
            // Check if player is a vampire
            if (!vampirePlayer.isVampire()) {
                sender.sendMessage(ChatColor.RED + "Only vampires can use blood flasks.");
                return;
            }
            
            // Check if player has a blood flask in hand
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand.getType() != Material.GLASS_BOTTLE) {
                sender.sendMessage(ChatColor.RED + "You must be holding a glass bottle to use this command.");
                return;
            }
            
            // Check if player has enough blood
            double bloodAmount = vampirePlayer.getBloodLevel();
            if (bloodAmount < 1.0) {
                sender.sendMessage(ChatColor.RED + "You don't have enough blood to fill a flask.");
                return;
            }
            
            // Create blood flask
            ItemStack bloodFlask = new ItemStack(Material.GLASS_BOTTLE);
            ItemMeta meta = bloodFlask.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Blood Flask");
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "A flask containing vampire blood.");
            lore.add(ChatColor.GRAY + "Right-click to drink.");
            meta.setLore(lore);
            
            bloodFlask.setItemMeta(meta);
            
            // Remove one glass bottle and add blood flask
            if (itemInHand.getAmount() > 1) {
                itemInHand.setAmount(itemInHand.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }
            
            // Add blood flask to inventory
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(bloodFlask);
            } else {
                player.getWorld().dropItem(player.getLocation(), bloodFlask);
            }
            
            // Remove blood from player
            vampirePlayer.setBloodLevel(bloodAmount - 1.0);
            
            sender.sendMessage(ChatColor.GREEN + "You have created a blood flask.");
        });
        
        return true;
    }
    
    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        return new ArrayList<>();
    }
} 