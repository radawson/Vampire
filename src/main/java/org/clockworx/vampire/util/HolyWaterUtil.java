package org.clockworx.vampire.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.clockworx.vampire.VampirePlugin;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for managing holy water items in the game.
 * Holy water is a special splash potion that can cure vampirism and infection.
 */
public class HolyWaterUtil {
    private static final String HOLY_WATER_NAME = ChatColor.GREEN + "Holy Water";
    private static final List<String> HOLY_WATER_LORE = Arrays.asList(
        ChatColor.GRAY + "Ordinary water infused with lapis.",
        ChatColor.GRAY + "Very dangerous to the unholy."
    );
    private static final PotionEffect HOLY_WATER_CUSTOM_EFFECT = new PotionEffect(PotionEffectType.REGENERATION, 20, 0);

    private final VampirePlugin plugin;

    public HolyWaterUtil(VampirePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a new holy water splash potion.
     * 
     * @return The created holy water ItemStack
     */
    public ItemStack createHolyWater() {
        ItemStack holyWater = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) holyWater.getItemMeta();
        
        // Set display name and lore
        meta.setDisplayName(HOLY_WATER_NAME);
        meta.setLore(HOLY_WATER_LORE);
        
        // Add custom effect
        meta.addCustomEffect(HOLY_WATER_CUSTOM_EFFECT, false);
        
        holyWater.setItemMeta(meta);
        return holyWater;
    }

    /**
     * Checks if a thrown potion is holy water.
     * 
     * @param potion The thrown potion to check
     * @return true if the potion is holy water
     */
    public boolean isHolyWater(ThrownPotion potion) {
        return isHolyWater(potion.getItem());
    }

    /**
     * Checks if an item is holy water.
     * 
     * @param item The item to check
     * @return true if the item is holy water
     */
    public boolean isHolyWater(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName()) return false;
        return HOLY_WATER_NAME.equals(meta.getDisplayName());
    }

    /**
     * Creates holy water from a water bottle and lapis lazuli.
     * 
     * @param waterBottle The water bottle item
     * @param lapis The lapis lazuli item
     * @return The created holy water, or null if ingredients are invalid
     */
    public ItemStack createHolyWaterFromIngredients(ItemStack waterBottle, ItemStack lapis) {
        if (waterBottle == null || lapis == null) return null;
        if (waterBottle.getType() != Material.POTION || lapis.getType() != Material.LAPIS_LAZULI) return null;
        
        return createHolyWater();
    }

    /**
     * Consumes the ingredients used to create holy water.
     * 
     * @param waterBottle The water bottle to consume
     * @param lapis The lapis lazuli to consume
     */
    public void consumeIngredients(ItemStack waterBottle, ItemStack lapis) {
        if (waterBottle != null && waterBottle.getAmount() > 1) {
            waterBottle.setAmount(waterBottle.getAmount() - 1);
        } else {
            waterBottle.setType(Material.AIR);
        }
        
        if (lapis != null && lapis.getAmount() > 1) {
            lapis.setAmount(lapis.getAmount() - 1);
        } else {
            lapis.setType(Material.AIR);
        }
    }
} 