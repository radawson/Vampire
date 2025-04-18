package org.clockworx.vampire.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for managing blood flasks in the game.
 * Blood flasks are special potions that can store and transfer blood between players.
 */
public class BloodFlaskUtil {
    private static final String COLOR_RED = ChatColor.RED.toString();
    private static final String BLOOD_FLASK_NAME = ChatColor.GREEN + "Blood Flask";
    private static final String BLOOD_FLASK_AMOUNT_SUFFIX = COLOR_RED + " units of blood";
    private static final String BLOOD_FLASK_VAMPIRIC_TRUE = COLOR_RED + "The blood is vampiric";
    private static final String BLOOD_FLASK_VAMPIRIC_FALSE = COLOR_RED + "The blood is not vampiric";
    private static final PotionEffect BLOOD_FLASK_CUSTOM_EFFECT = new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20, 0);

    private final VampirePlugin plugin;

    public BloodFlaskUtil(VampirePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a new blood flask item with the specified amount and vampiric status.
     * 
     * @param amount The amount of blood in the flask
     * @param isVampiric Whether the blood is from a vampire
     * @return The created blood flask ItemStack
     */
    public ItemStack createBloodFlask(double amount, boolean isVampiric) {
        ItemStack flask = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) flask.getItemMeta();
        
        // Set display name and lore
        meta.setDisplayName(BLOOD_FLASK_NAME);
        List<String> lore = new ArrayList<>();
        lore.add(String.format("%.1f %s", amount, BLOOD_FLASK_AMOUNT_SUFFIX));
        lore.add(isVampiric ? BLOOD_FLASK_VAMPIRIC_TRUE : BLOOD_FLASK_VAMPIRIC_FALSE);
        meta.setLore(lore);
        
        // Add custom effect and hide default potion effects
        meta.addCustomEffect(BLOOD_FLASK_CUSTOM_EFFECT, false);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ATTRIBUTES);
        
        flask.setItemMeta(meta);
        return flask;
    }

    /**
     * Checks if an item is a blood flask.
     * 
     * @param item The item to check
     * @return true if the item is a blood flask
     */
    public boolean isBloodFlask(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return BLOOD_FLASK_NAME.equals(item.getItemMeta().getDisplayName());
    }

    /**
     * Gets the amount of blood in a blood flask.
     * 
     * @param item The blood flask item
     * @return The amount of blood, or 0 if not a valid blood flask
     */
    public double getBloodFlaskAmount(ItemStack item) {
        if (!isBloodFlask(item) || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return 0.0;
        }
        
        String amountLine = item.getItemMeta().getLore().get(0);
        try {
            return Double.parseDouble(amountLine.split(" ")[0]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return 0.0;
        }
    }

    /**
     * Checks if a blood flask contains vampiric blood.
     * 
     * @param item The blood flask item
     * @return true if the blood is vampiric
     */
    public boolean isBloodFlaskVampiric(ItemStack item) {
        if (!isBloodFlask(item) || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return false;
        }
        return BLOOD_FLASK_VAMPIRIC_TRUE.equals(item.getItemMeta().getLore().get(1));
    }

    /**
     * Consumes a glass bottle from the player's inventory.
     * 
     * @param player The player whose bottle should be consumed
     */
    public void consumeGlassBottle(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack item = inventory.getItemInMainHand();
        
        if (item != null && item.getType() == Material.GLASS_BOTTLE) {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                inventory.setItemInMainHand(null);
            }
        }
    }

    /**
     * Creates a blood flask from a player's blood and gives it to them.
     * 
     * @param amount The amount of blood to put in the flask
     * @param player The vampire player creating the flask
     */
    public void createBloodFlaskFromPlayer(double amount, VampirePlayer player) {
        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer == null) return;

        // Consume the glass bottle
        consumeGlassBottle(bukkitPlayer);
        
        // Create and give the blood flask
        ItemStack flask = createBloodFlask(amount, player.isVampire());
        bukkitPlayer.getInventory().addItem(flask);
        
        // Notify the player
        player.sendMessage(plugin.getLanguageConfig().getMessage("blood-flask.created", 
            String.format("%.1f", amount)));
    }
} 