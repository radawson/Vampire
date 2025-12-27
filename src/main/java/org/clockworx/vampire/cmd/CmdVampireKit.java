package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.altar.AltarAbstract;
import org.clockworx.vampire.manager.AltarManager;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Command class for giving players altar construction kits.
 * Creates a chest containing all required materials for building altars.
 */
public class CmdVampireKit extends VCommand {

    private final AltarManager altarManager;

    /**
     * Creates a new kit command.
     *
     * @param plugin The plugin instance.
     */
    public CmdVampireKit(VampirePlugin plugin) {
        super(plugin, "kit", VampirePermission.KIT_RANK3,
              "Get altar construction kit in a chest", "<dark|light|all>");
        this.altarManager = plugin.getAltarManager();
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (!isPlayer(sender)) {
            VampireMessages.sendLocalized(sender, "command.error.player_only");
            return true;
        }

        if (args.length < 1) {
            VampireMessages.sendLocalized(sender, "command.kit.usage", getName(), getUsage());
            return false;
        }

        Player player = (Player) sender;
        String altarType = args[0].toLowerCase();

        // Safety check
        if (altarManager == null) {
            VampireMessages.sendLocalized(sender, "command.kit.no_altars");
            return true;
        }

        List<AltarAbstract> altarsToKit = new ArrayList<>();

        // Determine which altars to create kits for
        switch (altarType) {
            case "dark":
                altarsToKit = altarManager.getAltars().stream()
                    .filter(altar -> altar.getName().toLowerCase().contains("dark"))
                    .collect(Collectors.toList());
                break;
            case "light":
                altarsToKit = altarManager.getAltars().stream()
                    .filter(altar -> altar.getName().toLowerCase().contains("light"))
                    .collect(Collectors.toList());
                break;
            case "all":
                altarsToKit = new ArrayList<>(altarManager.getAltars());
                break;
            default:
                VampireMessages.sendLocalized(sender, "command.kit.invalid_type", altarType);
                return true;
        }

        if (altarsToKit.isEmpty()) {
            VampireMessages.sendLocalized(sender, "command.kit.no_altars");
            return true;
        }

        // Create chest with materials
        Location chestLocation = findChestLocation(player.getLocation());
        if (chestLocation == null) {
            VampireMessages.sendLocalized(sender, "command.kit.no_space");
            return true;
        }

        // Place chest block
        Block chestBlock = chestLocation.getBlock();
        chestBlock.setType(Material.CHEST);

        // Get chest inventory and fill it
        if (chestBlock.getState() instanceof Chest) {
            Chest chest = (Chest) chestBlock.getState();
            org.bukkit.inventory.Inventory chestInventory = chest.getInventory();

            // Collect all materials needed
            Map<Material, Integer> materialCounts = new HashMap<>();
            List<ItemStack> resources = new ArrayList<>();

            for (AltarAbstract altar : altarsToKit) {
                // Add core block
                Material coreMaterial = altar.getCoreMaterial();
                materialCounts.put(coreMaterial, materialCounts.getOrDefault(coreMaterial, 0) + 1);

                // Add structure materials
                Map<Material, Integer> altarMaterials = altar.getMaterialCounts();
                if (altarMaterials != null) {
                    for (Map.Entry<Material, Integer> entry : altarMaterials.entrySet()) {
                        Material mat = entry.getKey();
                        int count = entry.getValue();
                        // Don't double-count core material
                        if (mat != coreMaterial) {
                            materialCounts.put(mat, materialCounts.getOrDefault(mat, 0) + count);
                        }
                    }
                }

                // Add resource items
                List<ItemStack> altarResources = altar.getResources();
                if (altarResources != null) {
                    resources.addAll(altarResources);
                }
            }

            // Add materials to chest
            int slot = 0;
            for (Map.Entry<Material, Integer> entry : materialCounts.entrySet()) {
                if (slot >= chestInventory.getSize()) break;
                ItemStack item = new ItemStack(entry.getKey(), entry.getValue());
                chestInventory.setItem(slot++, item);
            }

            // Add resource items
            for (ItemStack resource : resources) {
                if (slot >= chestInventory.getSize()) break;
                if (resource != null && resource.getType() != Material.AIR) {
                    chestInventory.setItem(slot++, resource.clone());
                }
            }

            // Update chest state
            chest.update();
        }

        // Send success message
        if (altarsToKit.size() == 1) {
            VampireMessages.sendLocalized(sender, "command.kit.success", altarsToKit.get(0).getName());
        } else {
            VampireMessages.sendLocalized(sender, "command.kit.success_all", String.valueOf(altarsToKit.size()));
        }

        return true;
    }

    /**
     * Finds a suitable location to place a chest near the player.
     * Tries the player's location first, then adjacent blocks.
     *
     * @param playerLocation The player's location.
     * @return A suitable location for the chest, or null if no space found.
     */
    private Location findChestLocation(Location playerLocation) {
        // Try player's location first
        Block block = playerLocation.getBlock();
        if (block.getType() == Material.AIR || block.getType().isAir()) {
            return block.getLocation();
        }

        // Try adjacent blocks (north, south, east, west, up)
        Location[] adjacentLocations = {
            playerLocation.clone().add(1, 0, 0),  // East
            playerLocation.clone().add(-1, 0, 0), // West
            playerLocation.clone().add(0, 0, 1),  // South
            playerLocation.clone().add(0, 0, -1), // North
            playerLocation.clone().add(0, 1, 0)   // Up
        };

        for (Location loc : adjacentLocations) {
            Block adjBlock = loc.getBlock();
            if (adjBlock.getType() == Material.AIR || adjBlock.getType().isAir()) {
                return loc;
            }
        }

        return null; // No suitable location found
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            if ("dark".startsWith(input)) suggestions.add("dark");
            if ("light".startsWith(input)) suggestions.add("light");
            if ("all".startsWith(input)) suggestions.add("all");
            return suggestions;
        }
        return new ArrayList<>();
    }
}

