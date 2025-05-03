package org.clockworx.vampire.cmd;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command to set various vampire-related properties for a player.
 * Requires admin permissions (vampire.set or specific sub-permissions like vampire.set.level).
 */
public class CmdVampireSet extends VCommand {

    private final VampireManager vampireManager;
    private static final List<String> SET_TYPES = Arrays.asList("vampire", "infection", "blood", "level");

    public CmdVampireSet(VampirePlugin plugin) {
        super(plugin, "set", VampirePermission.SET, 
              "Set player vampire properties", "<type> <value> [player]");
        this.vampireManager = plugin.getVampireManager();
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 2) {
            VampireMessages.sendLocalized(sender, "command.usage.set", getName(), getUsage());
            return false;
        }

        String type = args[0].toLowerCase();
        String valueStr = args[1];
        String targetName = (args.length > 2) ? args[2] : null;
        UUID targetUuid = null;
        OfflinePlayer targetPlayer = null;

        // Determine target player
        if (targetName != null) {
            targetPlayer = Bukkit.getOfflinePlayer(targetName);
            if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
                VampireMessages.sendLocalized(sender, "player.not_found", targetName);
                return true;
            }
            targetUuid = targetPlayer.getUniqueId();
        } else {
            if (!(sender instanceof Player)) {
                VampireMessages.sendLocalized(sender, "command.error.player_only_or_specify");
                return true;
            }
            targetPlayer = (Player) sender;
            targetUuid = targetPlayer.getUniqueId();
        }

        // Check specific permission for the type AFTER determining type and BEFORE processing
        String requiredPermission = getPermissionForType(type);
        if (!sender.hasPermission(requiredPermission)) {
            VampireMessages.sendLocalized(sender, "command.no_permission");
            return true;
        }

        // Ensure target player name is resolved for messages
        final String finalTargetName = targetPlayer.getName(); 

        // --- Handle different set types --- 
        switch (type) {
            case "vampire":
                boolean isVampire;
                if (valueStr.equalsIgnoreCase("true") || valueStr.equalsIgnoreCase("on") || valueStr.equals("1")) {
                    isVampire = true;
                } else if (valueStr.equalsIgnoreCase("false") || valueStr.equalsIgnoreCase("off") || valueStr.equals("0")) {
                    isVampire = false;
                } else {
                    VampireMessages.sendLocalized(sender, "command.error.invalid_boolean", valueStr);
                    return true;
                }
                vampireManager.setVampireStatus(targetUuid, isVampire, "command");
                VampireMessages.sendLocalized(sender, "command.set.success.vampire", finalTargetName, valueStr);
                break;

            case "infection":
                try {
                    double infection = Double.parseDouble(valueStr);
                    if (infection < 0.0 || infection > 1.0) {
                        VampireMessages.sendLocalized(sender, "command.error.invalid_range_01", valueStr);
                        return true;
                    }
                    plugin.getVampireManager().setInfectionLevel(targetUuid, infection, "command");
                    VampireMessages.sendLocalized(sender, "command.set.success.infection", finalTargetName, String.format("%.2f", infection));
                } catch (NumberFormatException e) {
                    VampireMessages.sendLocalized(sender, "command.error.invalid_number", valueStr);
                    return true;
                }
                break;

            case "blood":
                try {
                    double blood = Double.parseDouble(valueStr);
                    if (blood < 0.0) {
                        VampireMessages.sendLocalized(sender, "command.error.invalid_non_negative", valueStr);
                        return true;
                    }
                    plugin.getVampireManager().setBloodLevel(targetUuid, blood);
                    VampireMessages.sendLocalized(sender, "command.set.success.blood", finalTargetName, String.format("%.1f", blood));
                } catch (NumberFormatException e) {
                    VampireMessages.sendLocalized(sender, "command.error.invalid_number", valueStr);
                    return true;
                }
                break;
            
            case "level":
                try {
                    int level = Integer.parseInt(valueStr);
                    if (level < 0) {
                        VampireMessages.sendLocalized(sender, "command.error.invalid_non_negative_int", valueStr);
                        return true;
                    }
                    boolean success = vampireManager.setVampireLevel(targetUuid, level);
                    if (success) {
                        VampireMessages.sendLocalized(sender, "command.set.success.level", finalTargetName, String.valueOf(level));
                    } else {
                        VampireMessages.sendLocalized(sender, "command.set.fail.level", finalTargetName, String.valueOf(level));
                    }
                } catch (NumberFormatException e) {
                    VampireMessages.sendLocalized(sender, "command.error.invalid_integer", valueStr);
                    return true;
                }
                break;

            default:
                VampireMessages.sendLocalized(sender, "command.error.unknown_set_type", type);
                return false;
        }

        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return SET_TYPES.stream()
                    .filter(type -> sender.hasPermission(getPermissionForType(type)))
                    .filter(type -> type.startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String type = args[0].toLowerCase();
            if (sender.hasPermission(getPermissionForType(type))) {
                 if (type.equals("vampire")) {
                    return Arrays.asList("true", "false").stream()
                        .filter(val -> val.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                 } else if (type.equals("level")) {
                     return Arrays.asList("0", "1", "2", "3", "4", "5").stream()
                         .filter(val -> val.startsWith(args[1]))
                         .collect(Collectors.toList());
                 } else if (type.equals("infection")) {
                      return Arrays.asList("0.0", "0.5", "1.0").stream()
                         .filter(val -> val.startsWith(args[1]))
                         .collect(Collectors.toList());
                 }
            }
        }

        if (args.length == 3) {
            String type = args[0].toLowerCase();
            if (sender.hasPermission(getPermissionForType(type))) {
                String input = args[2].toLowerCase();
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .sorted()
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    /**
     * Helper to get the specific permission node required for a set type.
     * Returns the base SET permission if the type is unknown, though this case
     * should ideally be handled before calling this.
     */
    private String getPermissionForType(String type) {
        switch(type) {
            case "vampire": return VampirePermission.SET;
            case "infection": return VampirePermission.SET_INFECTION;
            case "blood": return VampirePermission.SET_FOOD;
            case "level": return VampirePermission.SET_LEVEL;
            default: return VampirePermission.SET;
        }
    }
} 