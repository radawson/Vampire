package org.clockworx.vampire.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.util.VampireMessages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Manages custom vampire-related items, 
 * handling identification via NBT tags and potentially creation.
 */
public class ItemManager {

    private final VampirePlugin plugin;
    
    // NamespacedKeys for custom item identification
    public final NamespacedKey BLOOD_VIAL_KEY;
    public final NamespacedKey HOLY_WATER_KEY;
    // Add keys for other future items here (e.g., STAKE_KEY)

    public ItemManager(VampirePlugin plugin) {
        this.plugin = plugin;
        this.BLOOD_VIAL_KEY = new NamespacedKey(plugin, "vampire_blood_vial");
        this.HOLY_WATER_KEY = new NamespacedKey(plugin, "vampire_holy_water");
    }

    /**
     * Checks if the given ItemStack is a Blood Vial.
     *
     * @param item The ItemStack to check.
     * @return True if it's a Blood Vial, false otherwise.
     */
    public boolean isBloodVial(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(BLOOD_VIAL_KEY, PersistentDataType.BYTE)) {
            return false;
        }
        return meta.getPersistentDataContainer().get(BLOOD_VIAL_KEY, PersistentDataType.BYTE) == (byte) 1;
    }

    /**
     * Checks if the given ItemStack is Holy Water.
     *
     * @param item The ItemStack to check.
     * @return True if it's Holy Water, false otherwise.
     */
    public boolean isHolyWater(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) { // Assuming Holy Water is also a Potion
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(HOLY_WATER_KEY, PersistentDataType.BYTE)) {
            return false;
        }
        return meta.getPersistentDataContainer().get(HOLY_WATER_KEY, PersistentDataType.BYTE) == (byte) 1;
    }

    // --- Item Creation Methods (Optional - can be added as needed) ---

    /**
     * Creates a new Blood Vial ItemStack.
     * 
     * @return A new Blood Vial ItemStack.
     */
    public ItemStack createBloodVial() {
        ItemStack bloodVial = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) bloodVial.getItemMeta();
        if (meta == null) return new ItemStack(Material.AIR); // Should not happen

        meta.setBasePotionType(org.bukkit.potion.PotionType.AWKWARD);
        String name = VampireMessages.getLocalizedMessage("item.blood_vial.name");
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        
        List<String> loreStrings = new ArrayList<>();
        loreStrings.add(VampireMessages.getLocalizedMessage("item.blood_vial.lore1")); 
        loreStrings.add(VampireMessages.getLocalizedMessage("item.blood_vial.lore2")); 

        // Convert lore strings to Components and set using Adventure API
        List<Component> loreComponents = loreStrings.stream()
            .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(line))
            .collect(Collectors.toList());
        meta.lore(loreComponents);

        meta.getPersistentDataContainer().set(BLOOD_VIAL_KEY, PersistentDataType.BYTE, (byte) 1);
        bloodVial.setItemMeta(meta);
        return bloodVial;
    }

    /**
     * Creates a new Holy Water ItemStack.
     * 
     * @return A new Holy Water ItemStack.
     */
    public ItemStack createHolyWater() {
        ItemStack holyWater = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) holyWater.getItemMeta();
        if (meta == null) return new ItemStack(Material.AIR); // Should not happen

        // Use WATER potion as base for Holy Water visual?
        meta.setBasePotionType(org.bukkit.potion.PotionType.WATER);
        // Maybe add a custom color or glow effect?
        // meta.setColor(org.bukkit.Color.AQUA);
        // meta.addCustomEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 1, 0), true);

        String name = VampireMessages.getLocalizedMessage("item.holy_water.name");
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        
        List<String> loreStrings = new ArrayList<>();
        loreStrings.add(VampireMessages.getLocalizedMessage("item.holy_water.lore1")); 
        loreStrings.add(VampireMessages.getLocalizedMessage("item.holy_water.lore2")); 

        // Convert lore strings to Components and set using Adventure API
        List<Component> loreComponents = loreStrings.stream()
            .map(line -> LegacyComponentSerializer.legacyAmpersand().deserialize(line))
            .collect(Collectors.toList());
        meta.lore(loreComponents);

        meta.getPersistentDataContainer().set(HOLY_WATER_KEY, PersistentDataType.BYTE, (byte) 1);
        holyWater.setItemMeta(meta);
        return holyWater;
    }

} 