package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.manager.VampireManager;
import org.clockworx.vampire.util.VampireMessages;

/**
 * Command for managing vampire levels.
 * Allows rank3 admins to increase, decrease, or set player vampire levels.
 */
public class CmdVampireLevel extends VCommand {

    private final VampireManager vampireManager;
    private static final List<String> OPERATIONS = Arrays.asList("increase", "decrease", "set");

    /**
     * Creates a new level command.
     *
     * @param plugin The plugin instance.
     */
    public CmdVampireLevel(VampirePlugin plugin) {
        super(plugin, "level", VampirePermission.KIT_RANK3,
              "Manage vampire levels", "<increase|decrease|set> [amount] [player]");
        this.vampireManager = plugin.getVampireManager();
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            VampireMessages.sendLocalized(sender, "command.level.usage", getName(), getUsage());
            return false;
        }

        String operation = args[0].toLowerCase();
        if (!OPERATIONS.contains(operation)) {
            VampireMessages.sendLocalized(sender, "command.level.error.invalid_operation", operation);
            return true;
        }

        // Determine amount and player arguments
        // For "set", amount is required; for "increase"/"decrease", amount defaults to 1
        int amount = 1;
        int playerArgIndex = -1;
        
        if (operation.equals("set")) {
            // set requires amount: /vampire level set <amount> [player]
            if (args.length < 2) {
                VampireMessages.sendLocalized(sender, "command.level.usage", getName(), getUsage());
                return false;
            }
            try {
                amount = Integer.parseInt(args[1]);
                if (amount < 0) {
                    VampireMessages.sendLocalized(sender, "command.level.error.invalid_amount", String.valueOf(amount));
                    return true;
                }
                playerArgIndex = (args.length > 2) ? 2 : -1;
            } catch (NumberFormatException e) {
                VampireMessages.sendLocalized(sender, "command.level.error.invalid_amount", args[1]);
                return true;
            }
        } else {
            // increase/decrease: amount is optional, defaults to 1
            // /vampire level increase [amount] [player]
            if (args.length > 1) {
                try {
                    amount = Integer.parseInt(args[1]);
                    if (amount <= 0) {
                        VampireMessages.sendLocalized(sender, "command.level.error.invalid_amount", String.valueOf(amount));
                        return true;
                    }
                    playerArgIndex = (args.length > 2) ? 2 : -1;
                } catch (NumberFormatException e) {
                    // args[1] is not a number, treat it as player name
                    playerArgIndex = 1;
                }
            }
        }

        // Resolve target player
        TargetResolution target = resolveTargetPlayer(sender, args, playerArgIndex, true);
        if (target == null) {
            return true; // Error message already sent
        }

        // Check if target is a vampire
        org.clockworx.vampire.entity.VampirePlayer vp = vampireManager.getCachedVampirePlayer(target.uuid);
        if (vp == null) {
            VampireMessages.sendLocalized(sender, "command.error.player_data_not_found", target.name);
            return true;
        }

        if (!vp.isVampire()) {
            VampireMessages.sendLocalized(sender, "command.level.error.not_vampire", target.name);
            return true;
        }

        // Perform the operation
        boolean success = false;
        int oldLevel = vp.getVampireLevel();
        int newLevel = oldLevel;

        switch (operation) {
            case "increase":
                success = vampireManager.increaseVampireLevel(target.uuid, amount);
                if (success) {
                    newLevel = oldLevel + amount;
                    VampireMessages.sendLocalized(sender, "command.level.success.increase", 
                        target.name, String.valueOf(amount), String.valueOf(oldLevel), String.valueOf(newLevel));
                } else {
                    // Check if it was a bounds error
                    int maxLevel = plugin.getLevelManager().getMaxLevel();
                    if (oldLevel + amount > maxLevel) {
                        VampireMessages.sendLocalized(sender, "command.level.error.above_max", 
                            target.name, String.valueOf(maxLevel));
                    } else {
                        VampireMessages.sendLocalized(sender, "command.level.error.failed", target.name);
                    }
                }
                break;

            case "decrease":
                success = vampireManager.decreaseVampireLevel(target.uuid, amount);
                if (success) {
                    newLevel = oldLevel - amount;
                    VampireMessages.sendLocalized(sender, "command.level.success.decrease", 
                        target.name, String.valueOf(amount), String.valueOf(oldLevel), String.valueOf(newLevel));
                } else {
                    // Check if it was a bounds error
                    int minLevel = plugin.getLevelManager().getMinLevel();
                    if (oldLevel - amount < minLevel) {
                        VampireMessages.sendLocalized(sender, "command.level.error.below_min", 
                            target.name, String.valueOf(minLevel));
                    } else {
                        VampireMessages.sendLocalized(sender, "command.level.error.failed", target.name);
                    }
                }
                break;

            case "set":
                success = vampireManager.setVampireLevel(target.uuid, amount);
                if (success) {
                    VampireMessages.sendLocalized(sender, "command.level.success.set", 
                        target.name, String.valueOf(amount));
                } else {
                    // Check bounds
                    int minLevel = plugin.getLevelManager().getMinLevel();
                    int maxLevel = plugin.getLevelManager().getMaxLevel();
                    if (amount < minLevel) {
                        VampireMessages.sendLocalized(sender, "command.level.error.below_min", 
                            target.name, String.valueOf(minLevel));
                    } else if (amount > maxLevel) {
                        VampireMessages.sendLocalized(sender, "command.level.error.above_max", 
                            target.name, String.valueOf(maxLevel));
                    } else {
                        VampireMessages.sendLocalized(sender, "command.level.error.failed", target.name);
                    }
                }
                break;
        }

        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return OPERATIONS.stream()
                .filter(op -> op.startsWith(input))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String operation = args[0].toLowerCase();
            if (operation.equals("set")) {
                // Suggest level numbers
                int maxLevel = plugin.getLevelManager().getMaxLevel();
                List<String> suggestions = new ArrayList<>();
                String input = args[1];
                for (int i = 0; i <= maxLevel; i++) {
                    String levelStr = String.valueOf(i);
                    if (levelStr.startsWith(input)) {
                        suggestions.add(levelStr);
                    }
                }
                return suggestions;
            } else if (operation.equals("increase") || operation.equals("decrease")) {
                // Could be amount or player name - try to parse as number first
                String input = args[1].toLowerCase();
                try {
                    Integer.parseInt(input);
                    // It's a number, suggest player names
                    return Bukkit.getOnlinePlayers().stream()
                        .map(p -> p.getName())
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .sorted()
                        .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    // Not a number, suggest player names
                    return Bukkit.getOnlinePlayers().stream()
                        .map(p -> p.getName())
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .sorted()
                        .collect(Collectors.toList());
                }
            }
        }
        
        if (args.length == 3) {
            // Third argument is always player name
            String input = args[2].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                .map(p -> p.getName())
                .filter(name -> name.toLowerCase().startsWith(input))
                .sorted()
                .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}

