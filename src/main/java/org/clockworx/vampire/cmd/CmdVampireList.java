package org.clockworx.vampire.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.clockworx.vampire.VampirePermission;
import org.clockworx.vampire.VampirePlugin;
import org.clockworx.vampire.entity.VampirePlayer;
import org.clockworx.vampire.manager.VampireManager;

/**
 * Command for listing online vampires and infected players.
 * Retrieves data from cached online players via VampireManager.
 */
public class CmdVampireList extends VCommand {

    private final VampireManager vampireManager;
    private static final int PAGE_SIZE = 10;

    /**
     * Creates a new list command.
     */
    public CmdVampireList(VampirePlugin plugin) {
        super(plugin, "list", VampirePermission.LIST, 
              "Show list of online vampires and infected", "[page]");
        this.vampireManager = plugin.getVampireManager();
    }

    @Override
    protected boolean execute(CommandSender sender, Command command, String label, String[] args) {
        int page = 1;
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                sendError(sender, getMessage("command.list.invalid_page"));
                return true;
            }
        }

        Collection<VampirePlayer> onlineData = vampireManager.getCachedOnlinePlayers();

        List<String> onlineVampires = onlineData.stream()
                .filter(VampirePlayer::isVampire)
                .map(VampirePlayer::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        List<String> onlineInfected = onlineData.stream()
                .filter(vp -> !vp.isVampire() && vp.isInfected())
                .map(vp -> vp.getName() + String.format(" (%.0f%%)", vp.getInfectionLevel() * 100))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());

        List<String> combinedList = new ArrayList<>();
        if (!onlineVampires.isEmpty()) {
            combinedList.add("&cVampires (&aOnline&c):");
            combinedList.addAll(onlineVampires);
        }
        if (!onlineInfected.isEmpty()) {
            combinedList.add("&eInfected (&aOnline&e):");
            combinedList.addAll(onlineInfected);
        }

        if (combinedList.isEmpty()) {
            sendInfo(sender, getMessage("command.list.none_found"));
            return true;
        }

        int totalItems = combinedList.size();
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        if (page > totalPages) {
            page = totalPages;
        }

        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, totalItems);

        sendInfo(sender, getMessage("command.list.header")
            .replace("%page%", String.valueOf(page))
            .replace("%totalpages%", String.valueOf(totalPages)));

        for (int i = startIndex; i < endIndex; i++) {
            String prefix = combinedList.get(i).startsWith("&c") || combinedList.get(i).startsWith("&e") ? "" : "  &f- ";
            sendInfo(sender, prefix + combinedList.get(i));
        }

        if (totalPages > 1) {
            String paginationHelp = (page > 1 ? "&e/vampire list " + (page - 1) + " &7(Prev)  " : "") +
                                   (page < totalPages ? "&e/vampire list " + (page + 1) + " &7(Next)" : "");
            sendInfo(sender, paginationHelp);
        }

        return true;
    }

    @Override
    protected List<String> tabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("1", "2", "3").stream()
                       .filter(p -> p.startsWith(args[0]))
                       .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
} 