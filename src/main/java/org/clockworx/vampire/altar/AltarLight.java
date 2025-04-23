package org.clockworx.vampire.altar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.event.EventVampirePlayerInfectionChange;
import org.clockworx.vampire.manager.AltarManager;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.FxUtil;
import org.clockworx.vampire.util.ResourceUtil;
import org.clockworx.vampire.util.VampireMessages;

/**
 * The Light Altar allows players to decrease their infection or cure themselves of vampirism.
 * This altar requires specific materials to construct and resources to activate.
 * 
 * Infection Mechanics:
 * 1. If player is not infected (infection = 0.0):
 *    - No effect (already cured)
 * 2. If player is infected (0.0 < infection < 1.0):
 *    - Decreases infection by 0.2
 *    - If infection reaches 0.0, player is cured
 *    - Triggers EventVampirePlayerInfectionChange
 * 3. If player is fully infected (infection = 1.0):
 *    - Decreases infection to 0.8
 *    - Player is no longer a vampire
 *    - Triggers EventVampirePlayerInfectionChange
 * 
 *  * When used, it will:
 * 1. Check if the player is infected
 * 2. Apply visual and sound effects
 * 3. Consume required resources
 * 4. Decrease the player's infection rate
 * 5. If infection reaches 0.0, cure the player
 * 
 * <p>Configuration for materials and resources is loaded from the main plugin config 
 * under the `altars.light` section.</p>
 * 
 * <p>Usage Logic (mirrors Dark Altar but for curing):</p>
 * <ol>
 *   <li>Player interacts with the core block ({@link #coreMaterial}).</li>
 *   <li>{@link AltarManager#determineAltarType(Block)} validates the structure.</li>
 *   <li>{@link AltarManager#handleBlockInteract(Block, Player)} handles event, permission, precondition, and resource checks.</li>
 *   <li>If checks pass, {@link AltarManager#startAltarRitual(AltarAbstract, VampirePlayer, Player)} is called.</li>
 *   <li>{@code startAltarRitual} calls {@link #applyStartEffects(VampirePlayer, Player)}, registers location, and schedules a task.</li>
 *   <li>The scheduled task checks for movement using {@link #hasPlayerMoved(Player)}.</li>
 *   <li>If no movement, the task calls {@link #consumeResources(VampirePlayer, Player)}.</li>
 *   <li>If resources are consumed, the task calls {@link #applyEffects(VampirePlayer, Player, Block, VampireManager)}.</li>
 *   <li>{@code applyEffects} calculates reduced infection, calls {@link EventVampirePlayerInfectionChange}, applies the change, and potentially cures the player (sets infection to 0, vampire to false). Also applies minor healing.</li>
 * </ol>
 */
public class AltarLight extends AltarAbstract {
    
    // Constant for how much infection is reduced per use.
    private static final double INFECTION_DECREASE_AMOUNT = 0.2;

