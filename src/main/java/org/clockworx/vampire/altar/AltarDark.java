package org.clockworx.vampire.altar;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.event.EventVampirePlayerInfectionChange;
import org.clockworx.vampire.util.FxUtil;
import org.clockworx.vampire.util.ResourceUtil;
import org.clockworx.vampire.util.VampireMessages;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.VampirePermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents the Dark Altar, a structure used to infect players with vampirism 
 * or increase their existing infection level towards becoming a full vampire.
 * 
 * <p>Configuration for materials and resources is loaded from the main plugin config 
 * under the `altars.dark` section.</p>
 * 
 * <p>Usage Logic:</p>
 * <ol>
 *   <li>Player interacts with the core block ({@link #coreMaterial}).</li>
 *   <li>{@link AltarManager#determineAltarType(Block)} validates the structure.</li>
 *   <li>{@link AltarManager#handleBlockInteract(Block, Player)} handles event, permission, precondition, and resource checks.</li>
 *   <li>If checks pass, {@link AltarManager#startAltarRitual(AltarAbstract, VampirePlayer, Player)} is called.</li>
 *   <li>{@code startAltarRitual} calls {@link #applyStartEffects(VampirePlayer, Player)}, registers location, and schedules a task.</li>
 *   <li>The scheduled task checks for movement using {@link #hasPlayerMoved(Player)}.</li>
 *   <li>If no movement, the task calls {@link #consumeResources(VampirePlayer, Player)}.</li>
 *   <li>If resources are consumed, the task calls {@link #applyEffects(VampirePlayer, Player, Block, VampireManager)}.</li>
 *   <li>{@code applyEffects} calculates new infection, calls {@link EventVampirePlayerInfectionChange}, applies the infection, and potentially turns the player into a vampire.</li>
 * </ol>
 */
public class AltarDark extends AltarAbstract {
    
    // Constants for infection logic
    private static final double INFECTION_INCREASE_AMOUNT = 0.2;
    private static final double INITIAL_INFECTION_AMOUNT = 0.1;

    /**
     * Constructs and configures the Dark Altar instance.
     * Reads required materials, core block, and resource costs from the plugin configuration.
     * 
     * @param plugin The main {@link VampirePlugin} instance.
     */
    public AltarDark(VampirePlugin plugin) {
        super(plugin);
        this.name = "Dark Altar";
        this.desc = "&8An altar pulsing with dark energy..."; 
        
        // Load specific configuration for the Dark Altar
        Map<String, Object> configMap = plugin.getVampireConfig().getDarkAltarConfig();
        if (configMap == null) {
            VampireMessages.error("Dark Altar configuration section ('altars.dark') missing or invalid! Using defaults.", null);
            configMap = new HashMap<>(); // Use empty map to avoid NullPointerExceptions below
        }
        
        // --- Configure Core Material ---
        String coreMaterialStr = (String) configMap.getOrDefault("core-material", "OBSIDIAN");
        try {
            this.coreMaterial = Material.valueOf(coreMaterialStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            VampireMessages.error("Invalid core-material in dark altar config: " + coreMaterialStr + ". Defaulting to OBSIDIAN.", e);
            this.coreMaterial = Material.OBSIDIAN;
        }
        
        // --- Set Abstract Properties ---
        this.usePermission = VampirePermission.ALTAR_DARK; // Reference permission constant
        this.successSound = Sound.ENTITY_WITHER_SPAWN; // Example sound
        this.successMessageKey = "altar.dark.success"; // Need to add this key to en.yml

        // --- Configure Channeling --- (Needs config keys: altars.dark.channeling_delay_ticks, altars.dark.max_movement_distance)
        this.channelingDelayTicks = plugin.getVampireConfig().getConfig().getInt("altar.dark.channeling_delay_ticks", 60); // Default 3 seconds
        double maxMoveDist = plugin.getVampireConfig().getConfig().getDouble("altar.dark.max_movement_distance", 1.5); // Default 1.5 blocks
        this.maxMovementDistanceSquared = maxMoveDist * maxMoveDist;

        // --- Configure Structure Materials ---
        // Defaults to requiring only the core material if not specified
        this.materialCounts = new HashMap<>(); 
        @SuppressWarnings("unchecked") // Suppress warning for unchecked cast
        Map<String, Integer> materialsConfig = (Map<String, Integer>) configMap.getOrDefault("materials", new HashMap<>());
        for (Map.Entry<String, Integer> entry : materialsConfig.entrySet()) {
            try {
                Material material = Material.valueOf(entry.getKey().toUpperCase());
                int count = entry.getValue() != null ? entry.getValue() : 1; // Default count to 1 if null
                if (count > 0) {
                    this.materialCounts.put(material, count);
                }
            } catch (IllegalArgumentException | NullPointerException e) {
                VampireMessages.error("Invalid material key/value in dark altar config: " + entry.getKey() + ", value: " + entry.getValue(), e);
            }
        }
        // Ensure core material is included if not explicitly defined
        this.materialCounts.putIfAbsent(this.coreMaterial, 1);
        
        // --- Configure Resource Costs ---
        this.resources = new ArrayList<>(); // Initialize the list
        @SuppressWarnings("unchecked")
        List<String> resourceStrings = (List<String>) configMap.getOrDefault("resources", List.of("REDSTONE:10", "WITHER_ROSE:1")); // Default resources
        
        // Parse resource strings (e.g., "DIAMOND:1", "REDSTONE:10")
        this.resources = resourceStrings.stream()
            .map(str -> {
                if (str == null || str.isEmpty()) return null;
                String[] parts = str.split(":");
                if (parts.length != 2) {
                    VampireMessages.error("Invalid resource format in dark altar config (Expected MATERIAL:AMOUNT): " + str, null);
                    return null;
                }
                try {
                    Material material = Material.valueOf(parts[0].trim().toUpperCase());
                    int amount = Integer.parseInt(parts[1].trim());
                    if (amount <= 0) {
                         VampireMessages.error("Invalid resource amount in dark altar config (must be > 0): " + str, null);
                         return null;
                    }
                    return new ItemStack(material, amount);
                } catch (IllegalArgumentException | NullPointerException e) {
                    VampireMessages.error("Invalid resource material/amount in dark altar config: " + str, e);
                    return null;
                }
            })
            .filter(Objects::nonNull) // Remove any nulls resulting from parsing errors
            .collect(Collectors.toList()); // Collect into the final list
    }
    
    // --- Implement Abstract Methods ---

    @Override
    public boolean checkPreconditions(VampirePlayer vp, Player player) {
        if (vp.isVampire()) {
            VampireMessages.sendLocalized(player, "altar.dark.fail.already_vampire"); 
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
        // TODO: Add blood cost check if applicable for Dark Altar?
        return true;
    }

    @Override
    public void applyStartEffects(VampirePlayer vp, Player player) {
        VampireMessages.sendLocalized(player, "altar.dark.start_ritual"); 
        FxUtil.ensure(PotionEffectType.BLINDNESS, player, getChannelingDelayTicks() + 20);
        FxUtil.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0f, 0.5f);
        FxUtil.playParticle(player.getEyeLocation(), Particle.SMOKE, 10, 0.5, 0.5, 0.5, 0.01); 
    }

    @Override
    public boolean consumeResources(VampirePlayer vp, Player player) {
        PlayerInventory inventory = player.getInventory();
        // Double-check resources before consuming
        for (ItemStack resource : this.resources) {
            if (!inventory.containsAtLeast(resource, resource.getAmount())) {
                VampireMessages.sendLocalized(player, "altar.fail.resource_missing_last_moment");
                return false; // Resources disappeared during delay
            }
        }
        // Consume items
        for (ItemStack resource : this.resources) {
            ItemStack toRemove = resource.clone(); 
            inventory.removeItemAnySlot(toRemove);
        }
        // TODO: Consume blood cost if applicable
        return true;
    }
    
    /**
     * Applies the infection logic of the Dark Altar.
     * This method is called by the scheduled task in the AltarManager if the player hasn't moved.
     * It increases the player's infection level or infects them initially, potentially turning them into a vampire.
     *
     * @param vampirePlayer The {@link VampirePlayer} instance of the player.
     * @param player The Bukkit {@link Player} instance.
     * @param block The core altar {@link Block} interacted with (potentially unused here, but part of the signature).
     * @param manager The {@link VampireManager} instance.
     */
    @Override
    public void applyEffects(VampirePlayer vampirePlayer, Player player, Block block, VampireManager manager) {
        double newInfectionLevel;
        boolean wasInfected = vampirePlayer.isInfected();
        
        // Determine new infection level based on current state.
        if (wasInfected) {
            VampireMessages.sendLocalized(player, "altar.dark.effect.increase_infection"); 
            newInfectionLevel = Math.min(1.0, vampirePlayer.getInfectionLevel() + INFECTION_INCREASE_AMOUNT);
        } else {
            newInfectionLevel = INITIAL_INFECTION_AMOUNT;
        }
        
        // Call the event to allow modification or cancellation.
        EventVampirePlayerInfectionChange event = new EventVampirePlayerInfectionChange(newInfectionLevel, vampirePlayer);
        plugin.getServer().getPluginManager().callEvent(event);
        
        // Proceed only if the event was not cancelled.
        if (!event.isCancelled()) {
            double finalInfectionLevel = event.getInfection();
            
            // Apply the infection change via the manager.
            manager.setInfectionLevel(vampirePlayer.getUuid(), finalInfectionLevel, "Used Dark Altar");
            
            // Check consequences of the infection change.
            if (finalInfectionLevel >= 1.0 && !vampirePlayer.isVampire()) { 
                 manager.setVampireStatus(vampirePlayer.getUuid(), true, "Reached full infection via Dark Altar");
                 VampireMessages.sendLocalized(player, "altar.dark.effect.became_vampire"); 
                 FxUtil.playVampireEffect(player); 
            } else if (!wasInfected && finalInfectionLevel > 0) {
                 VampireMessages.sendLocalized(player, "altar.dark.effect.initial_infection"); 
                 FxUtil.playInfectionEffect(player);
            } else {
                 FxUtil.playSound(player.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 0.8f, 1.5f);
                 FxUtil.playParticle(player.getEyeLocation(), Particle.WITCH, 20, 0.5, 0.8, 0.5, 0.1);
            }

            if (!(finalInfectionLevel >= 1.0 && !vampirePlayer.isVampire())) {
                 FxUtil.playSound(player.getLocation(), getSound(), 1.0f, 0.5f);
            }
             if(getSuccessMessageKey() != null && !getSuccessMessageKey().isEmpty()){
                 VampireMessages.sendLocalized(player, getSuccessMessageKey());
             }

        } else {
            VampireMessages.sendLocalized(player, "altar.fail.cancelled"); 
            ResourceUtil.playerAdd(player, this.resources);
             FxUtil.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 0.5f);
        }
    }
} 