    /**
     * Constructs and configures the Light Altar instance.
     * Reads required materials, core block, and resource costs from the plugin configuration.
     * 
     * @param plugin The main {@link VampirePlugin} instance.
     */
    public AltarLight(VampirePlugin plugin) {
        super(plugin);
        this.name = "Light Altar";
        this.desc = "&fAn altar shimmering with faint light...";
        
        // Load specific configuration for the Light Altar
        Map<String, Object> configMap = plugin.getVampireConfig().getLightAltarConfig();
        if (configMap == null) {
            VampireMessages.error("Light Altar configuration section ('altars.light') missing or invalid! Using defaults.", null);
            configMap = new HashMap<>(); // Use empty map to avoid NullPointerExceptions below
        }
        
        // --- Configure Core Material ---
        String coreMaterialStr = (String) configMap.getOrDefault("core-material", "DIAMOND_BLOCK");
        try {
            this.coreMaterial = Material.valueOf(coreMaterialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            VampireMessages.error("Invalid core-material in light altar config: " + coreMaterialStr + ". Defaulting to DIAMOND_BLOCK.", e);
            this.coreMaterial = Material.DIAMOND_BLOCK;
        }
        
        // --- Set Abstract Properties ---
        this.usePermission = VampirePermission.ALTAR_LIGHT; // Reference permission constant
        this.successMessageKey = "altar.light.success"; // Need to add this key to en.yml

        // --- Configure Channeling --- (Needs config keys: altars.light.channeling_delay_ticks, altars.light.max_movement_distance)
        this.channelingDelayTicks = plugin.getVampireConfig().getConfig().getInt("altar.light.channeling_delay_ticks", 60); // Default 3 seconds
        double maxMoveDist = plugin.getVampireConfig().getConfig().getDouble("altar.light.max_movement_distance", 1.5); // Default 1.5 blocks
        this.maxMovementDistanceSquared = maxMoveDist * maxMoveDist;

        // --- Configure Structure Materials ---
        this.materialCounts = new HashMap<>(); 
        Object materialsObj = configMap.get("materials"); // Get the object first

        // Check if the retrieved object is actually a ConfigurationSection
        if (materialsObj instanceof ConfigurationSection) {
            ConfigurationSection materialsSection = (ConfigurationSection) materialsObj;
            for (String materialKey : materialsSection.getKeys(false)) { // Iterate through keys
                try {
                    Material material = Material.valueOf(materialKey.toUpperCase());
                    int count = materialsSection.getInt(materialKey, 1); // Get int value using Bukkit API
                    if (count > 0) {
                        this.materialCounts.put(material, count);
                    }
                } catch (IllegalArgumentException e) {
                    VampireMessages.error("Invalid material key in light altar config: " + materialKey, e);
                }
            }
        } else if (materialsObj != null) {
            // Log an error if 'materials' exists but isn't a section
            VampireMessages.error("Invalid 'materials' format in light altar config: Expected a map/section, found " + materialsObj.getClass().getName(), null);
        }
        // If materialsObj is null or not a ConfigurationSection, materialCounts remains empty or partially filled.

        this.materialCounts.putIfAbsent(this.coreMaterial, 1); // Ensure core is included
        
        // --- Configure Resource Costs ---
        this.resources = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<String> resourceStrings = (List<String>) configMap.getOrDefault("resources", List.of("LAPIS_LAZULI:10", "DIAMOND:1")); // Default resources
        
        this.resources = resourceStrings.stream()
            .map(str -> {
                if (str == null || str.isEmpty()) return null;
                String[] parts = str.split(":");
                if (parts.length != 2) {
                     VampireMessages.error("Invalid resource format in light altar config (Expected MATERIAL:AMOUNT): " + str, null);
                    return null;
                }
                try {
                    Material material = Material.valueOf(parts[0].trim().toUpperCase());
                    int amount = Integer.parseInt(parts[1].trim());
                     if (amount <= 0) {
                         VampireMessages.error("Invalid resource amount in light altar config (must be > 0): " + str, null);
                         return null;
                    }
                    return new ItemStack(material, amount);
                } catch (IllegalArgumentException | NullPointerException e) {
                     VampireMessages.error("Invalid resource material/amount in light altar config: " + str, e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    // --- Implement Abstract Methods ---

    @Override
    public boolean checkPreconditions(VampirePlayer vp, Player player) {
        if (!vp.isVampire() && vp.getInfectionLevel() <= 0) {
            VampireMessages.sendLocalized(player, "altar.light.fail.already_cured");
            return false;
        }
        return true;
    }

    @Override
    public boolean checkResources(VampirePlayer vp, Player player) {
        PlayerInventory inventory = player.getInventory();
        for (ItemStack resource : this.resources) {
            if (resource == null || resource.getAmount() <= 0) continue;
            if (!inventory.containsAtLeast(resource, resource.getAmount())) {
                String itemName = ResourceUtil.getMaterialName(resource.getType());
                VampireMessages.sendLocalized(player, "altar.fail.resources", resource.getAmount(), itemName);
                return false;
            }
        }
        return true;
    }

    @Override
    public void applyStartEffects(VampirePlayer vp, Player player) {
        VampireMessages.sendLocalized(player, "altar.light.start_ritual");
        FxUtil.ensure(PotionEffectType.GLOWING, player, getChannelingDelayTicks() + 20);
        FxUtil.playSound(player.getLocation(), "minecraft:sound.beacon.activate", 0.8f, 1.5f);
        FxUtil.playParticle(player.getEyeLocation(), Particle.END_ROD, 15, 0.5, 0.5, 0.5, 0.01);
    }

    @Override
    public boolean consumeResources(VampirePlayer vp, Player player) {
        PlayerInventory inventory = player.getInventory();
        for (ItemStack resource : this.resources) {
             if (!inventory.containsAtLeast(resource, resource.getAmount())) {
                 VampireMessages.sendLocalized(player, "altar.fail.resource_missing_last_moment");
                 return false; 
            }
        }
        for (ItemStack resource : this.resources) {
            ItemStack toRemove = resource.clone();
            inventory.removeItemAnySlot(toRemove);
        }
        return true;
    }

    /**
     * Applies the curing logic of the Light Altar.
     * Called by the scheduled task in the AltarManager if the player hasn't moved.
     * Decreases infection level, potentially cures the player (removes vampire status), and applies minor healing.
     *
     * @param vampirePlayer The {@link VampirePlayer} instance of the player.
     * @param player The Bukkit {@link Player} instance.
     * @param block The core altar {@link Block} interacted with.
     * @param manager The {@link VampireManager} instance.
     */
    @Override
    public void applyEffects(VampirePlayer vampirePlayer, Player player, Block block, VampireManager manager) {
        double currentInfection = vampirePlayer.getInfectionLevel();
        double newInfectionLevel = Math.max(0.0, currentInfection - INFECTION_DECREASE_AMOUNT);
        boolean wasVampire = vampirePlayer.isVampire();
        
        EventVampirePlayerInfectionChange event = new EventVampirePlayerInfectionChange(newInfectionLevel, vampirePlayer);
        plugin.getServer().getPluginManager().callEvent(event);
        
        if (!event.isCancelled()) {
             double finalInfectionLevel = event.getInfection();
             
             manager.setInfectionLevel(vampirePlayer.getUuid(), finalInfectionLevel, "Used Light Altar");
            
            if (finalInfectionLevel <= 0.0) {
                 if (wasVampire) {
                    manager.setVampireStatus(vampirePlayer.getUuid(), false, "Cured by Light Altar");
                    VampireMessages.sendLocalized(player, "altar.light.effect.cured_vampire");
                    FxUtil.playCureEffect(player);
                 } else {
                     VampireMessages.sendLocalized(player, "altar.light.effect.cured_infection");
                     FxUtil.playCureEffect(player);
                 }
            } else if (wasVampire && finalInfectionLevel < 1.0) {
                 manager.setVampireStatus(vampirePlayer.getUuid(), false, "Infection reduced by Light Altar");
                 VampireMessages.sendLocalized(player, "altar.light.effect.weakened_curse");
            } else {
                VampireMessages.sendLocalized(player, "altar.light.effect.decrease_infection");
                FxUtil.runHeal(player);
            }
            
            AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealthAttribute == null) {
                VampireMessages.error("Could not get max health attribute for player " + player.getName() + " during AltarLight use.", null);
            } else {
                double maxHealth = maxHealthAttribute.getValue();
                player.setHealth(Math.min(maxHealth, player.getHealth() + 4.0));
            }
            
            if (!(finalInfectionLevel <= 0.0)) { 
                FxUtil.playAltarLightSuccessEffect(player);
            }

            if(getSuccessMessageKey() != null && !getSuccessMessageKey().isEmpty()){
                VampireMessages.sendLocalized(player, getSuccessMessageKey());
            }

        } else {
             VampireMessages.sendLocalized(player, "altar.fail.cancelled");
            ResourceUtil.playerAdd(player, this.resources);
            FxUtil.playAltarFailEffect(player.getLocation());
        }
    }
} 